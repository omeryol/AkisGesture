package com.omer.akisgesture.gesture

import android.content.res.Configuration
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.ViewConfiguration
import com.omer.akisgesture.action.ActionDispatcher
import com.omer.akisgesture.feedback.ActionSymbols
import com.omer.akisgesture.feedback.FeedbackView
import com.omer.akisgesture.feedback.HapticHelper
import com.omer.akisgesture.gesture.model.GestureResult
import com.omer.akisgesture.model.ActionNode
import com.omer.akisgesture.model.GestureType
import com.omer.akisgesture.model.TriggerMode
import com.omer.akisgesture.overlay.Edge
import com.omer.akisgesture.overlay.EdgeSensorView
import com.omer.akisgesture.overlay.OverlayManager
import com.omer.akisgesture.overlay.OverlayWindowFactory
import com.omer.akisgesture.rule.CompiledRuleSet
import com.omer.akisgesture.rule.RuleProfileResolver
import com.omer.akisgesture.service.GestureAccessibilityService
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

                    pausedForForegroundApp = AppPausePolicy.shouldPause(foregroundPackage, inputs.pausedPackages)
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

        val shouldPause = AppPausePolicy.shouldPause(packageName, pausedPackages)
        val pauseChanged = shouldPause != pausedForForegroundApp
        pausedForForegroundApp = shouldPause

        if (isPaused()) {
            clearOverlays()
        } else if (pauseChanged || oldActiveRuleSet !== activeRuleSet) {
            rebuildOverlays(activeRuleSet)
        }
    }

    fun onSystemContextChanged(
        lockScreenVisible: Boolean,
        keyboardVisible: Boolean,
        landscape: Boolean,
        fullScreen: Boolean = false,
        permissionScreen: Boolean = false,
        keyboardTopRatio: Float = 1.0f,
    ) {
        this.lockScreenVisible = lockScreenVisible
        val keyboardStateChanged = this.keyboardVisible != keyboardVisible || this.currentKeyboardTopRatio != keyboardTopRatio

        this.keyboardVisible = keyboardVisible
        this.currentKeyboardTopRatio = keyboardTopRatio
        this.landscape = landscape
        this.fullScreen = fullScreen
        this.permissionScreen = permissionScreen

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
        )
        if (!isPaused()) rebuildOverlays(activeRuleSet)
    }

    private fun shouldPauseForSystemContext(): Boolean =
        SystemPausePolicy.shouldPause(
            config = currentConfig,
            lockScreenVisible = lockScreenVisible,
            keyboardVisible = false, // Handled dynamically via keyboardTopRatio clipping
            landscape = landscape,
            fullScreen = fullScreen,
            permissionScreen = permissionScreen,
        )

    private fun isPaused(): Boolean = pausedForForegroundApp || pausedForSystemContext

    private fun clearOverlays() {
        overlayManager.removeAll()
        detectors.clear()
        edgeLengths.clear()
        feedbackView = null
        lastArmed = false
        lastProgressActive = false
        lastHoldArmed = false
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
            old.sectionCount != new.sectionCount

        for (edge in Edge.entries) {
            val hasRules = ruleSet.hasRulesFor(edge)
            val hadOverlay = detectors.containsKey(edge)
            val needsRebuild = behaviorNeedsRebuild || when (edge) {
                Edge.LEFT, Edge.RIGHT -> sideNeedsRebuild
                Edge.BOTTOM -> bottomNeedsRebuild
            }

            if (hadOverlay && !hasRules) {
                removeEdge(edge)
            } else if (!hadOverlay && hasRules) {
                addEdgeOverlay(edge)
            } else if (hasRules && needsRebuild) {
                removeEdge(edge)
                addEdgeOverlay(edge)
            }
        }
    }

    private fun rebuildOverlays(ruleSet: CompiledRuleSet) {
        clearOverlays()
        addFeedbackOverlay()
        for (edge in Edge.entries) {
            if (ruleSet.hasRulesFor(edge)) {
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
                val effectiveVEnd = if (keyboardVisible) minOf(vEnd, currentKeyboardTopRatio) else vEnd
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
                val effectiveVEnd = if (keyboardVisible) minOf(vEnd, currentKeyboardTopRatio) else vEnd
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
        val configCopy = currentConfig.copy(
            sensorLength = sensorLength,
            dampingFactor = currentConfig.dampingFor(edge),
            minSwipeThresholdPx = perEdgeThresholdPx,
        )
        val touchSlop = ViewConfiguration.get(overlayManager.context).scaledTouchSlop
        val edgeTriggerMode = activeRuleSet.triggerModeFor(edge)

        return EdgeGestureDetector(
            edge = edge,
            config = configCopy,
            scaledTouchSlop = touchSlop,
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
        )
    }

    private fun handleGestureProgress(progress: GestureProgress) {
        val view = feedbackView ?: return
        @Suppress("DEPRECATION")
        view.peakThreshold = currentConfig.minSwipeThresholdPx

        val effectiveColor = if (currentConfig.useAppAdaptiveColor && adaptiveAppColor != null) {
            adaptiveAppColor!!
        } else {
            currentConfig.feedbackColorArgb
        }

        view.feedbackColor = effectiveColor
        view.feedbackOpacity = currentConfig.feedbackOpacity
        view.feedbackAnimation = currentConfig.feedbackAnimation
        view.animationSpeed = currentConfig.animationSpeed
        view.animationSize = currentConfig.animationSize
        view.showIndicatorBar = currentConfig.showGestureIndicatorBar

        val matchedAction = if (progress.active) {
            val sensorLen = edgeLengths[progress.edge] ?: 0f
            val ratio = if (sensorLen > 0f) (progress.touchAlongEdgePx / sensorLen).coerceIn(0f, 1f) else 0f
            val gestureType = if (progress.holdArmed) GestureType.SWIPE_HOLD else GestureType.QUICK_SWIPE
            activeRuleSet.match(edge = progress.edge, gestureType = gestureType, sectionRatio = ratio)
        } else null

        view.actionSymbol = ActionSymbols.symbolFor(matchedAction)
        view.updateGestureState(
            edge = progress.edge,
            stretch = progress.stretch,
            touchPos = progress.touchAlongEdgePx,
            active = progress.active,
            armed = progress.armed,
            holdArmed = progress.holdArmed,
            appSwitchDirection = progress.appSwitchDirection,
        )

        // Haptic and sound execution
        HapticHelper.intensity = currentConfig.hapticIntensity
        HapticHelper.soundEnabled = currentConfig.hapticSoundEnabled

        if (progress.active && !lastProgressActive) {
            HapticHelper.performHaptic(view, HapticHelper.HapticType.LIGHT)
        } else if (progress.armed && !lastArmed) {
            HapticHelper.performHaptic(view, HapticHelper.HapticType.MEDIUM)
        } else if (progress.holdArmed && !lastHoldArmed) {
            HapticHelper.performHaptic(view, HapticHelper.HapticType.HEAVY)
        } else if (!progress.active && lastArmed) {
            HapticHelper.performHaptic(view, HapticHelper.HapticType.MEDIUM)
        }

        lastArmed = progress.armed
        lastHoldArmed = progress.holdArmed
        lastProgressActive = progress.active
    }

    private fun handleGestureResult(result: GestureResult) {
        if (result is GestureResult.BottomHorizontalSwipe) {
            val action = when (result.direction) {
                com.omer.akisgesture.gesture.model.SwipeDirection.RIGHT -> ActionNode.SwitchLastApp
                com.omer.akisgesture.gesture.model.SwipeDirection.LEFT -> ActionNode.SwitchNextApp
                else -> return
            }
            scope.launch {
                val dispatchResult = actionDispatcher.dispatch(action)
                Log.d("AkisGesture", "bottom_app_switch direction=${result.direction} result=$dispatchResult")
            }
            return
        }

        val actionNode = matchViaRuleSet(result)
        Log.d("AkisGesture", "matched_result result=$result action=$actionNode")
        if (actionNode == null) return

        scope.launch {
            val dispatchResult = actionDispatcher.dispatch(actionNode)
            Log.d("AkisGesture", "dispatch_result action=$actionNode result=$dispatchResult")
        }
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
