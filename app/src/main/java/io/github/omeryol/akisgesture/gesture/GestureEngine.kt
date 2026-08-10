package io.github.omeryol.akisgesture.gesture

import android.content.res.Configuration
import android.content.Intent
import android.util.TypedValue
import android.view.MotionEvent
import android.view.ViewConfiguration
import io.github.omeryol.akisgesture.action.ActionDispatcher
import io.github.omeryol.akisgesture.diagnostics.RuntimeDiagnostics
import io.github.omeryol.akisgesture.feedback.ActionSymbols
import io.github.omeryol.akisgesture.feedback.FeedbackView
import io.github.omeryol.akisgesture.feedback.HapticHelper
import io.github.omeryol.akisgesture.gesture.model.GestureResult
import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.model.GestureType
import io.github.omeryol.akisgesture.model.TriggerMode
import io.github.omeryol.akisgesture.overlay.Edge
import io.github.omeryol.akisgesture.overlay.EdgeSensorView
import io.github.omeryol.akisgesture.overlay.OverlayManager
import io.github.omeryol.akisgesture.overlay.OverlayWindowFactory
import io.github.omeryol.akisgesture.rule.CompiledRuleSet
import io.github.omeryol.akisgesture.rule.RuleProfileResolver
import io.github.omeryol.akisgesture.service.GestureAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Main Orchestration Engine for Akış Gesture.
 * Coordinates input flows, rule sets, overlay windows, touch detectors,
 * visual feedback rendering, and action dispatching.
 */
