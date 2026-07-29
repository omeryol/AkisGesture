package com.omer.akisgesture.gesture

import android.content.res.Configuration
import android.util.TypedValue
import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration
import com.omer.akisgesture.action.ActionDispatcher
import com.omer.akisgesture.action.ActionType
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
import com.omer.akisgesture.service.GestureAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class GestureEngine(
    private val configFlow: StateFlow<GestureConfig>,
    private val actionDispatcher: ActionDispatcher,
    private val overlayManager: OverlayManager,
    private val compiledRuleSetFlow: StateFlow<CompiledRuleSet>,
    private val pausedPackagesFlow: StateFlow<Set<String>>,
) : EdgeSensorView.OnEdgeTouchListener {

    private var scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val detectors = mutableMapOf<Edge, EdgeGestureDetector>()
    private var currentConfig: GestureConfig = configFlow.value
    private var started = false
    private var feedbackView: FeedbackView? = null
    private var lastArmed = false
    private var lastProgressActive = false
    private var lastHoldArmed = false
    private var foregroundPackage: String? = null
    private var pausedPackages: Set<String> = pausedPackagesFlow.value
    private var pausedForForegroundApp = false

    fun stop() {
        overlayManager.removeAll()
        detectors.clear()
        edgeLengths.clear()
        feedbackView = null
        lastArmed = false
        lastProgressActive = false
        lastHoldArmed = false
        scope.cancel()
        started = false
    }

    fun start() {
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        scope.launch {
            combine(
                configFlow,
                compiledRuleSetFlow,
                pausedPackagesFlow,
            ) { config, ruleSet, packages -> Triple(config, ruleSet, packages) }
                .collect { (newConfig, ruleSet, packages) ->
                    val old = currentConfig
                    currentConfig = newConfig
                    pausedPackages = packages
                    val shouldPause = AppPausePolicy.shouldPause(foregroundPackage, packages)
                    if (shouldPause) {
                        pausedForForegroundApp = true
                        overlayManager.removeAll()
                        detectors.clear()
                        edgeLengths.clear()
                        feedbackView = null
                        started = true
                        return@collect
                    }
                    if (pausedForForegroundApp) {
                        pausedForForegroundApp = false
                        started = true
                        rebuildOverlays(ruleSet)
                        return@collect
                    }
                    if (!started) {
                        started = true
                        rebuildOverlays(ruleSet)
                    } else {
                        applyConfigDiff(old, newConfig, ruleSet)
                    }
                }
        }
    }

    fun onForegroundAppChanged(packageName: String) {
        foregroundPackage = packageName
        val shouldPause = AppPausePolicy.shouldPause(packageName, pausedPackages)
        if (shouldPause == pausedForForegroundApp) return
        pausedForForegroundApp = shouldPause
        if (shouldPause) {
            overlayManager.removeAll()
            detectors.clear()
            edgeLengths.clear()
            feedbackView = null
        } else {
            rebuildOverlays(compiledRuleSetFlow.value)
        }
    }

    fun onConfigurationChanged(newConfig: Configuration) {
        rebuildOverlays(compiledRuleSetFlow.value)
    }

    private fun applyConfigDiff(old: GestureConfig, new: GestureConfig, ruleSet: CompiledRuleSet) {
        // If edge width changed, rebuild all side overlays
        val sideNeedsRebuild = old.edgeTriggerWidthDp != new.edgeTriggerWidthDp

        // If bottom height changed, rebuild bottom overlay
        val bottomNeedsRebuild = old.bottomTriggerHeightDp != new.bottomTriggerHeightDp
        val behaviorNeedsRebuild =
            old.dampingFactor != new.dampingFactor ||
            old.minSwipeThresholdPx != new.minSwipeThresholdPx ||
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
        overlayManager.removeAll()
        detectors.clear()
        edgeLengths.clear()
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

        val edgeWidthPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, currentConfig.edgeTriggerWidthDp, displayMetrics
        ).toInt()

        val bottomHeightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, currentConfig.bottomTriggerHeightDp, displayMetrics
        ).toInt()

        val tag = "sensor_${edge.name.lowercase()}"

        when (edge) {
            Edge.LEFT -> {
                val detector = createDetector(Edge.LEFT, screenHeight.toFloat())
                val window = OverlayWindowFactory.createEdgeSensor(
                    overlayManager.context, Edge.LEFT, edgeWidthPx, screenHeight,
                    onTouchListener = this
                )
                detectors[Edge.LEFT] = detector
                edgeLengths[Edge.LEFT] = screenHeight.toFloat()
                overlayManager.addWindow(tag, window)
            }
            Edge.RIGHT -> {
                val detector = createDetector(Edge.RIGHT, screenHeight.toFloat())
                val window = OverlayWindowFactory.createEdgeSensor(
                    overlayManager.context, Edge.RIGHT, edgeWidthPx, screenHeight,
                    onTouchListener = this
                )
                detectors[Edge.RIGHT] = detector
                edgeLengths[Edge.RIGHT] = screenHeight.toFloat()
                overlayManager.addWindow(tag, window)
            }
            Edge.BOTTOM -> {
                val detector = createDetector(Edge.BOTTOM, screenWidth.toFloat())
                val window = OverlayWindowFactory.createEdgeSensor(
                    overlayManager.context, Edge.BOTTOM, screenWidth, bottomHeightPx,
                    onTouchListener = this
                )
                detectors[Edge.BOTTOM] = detector
                edgeLengths[Edge.BOTTOM] = screenWidth.toFloat()
                overlayManager.addWindow(tag, window)
            }
        }
    }

    private fun createDetector(edge: Edge, sensorLength: Float): EdgeGestureDetector {
        val configCopy = currentConfig.copy(sensorLength = sensorLength)
        val touchSlop = ViewConfiguration.get(overlayManager.context).scaledTouchSlop
        val edgeTriggerMode = compiledRuleSetFlow.value.triggerModeFor(edge)
        return EdgeGestureDetector(
            edge = edge,
            config = configCopy,
            scaledTouchSlop = touchSlop,
            onGestureResult = { result -> handleGestureResult(result) },
            triggerMode = edgeTriggerMode,
            onReplayTap = if (edgeTriggerMode == TriggerMode.SWIPE) { x, y ->
                GestureAccessibilityService.getInstance()?.dispatchTap(x, y)
            } else null,
            onProgress = ::handleGestureProgress,
            hasHoldActionAt = { touchAlongEdgePx ->
                if (sensorLength <= 0f) {
                    false
                } else {
                    compiledRuleSetFlow.value.match(
                        edge = edge,
                        gestureType = GestureType.SWIPE_HOLD,
                        sectionRatio = (touchAlongEdgePx / sensorLength).coerceIn(0f, 1f),
                    ) != null
                }
            },
        )
    }

    private fun handleGestureProgress(progress: GestureProgress) {
        val view = feedbackView ?: return
        view.peakThreshold = currentConfig.minSwipeThresholdPx
        view.feedbackColor = currentConfig.feedbackColorArgb
        view.feedbackOpacity = currentConfig.feedbackOpacity
        view.feedbackAnimation = currentConfig.feedbackAnimation
        view.quickIcon = currentConfig.quickFeedbackIcon
        view.holdIcon = currentConfig.holdFeedbackIcon
        view.updateGestureState(
            edge = progress.edge,
            stretch = progress.stretch,
            touchPos = progress.touchAlongEdgePx,
            active = progress.active,
            armed = progress.armed,
            holdArmed = progress.holdArmed,
        )

        if (currentConfig.hapticEnabled) {
            if (progress.active && !lastProgressActive) {
                HapticHelper.performHaptic(view, HapticHelper.HapticType.LIGHT)
            } else if (progress.armed && !lastArmed) {
                HapticHelper.performHaptic(view, HapticHelper.HapticType.MEDIUM)
            } else if (progress.holdArmed && !lastHoldArmed) {
                HapticHelper.performHaptic(view, HapticHelper.HapticType.HEAVY)
            } else if (!progress.active && lastArmed) {
                HapticHelper.performHaptic(view, HapticHelper.HapticType.MEDIUM)
            }
        }
        lastArmed = progress.armed
        lastHoldArmed = progress.holdArmed
        lastProgressActive = progress.active
    }

    /** Map of edge → current sensor length in pixels, updated when overlays are created. */
    private val edgeLengths = mutableMapOf<Edge, Float>()

    private fun handleGestureResult(result: GestureResult) {
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
            is GestureResult.EdgeSwipe -> Triple(
                result.edge,
                result.gestureType,
                result.touchAlongEdgePx
            )
            is GestureResult.VerticalSwipe -> Triple(
                result.edge,
                GestureType.QUICK_SWIPE,
                result.touchAlongEdgePx
            )
            is GestureResult.Tap -> Triple(
                result.edge,
                GestureType.QUICK_SWIPE,
                result.touchAlongEdgePx
            )
        }
        val compiledRuleSet = compiledRuleSetFlow.value
        val edgeLength = edgeLengths[edge] ?: return null
        if (edgeLength <= 0f) return null
        val sectionRatio = (touchPx / edgeLength).coerceIn(0f, 1f)
        return compiledRuleSet.match(edge, gestureType, sectionRatio)
    }

    override fun onEdgeTouch(edge: Edge, event: MotionEvent): Boolean {
        if (pausedForForegroundApp) return false
        return detectors[edge]?.onTouchEvent(event) ?: false
    }
}