class GestureEngine(
    private val configFlow: StateFlow<GestureConfig>,
    private val actionDispatcher: ActionDispatcher,
    private val overlayManager: OverlayManager,
    private val compiledRuleSetFlow: StateFlow<CompiledRuleSet>,
    private val pausedPackagesFlow: StateFlow<Set<String>>,
    private val ruleProfilesFlow: StateFlow<Map<String, CompiledRuleSet>>,
) : EdgeSensorView.OnEdgeTouchListener {

    private var scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val detectors = mutableMapOf<Edge, EdgeGestureDetector>()
    private val edgeLengths = mutableMapOf<Edge, Float>()

    private var currentConfig: GestureConfig = configFlow.value
    private var started = false
    private var feedbackView: FeedbackView? = null

    private var lastArmed = false
    private var lastProgressActive = false
    private var lastHoldArmed = false

    /** Suppresses haptic retriggering after overlay rebuilds while a touch is still active. */
    private var suppressHaptic = false
    private var lastHapticMs = 0L
    private val HAPTIC_MIN_INTERVAL_MS = 80L

    private var foregroundPackage: String? = null
    private var adaptiveAppColor: Int? = null
    private var pausedPackages: Set<String> = pausedPackagesFlow.value
    private var pausedForForegroundApp = false
    private var pausedForSystemContext = false

    private var defaultRuleSet = compiledRuleSetFlow.value
    private var ruleProfiles = ruleProfilesFlow.value
    private var activeRuleSet = RuleProfileResolver.resolve(
        foregroundPackage = null,
        defaultRules = defaultRuleSet,
        appProfiles = ruleProfiles,
    )

    private var lockScreenVisible = false
    private var keyboardVisible = false
    private var currentKeyboardTopRatio: Float = 1.0f
    private var landscape = false
    private var fullScreen = false
    private var permissionScreen = false

    fun start() {
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        scope.launch {
            combine(
                configFlow,
                compiledRuleSetFlow,
                pausedPackagesFlow,
                ruleProfilesFlow,
            ) { config, ruleSet, packages, profiles ->
                RuntimeInputs(config, ruleSet, packages, profiles)
            }
                .collect { inputs ->
                    val oldConfig = currentConfig
                    val oldActiveRuleSet = activeRuleSet

                    currentConfig = inputs.config
                    defaultRuleSet = inputs.defaultRuleSet
                    pausedPackages = inputs.pausedPackages
                    ruleProfiles = inputs.profiles

                    activeRuleSet = RuleProfileResolver.resolve(
                        foregroundPackage,
                        defaultRuleSet,
                        ruleProfiles,
                    )

                    pausedForForegroundApp = shouldPauseForForegroundApp(
                        packageName = foregroundPackage,
                        pausedPackages = inputs.pausedPackages,
                        mode = inputs.config.appPauseMode,
                    )
                    pausedForSystemContext = shouldPauseForSystemContext()


                    if (isPaused()) {
                        clearOverlays()
                        started = true
                        return@collect
                    }

                    if (!started || oldActiveRuleSet !== activeRuleSet || detectors.isEmpty()) {
                        started = true
                        rebuildOverlays(activeRuleSet)
                    } else {
                        applyConfigDiff(oldConfig, inputs.config, activeRuleSet)
                    }
                }
        }
    }

    fun stop() {
        clearOverlays()
        scope.cancel()
        started = false
    }

    fun onForegroundAppChanged(packageName: String, adaptiveColor: Int? = null) {
        val oldActiveRuleSet = activeRuleSet
        foregroundPackage = packageName
        this.adaptiveAppColor = adaptiveColor

        activeRuleSet = RuleProfileResolver.resolve(
            foregroundPackage,
            defaultRuleSet,
            ruleProfiles,
        )

        val shouldPause = shouldPauseForForegroundApp(packageName, pausedPackages, currentConfig.appPauseMode)
        val pauseChanged = shouldPause != pausedForForegroundApp
        pausedForForegroundApp = shouldPause

        if (isPaused()) {
            clearOverlays()
        } else if (pauseChanged || oldActiveRuleSet !== activeRuleSet) {
            rebuildOverlays(activeRuleSet)
        }
    }

    private var cameraActive = false
    private var phoneCallActive = false

    fun onSystemContextChanged(
        lockScreenVisible: Boolean,
        keyboardVisible: Boolean,
        landscape: Boolean,
        fullScreen: Boolean = false,
        permissionScreen: Boolean = false,
        keyboardTopRatio: Float = 1.0f,
        cameraActive: Boolean = false,
        phoneCallActive: Boolean = false,
    ) {
        this.lockScreenVisible = lockScreenVisible
        val keyboardStateChanged = this.keyboardVisible != keyboardVisible || this.currentKeyboardTopRatio != keyboardTopRatio

        this.keyboardVisible = keyboardVisible
        this.currentKeyboardTopRatio = keyboardTopRatio
        this.landscape = landscape
        this.fullScreen = fullScreen
        this.permissionScreen = permissionScreen
        this.cameraActive = cameraActive
        this.phoneCallActive = phoneCallActive

        val shouldPause = shouldPauseForSystemContext()
        if (shouldPause == pausedForSystemContext) {
            if (!shouldPause && keyboardStateChanged) {
                rebuildOverlays(activeRuleSet)
            }
            return
        }

        pausedForSystemContext = shouldPause
        if (isPaused()) {
            clearOverlays()
        } else {
            rebuildOverlays(activeRuleSet)
        }
    }

    fun onConfigurationChanged(newConfig: Configuration) {
        landscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
        onSystemContextChanged(
            lockScreenVisible,
            keyboardVisible,
            landscape,
            fullScreen,
            permissionScreen,
            currentKeyboardTopRatio,
            cameraActive,
            phoneCallActive,
        )
        if (!isPaused()) rebuildOverlays(activeRuleSet)
    }

    private fun shouldPauseForSystemContext(): Boolean =
        SystemPausePolicy.shouldPause(
            config = currentConfig,
            lockScreenVisible = lockScreenVisible,
            keyboardVisible = keyboardVisible,
            landscape = landscape,
            fullScreen = fullScreen,
            permissionScreen = permissionScreen,
            cameraActive = cameraActive,
            phoneCallActive = phoneCallActive,
        )


    private fun isPaused(): Boolean = pausedForForegroundApp || pausedForSystemContext

    private fun shouldPauseForForegroundApp(
        packageName: String?,
        pausedPackages: Set<String>,
        mode: AppPauseMode,
    ): Boolean {
        if (currentConfig.pauseOnLauncher && packageName == launcherPackage()) return true
        return AppPausePolicy.shouldPause(packageName, pausedPackages, mode)
    }

    private fun launcherPackage(): String? = overlayManager.context.packageManager
        .resolveActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0)
        ?.activityInfo?.packageName

    private fun clearOverlays() {
        overlayManager.removeAll()
        detectors.clear()
        edgeLengths.clear()
        feedbackView = null
        lastArmed = false
        lastProgressActive = false
        lastHoldArmed = false
        suppressHaptic = true
    }

    private fun applyConfigDiff(old: GestureConfig, new: GestureConfig, ruleSet: CompiledRuleSet) {
        val sideNeedsRebuild = old.leftTriggerWidthDp != new.leftTriggerWidthDp ||
            old.rightTriggerWidthDp != new.rightTriggerWidthDp
        val bottomNeedsRebuild = old.bottomTriggerHeightDp != new.bottomTriggerHeightDp

        val behaviorNeedsRebuild =
            old.leftDamping != new.leftDamping ||
            old.rightDamping != new.rightDamping ||
            old.bottomDamping != new.bottomDamping ||
            old.leftSwipeThresholdDp != new.leftSwipeThresholdDp ||
            old.rightSwipeThresholdDp != new.rightSwipeThresholdDp ||
            old.bottomSwipeThresholdDp != new.bottomSwipeThresholdDp ||
            old.holdTimeMs != new.holdTimeMs ||
            old.holdFireMode != new.holdFireMode ||
            old.directionToleranceDegrees != new.directionToleranceDegrees ||
            old.hysteresisRatio != new.hysteresisRatio ||
            old.lSwipeThresholdDp != new.lSwipeThresholdDp ||
            old.sectionCount != new.sectionCount

        val sideRangeNeedsRebuild =
            old.leftVerticalStart != new.leftVerticalStart ||
            old.leftVerticalEnd != new.leftVerticalEnd ||
            old.rightVerticalStart != new.rightVerticalStart ||
            old.rightVerticalEnd != new.rightVerticalEnd

        val leftChanged = old.leftTriggerWidthDp != new.leftTriggerWidthDp ||
            old.leftDamping != new.leftDamping ||
            old.leftSwipeThresholdDp != new.leftSwipeThresholdDp ||
            old.leftVerticalStart != new.leftVerticalStart ||
            old.leftVerticalEnd != new.leftVerticalEnd

        val rightChanged = old.rightTriggerWidthDp != new.rightTriggerWidthDp ||
            old.rightDamping != new.rightDamping ||
            old.rightSwipeThresholdDp != new.rightSwipeThresholdDp ||
            old.rightVerticalStart != new.rightVerticalStart ||
            old.rightVerticalEnd != new.rightVerticalEnd

        val bottomChanged = old.bottomTriggerHeightDp != new.bottomTriggerHeightDp ||
            old.bottomDamping != new.bottomDamping ||
            old.bottomSwipeThresholdDp != new.bottomSwipeThresholdDp

        for (edge in Edge.entries) {
            val hasRules = ruleSet.hasRulesFor(edge) && new.isEnabled(edge)
            val hadOverlay = detectors.containsKey(edge)
            val edgeModified = when (edge) {
                Edge.LEFT -> leftChanged
                Edge.RIGHT -> rightChanged
                Edge.BOTTOM -> bottomChanged
            }
            val needsRebuild = behaviorNeedsRebuild || edgeModified

            if (hadOverlay && !hasRules) {
                removeEdge(edge)
            } else if (!hadOverlay && hasRules) {
                addEdgeOverlay(edge)
                highlightEdge(edge)
            } else if (hasRules && needsRebuild) {
                removeEdge(edge)
                addEdgeOverlay(edge)
                if (edgeModified) {
                    highlightEdge(edge)
                }
            }
        }
    }

    fun highlightEdge(edge: Edge, durationMs: Long = 2500L) {
        val tag = "sensor_${edge.name.lowercase()}"
        val window = overlayManager.getWindow(tag)
        (window?.view as? EdgeSensorView)?.triggerHighlight(durationMs)
    }

    /** Moves an existing side sensor in-place so map dragging never rebuilds overlays. */
    fun previewEdgeVerticalRange(edge: Edge, start: Float, end: Float) {
        if (edge == Edge.BOTTOM || isPaused()) return
        val window = overlayManager.getWindow("sensor_${edge.name.lowercase()}") ?: return
        val displayMetrics = overlayManager.context.resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val safeStart = start.coerceIn(0f, 1f)
        val safeEnd = end.coerceIn(safeStart + 0.20f, 1f)
        window.params.y = (safeStart * screenHeight).toInt()
        window.params.height = ((safeEnd - safeStart) * screenHeight).toInt().coerceAtLeast(1)
        edgeLengths[edge] = window.params.height.toFloat()
        overlayManager.updateWindow("sensor_${edge.name.lowercase()}")
        highlightEdge(edge, durationMs = 2_000L)
    }

    private fun rebuildOverlays(ruleSet: CompiledRuleSet) {
        clearOverlays()
        addFeedbackOverlay()
        for (edge in Edge.entries) {
            if (ruleSet.hasRulesFor(edge) && currentConfig.isEnabled(edge)) {
                addEdgeOverlay(edge)
            }
        }
    }

    private fun addFeedbackOverlay() {
        val window = OverlayWindowFactory.createFeedbackOverlay(overlayManager.context)
        feedbackView = window.view as FeedbackView
        overlayManager.addWindow("gesture_feedback", window)
    }

    private fun removeEdge(edge: Edge) {
        val tag = "sensor_${edge.name.lowercase()}"
        overlayManager.removeWindow(tag)
        detectors.remove(edge)
        edgeLengths.remove(edge)
    }

    private fun addEdgeOverlay(edge: Edge) {
        val displayMetrics = overlayManager.context.resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val screenWidth = displayMetrics.widthPixels

        val triggerSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            currentConfig.triggerSizeDpFor(edge),
            displayMetrics,
        ).toInt()

        val tag = "sensor_${edge.name.lowercase()}"

        when (edge) {
            Edge.LEFT -> {
                val (vStart, vEnd) = currentConfig.verticalRangeFor(Edge.LEFT) ?: (0f to 1f)
                val effectiveVEnd = if (currentConfig.pauseWhenKeyboardVisible && keyboardVisible) {
                    minOf(vEnd, currentKeyboardTopRatio)
                } else {
                    vEnd
                }
                val sensorHeight = ((effectiveVEnd - vStart) * screenHeight).toInt().coerceAtLeast(1)
                val verticalOffset = (vStart * screenHeight).toInt()

                val detector = createDetector(Edge.LEFT, sensorHeight.toFloat())
                val window = OverlayWindowFactory.createEdgeSensor(
                    overlayManager.context, Edge.LEFT, triggerSizePx, sensorHeight,
                    offsetPx = verticalOffset, onTouchListener = this
                )
                detectors[Edge.LEFT] = detector
                edgeLengths[Edge.LEFT] = sensorHeight.toFloat()
                overlayManager.addWindow(tag, window)
            }
            Edge.RIGHT -> {
                val (vStart, vEnd) = currentConfig.verticalRangeFor(Edge.RIGHT) ?: (0f to 1f)
                val effectiveVEnd = if (currentConfig.pauseWhenKeyboardVisible && keyboardVisible) {
                    minOf(vEnd, currentKeyboardTopRatio)
                } else {
                    vEnd
                }
                val sensorHeight = ((effectiveVEnd - vStart) * screenHeight).toInt().coerceAtLeast(1)
                val verticalOffset = (vStart * screenHeight).toInt()

                val detector = createDetector(Edge.RIGHT, sensorHeight.toFloat())
                val window = OverlayWindowFactory.createEdgeSensor(
                    overlayManager.context, Edge.RIGHT, triggerSizePx, sensorHeight,
                    offsetPx = verticalOffset, onTouchListener = this
                )
                detectors[Edge.RIGHT] = detector
                edgeLengths[Edge.RIGHT] = sensorHeight.toFloat()
                overlayManager.addWindow(tag, window)
            }
            Edge.BOTTOM -> {
                val detector = createDetector(Edge.BOTTOM, screenWidth.toFloat())
                val window = OverlayWindowFactory.createEdgeSensor(
                    overlayManager.context, Edge.BOTTOM, screenWidth, triggerSizePx,
                    onTouchListener = this
                )
                detectors[Edge.BOTTOM] = detector
                edgeLengths[Edge.BOTTOM] = screenWidth.toFloat()
                overlayManager.addWindow(tag, window)
            }
        }
    }

    private fun createDetector(edge: Edge, sensorLength: Float): EdgeGestureDetector {
        val displayMetrics = overlayManager.context.resources.displayMetrics
        val perEdgeThresholdPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            currentConfig.swipeThresholdDpFor(edge),
            displayMetrics,
        )
        val lSwipeThresholdPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            currentConfig.lSwipeThresholdDp,
            displayMetrics,
        )
        val configCopy = currentConfig.copy(
            sensorLength = sensorLength,
        )
        val touchSlop = ViewConfiguration.get(overlayManager.context).scaledTouchSlop
        val edgeTriggerMode = activeRuleSet.triggerModeFor(edge)

        return EdgeGestureDetector(
            edge = edge,
            config = configCopy,
            scaledTouchSlop = touchSlop,
            swipeThresholdPx = perEdgeThresholdPx,
            lSwipeThresholdPx = lSwipeThresholdPx,
            onGestureResult = { result -> handleGestureResult(result) },
            triggerMode = edgeTriggerMode,
            onReplayTap = if (edgeTriggerMode == TriggerMode.SWIPE) { x, y ->
                GestureAccessibilityService.instance?.dispatchTap(x, y)
            } else null,
            onProgress = ::handleGestureProgress,
            hasHoldActionAt = { touchPx ->
                if (sensorLength <= 0f) false
                else activeRuleSet.match(edge, GestureType.SWIPE_HOLD, (touchPx / sensorLength).coerceIn(0f, 1f)) != null
            },
            hasLActionAt = { touchPx ->
                if (sensorLength <= 0f) false
                else {
                    val ratio = (touchPx / sensorLength).coerceIn(0f, 1f)
                    activeRuleSet.match(edge, GestureType.SWIPE_UP_L, ratio) != null ||
                        activeRuleSet.match(edge, GestureType.SWIPE_DOWN_L, ratio) != null
                }
            },
            hasRingActions = { currentConfig.hasRingActionsFor(edge) },
            onRingActionSelected = { index ->
                currentConfig.ringActionsFor(edge).getOrNull(index)?.let { action ->
                    handleRingAction(edge, index, action)
                }
            },
            ringHitTest = { x, y, touchAlongEdge ->
                ringHitTest(edge, x, y, touchAlongEdge, displayMetrics)
            },
            ringHoverTest = { x, y, touchAlongEdge ->
                ringHitTest(edge, x, y, touchAlongEdge, displayMetrics, hitScale = 2.2f)
            },
        )
    }

    private fun ringHitTest(
        edge: Edge,
        x: Float,
        y: Float,
        touchAlongEdge: Float,
        metrics: android.util.DisplayMetrics,
        hitScale: Float = 1.3f,
    ): Int {
        val density = metrics.density
        val width = metrics.widthPixels.toFloat()
        val height = metrics.heightPixels.toFloat()
        val span = if (edge == Edge.BOTTOM) height else width
        val radius = currentConfig.ringSizeDp * currentConfig.iconSize
        val maxInset = if (edge == Edge.BOTTOM) span * 0.5f else span * 0.9f
        val inset = (currentConfig.ringGroupInsetDp * density)
            .coerceIn(radius * 1.2f, maxInset)
        val spread = (currentConfig.ringGroupSpacingDp * density).coerceAtLeast(36f)
        val anchor = when (edge) {
            Edge.LEFT -> inset
            Edge.RIGHT -> width - inset
            Edge.BOTTOM -> height - inset
        }
        val middleLead = 72f * currentConfig.iconSize
        val sideLead = middleLead * 0.52f
        val middle = when (edge) {
            Edge.LEFT -> anchor + middleLead
            Edge.RIGHT, Edge.BOTTOM -> anchor - middleLead
        }
        val side = listOf(
            (touchAlongEdge - spread).coerceIn(radius, if (edge == Edge.BOTTOM) width - radius else height - radius),
            touchAlongEdge.coerceIn(radius, if (edge == Edge.BOTTOM) width - radius else height - radius),
            (touchAlongEdge + spread).coerceIn(radius, if (edge == Edge.BOTTOM) width - radius else height - radius),
        )
        val centers = when (edge) {
            Edge.LEFT, Edge.RIGHT -> listOf(
                (if (edge == Edge.LEFT) anchor + sideLead else anchor - sideLead) to side[0],
                middle to side[1],
                (if (edge == Edge.LEFT) anchor + sideLead else anchor - sideLead) to side[2],
            )
            Edge.BOTTOM -> listOf(
                side[0] to (anchor - sideLead),
                side[1] to middle,
                side[2] to (anchor - sideLead),
            )
        }
        // Treat contact with any visible part of the bubble as a hit. The
        // extra margin also covers the selected bubble's animated growth.
        val hitRadius = radius * hitScale
        val hitRadiusSquared = hitRadius * hitRadius
        val nearest = centers.mapIndexed { index, (cx, cy) ->
            val dx = x - cx
            val dy = y - cy
            index to (dx * dx + dy * dy)
        }.minByOrNull { it.second }
        val hit = if (nearest != null && nearest.second <= hitRadiusSquared) nearest.first else -1
        RuntimeDiagnostics.ringHitProbe(edge.name, hit, x, y, touchAlongEdge)
        return hit
    }

    private fun handleGestureProgress(progress: GestureProgress) {
        val view = feedbackView ?: return
        view.peakThreshold = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            currentConfig.swipeThresholdDpFor(progress.edge),
            overlayManager.context.resources.displayMetrics,
        )

        val effectivePrimaryColor = if (currentConfig.useAppAdaptiveColor && adaptiveAppColor != null) {
            blendColor(currentConfig.feedbackColorArgb, adaptiveAppColor!!, 0.72f)
        } else {
            currentConfig.feedbackColorArgb
        }

        view.primaryColor = effectivePrimaryColor
        view.secondaryColor = currentConfig.secondaryColorArgb
        view.lSwipeColor = currentConfig.lSwipeColorArgb
        view.feedbackOpacity = currentConfig.feedbackOpacity
        view.feedbackAnimation = currentConfig.feedbackAnimation
        view.animationSpeed = currentConfig.animationSpeed
        view.animationSize = currentConfig.animationSize
        view.iconSize = currentConfig.iconSize
        view.showIndicatorBar = currentConfig.showGestureIndicatorBar
        view.ringGroupInsetDp = currentConfig.ringGroupInsetDp
        view.ringGroupSpacingDp = currentConfig.ringGroupSpacingDp
        view.ringSizeDp = currentConfig.ringSizeDp
        view.ringSymbols = if (currentConfig.hasRingActionsFor(progress.edge)) {
            currentConfig.ringActionsFor(progress.edge).map { action ->
                ActionSymbols.symbolFor(action, currentConfig.actionIconPack)
            }
        } else {
            emptyList()
        }
        view.ringSelectedIndex = progress.ringSelectedIndex

        val matchedAction = if (progress.active) {
            val sensorLen = edgeLengths[progress.edge] ?: 0f
            val ratio = if (sensorLen > 0f) (progress.touchAlongEdgePx / sensorLen).coerceIn(0f, 1f) else 0f
            val gestureType = when {
                progress.isLUp -> GestureType.SWIPE_UP_L
                progress.isLDown -> GestureType.SWIPE_DOWN_L
                progress.lPreviewGesture != null -> progress.lPreviewGesture
                progress.holdArmed -> GestureType.SWIPE_HOLD
                else -> GestureType.QUICK_SWIPE
            }
            activeRuleSet.match(edge = progress.edge, gestureType = gestureType, sectionRatio = ratio)
        } else null

        // Keep the selected action visible through the release animation. Clearing
        // it on ACTION_UP makes the renderer fall back to intermediate icons.
        if (progress.active) {
            // The L guide is its own visual cue. Do not place the action's
            // fallback "L" glyph inside the trigger bubble while previewing.
            view.actionSymbol = if (progress.lPreviewGesture != null) {
                ""
            } else {
                ActionSymbols.symbolFor(matchedAction, currentConfig.actionIconPack)
            }
        }
        view.updateGestureState(
            edge = progress.edge,
            stretch = progress.stretch,
            touchPos = progress.touchAlongEdgePx,
            active = progress.active,
            armed = progress.armed,
            holdArmed = progress.holdArmed,
            appSwitchDirection = progress.appSwitchDirection,
            isLUp = progress.isLUp,
            isLDown = progress.isLDown,
            bendStartY = progress.bendStartY,
            lColorProgress = progress.lColorProgress,
            lPreviewGesture = progress.lPreviewGesture,
            ringActive = progress.ringActive,
        )

        // Haptic and sound execution
        HapticHelper.intensity = currentConfig.hapticIntensity
        HapticHelper.enabled = currentConfig.hapticEnabled
        HapticHelper.soundEnabled = currentConfig.hapticSoundEnabled

        // Clear suppress flag on a genuine new touch start (was idle → now active).
        if (progress.active && !lastProgressActive && suppressHaptic) {
            suppressHaptic = false
        }

        // When touch finishes, immediately cancel any active vibration pulse.
        if (!progress.active && lastProgressActive) {
            HapticHelper.cancel(overlayManager.context)
        }

        val now = System.currentTimeMillis()
        if (!suppressHaptic && (now - lastHapticMs >= HAPTIC_MIN_INTERVAL_MS)) {
            // Only trigger haptic on actual gesture detection (armed state), not on initial touch down
            // This prevents unwanted vibration when accidentally touching the trigger zone
            if (progress.armed && !lastArmed) {
                HapticHelper.performHaptic(view, HapticHelper.HapticType.MEDIUM)
                lastHapticMs = now
            } else if (progress.holdArmed && !lastHoldArmed) {
                HapticHelper.performHaptic(view, HapticHelper.HapticType.HEAVY)
                lastHapticMs = now
            } else if (lastHoldArmed && !progress.holdArmed && progress.active) {
                HapticHelper.performHaptic(view, HapticHelper.HapticType.LIGHT)
                lastHapticMs = now
            }
        }

        lastArmed = progress.armed
        lastHoldArmed = progress.holdArmed
        lastProgressActive = progress.active
    }


    private fun blendColor(base: Int, overlay: Int, amount: Float): Int {
        val t = amount.coerceIn(0f, 1f)
        fun channel(shift: Int): Int {
            val from = base shr shift and 0xFF
            val to = overlay shr shift and 0xFF
            return (from + (to - from) * t).toInt().coerceIn(0, 255)
        }
        return android.graphics.Color.argb(
            channel(24), channel(16), channel(8), channel(0),
        )
    }

    private fun handleGestureResult(result: GestureResult) {
        if (result is GestureResult.BottomHorizontalSwipe) {
            val action = when (result.direction) {
                io.github.omeryol.akisgesture.gesture.model.SwipeDirection.RIGHT -> ActionNode.SwitchLastApp
                io.github.omeryol.akisgesture.gesture.model.SwipeDirection.LEFT -> ActionNode.SwitchNextApp
                else -> return
            }
            RuntimeDiagnostics.gestureMatched(
                edge = Edge.BOTTOM.name,
                gesture = "BOTTOM_HORIZONTAL_${result.direction.name}",
                actionId = action.id,
            )
            performResultHapticIfNeeded()
            scope.launch {
                actionDispatcher.dispatch(action)
            }
            return
        }

        val actionNode = matchViaRuleSet(result)
        RuntimeDiagnostics.gestureMatched(
            edge = result.edgeName(),
            gesture = result.gestureName(),
            actionId = actionNode?.id,
        )
        if (actionNode == null) return

        // Record real runtime gesture usage stats
        val (edge, gestureType, _) = when (result) {
            is GestureResult.EdgeSwipe -> Triple(result.edge, result.gestureType, result.touchAlongEdgePx)
            is GestureResult.VerticalSwipe -> Triple(result.edge, GestureType.QUICK_SWIPE, result.touchAlongEdgePx)
            is GestureResult.Tap -> Triple(result.edge, GestureType.QUICK_SWIPE, result.touchAlongEdgePx)
            else -> Triple(Edge.BOTTOM, GestureType.QUICK_SWIPE, 0f)
        }
        io.github.omeryol.akisgesture.util.GestureTracker.recordGesture(overlayManager.context, edge, gestureType)

        performResultHapticIfNeeded()

        scope.launch {
            actionDispatcher.dispatch(actionNode)
        }
    }

    private fun handleRingAction(edge: Edge, index: Int, action: ActionNode) {
        RuntimeDiagnostics.ringAction(edge.name, index, action.id)
        RuntimeDiagnostics.gestureMatched(edge.name, "RING_$index", action.id)
        io.github.omeryol.akisgesture.util.GestureTracker.recordGesture(
            overlayManager.context,
            edge,
            GestureType.SWIPE_HOLD,
        )
        performResultHapticIfNeeded()
        scope.launch { actionDispatcher.dispatch(action) }
    }

    private fun GestureResult.edgeName(): String = when (this) {
        is GestureResult.EdgeSwipe -> edge.name
        is GestureResult.VerticalSwipe -> edge.name
        is GestureResult.Tap -> edge.name
        is GestureResult.BottomHorizontalSwipe -> Edge.BOTTOM.name
    }

    private fun GestureResult.gestureName(): String = when (this) {
        is GestureResult.EdgeSwipe -> gestureType.name
        is GestureResult.VerticalSwipe -> "VERTICAL_SWIPE"
        is GestureResult.Tap -> "TAP"
        is GestureResult.BottomHorizontalSwipe -> "BOTTOM_HORIZONTAL_${direction.name}"
    }

    private fun performResultHapticIfNeeded() {
        if (lastArmed) return
        HapticHelper.enabled = currentConfig.hapticEnabled
        HapticHelper.intensity = currentConfig.hapticIntensity
        HapticHelper.soundEnabled = currentConfig.hapticSoundEnabled
        feedbackView?.let { HapticHelper.performHaptic(it, HapticHelper.HapticType.MEDIUM) }
            ?: HapticHelper.performHaptic(overlayManager.context, HapticHelper.HapticType.MEDIUM)
    }

    private fun matchViaRuleSet(result: GestureResult): ActionNode? {
        val (edge, gestureType, touchPx) = when (result) {
            is GestureResult.EdgeSwipe -> Triple(result.edge, result.gestureType, result.touchAlongEdgePx)
            is GestureResult.VerticalSwipe -> Triple(result.edge, GestureType.QUICK_SWIPE, result.touchAlongEdgePx)
            is GestureResult.Tap -> Triple(result.edge, GestureType.QUICK_SWIPE, result.touchAlongEdgePx)
            is GestureResult.BottomHorizontalSwipe -> return null
        }

        val compiledRuleSet = activeRuleSet
        val edgeLength = edgeLengths[edge] ?: return null
        if (edgeLength <= 0f) return null

        val sectionRatio = (touchPx / edgeLength).coerceIn(0f, 1f)
        return compiledRuleSet.match(edge, gestureType, sectionRatio)
    }

    override fun onEdgeTouch(edge: Edge, event: MotionEvent): Boolean {
        if (isPaused()) return false
        return detectors[edge]?.onTouchEvent(event) ?: false
    }

    private data class RuntimeInputs(
        val config: GestureConfig,
        val defaultRuleSet: CompiledRuleSet,
        val pausedPackages: Set<String>,
        val profiles: Map<String, CompiledRuleSet>,
    )
}
