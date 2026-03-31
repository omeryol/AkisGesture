package com.openswipe.gesture

import android.content.res.Configuration
import android.content.res.Resources
import android.util.TypedValue
import android.view.MotionEvent
import android.view.ViewConfiguration
import com.openswipe.action.ActionDispatcher
import com.openswipe.action.ActionType
import com.openswipe.gesture.model.GestureResult
import com.openswipe.model.ActionNode
import com.openswipe.model.GestureType
import com.openswipe.overlay.Edge
import com.openswipe.overlay.EdgeSensorView
import com.openswipe.overlay.OverlayManager
import com.openswipe.overlay.OverlayWindowFactory
import com.openswipe.rule.CompiledRuleSet
import com.openswipe.service.GestureAccessibilityService
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
) : EdgeSensorView.OnEdgeTouchListener {

    private var scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val detectors = mutableMapOf<Edge, EdgeGestureDetector>()
    private var currentConfig: GestureConfig = configFlow.value
    private var started = false

    fun stop() {
        overlayManager.removeAll()
        detectors.clear()
        edgeLengths.clear()
        scope.cancel()
        started = false
    }

    fun start() {
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        scope.launch {
            combine(configFlow, compiledRuleSetFlow) { config, ruleSet -> config to ruleSet }
                .collect { (newConfig, ruleSet) ->
                    val old = currentConfig
                    currentConfig = newConfig
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
        // Phase 2: 黑名单/白名单检测
    }

    fun onConfigurationChanged(newConfig: Configuration) {
        rebuildOverlays(compiledRuleSetFlow.value)
    }

    private fun applyConfigDiff(old: GestureConfig, new: GestureConfig, ruleSet: CompiledRuleSet) {
        // If edge width changed, rebuild all side overlays
        val sideNeedsRebuild = old.edgeTriggerWidthDp != new.edgeTriggerWidthDp

        // If bottom height or trigger mode changed, rebuild bottom overlay
        val bottomNeedsRebuild = old.bottomTriggerHeightDp != new.bottomTriggerHeightDp ||
                old.bottomTriggerMode != new.bottomTriggerMode

        for (edge in Edge.entries) {
            val hasRules = ruleSet.hasRulesFor(edge)
            val hadOverlay = detectors.containsKey(edge)
            val needsRebuild = when (edge) {
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
        for (edge in Edge.entries) {
            if (ruleSet.hasRulesFor(edge)) {
                addEdgeOverlay(edge)
            }
        }
    }

    private fun removeEdge(edge: Edge) {
        val tag = "sensor_${edge.name.lowercase()}"
        overlayManager.removeWindow(tag)
        detectors.remove(edge)
        edgeLengths.remove(edge)
    }

    private fun addEdgeOverlay(edge: Edge) {
        val resources = Resources.getSystem()
        val displayMetrics = resources.displayMetrics
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
        return EdgeGestureDetector(
            edge = edge,
            config = configCopy,
            scaledTouchSlop = touchSlop,
            onGestureResult = { result -> handleGestureResult(result) },
            triggerMode = if (edge == Edge.BOTTOM) currentConfig.bottomTriggerMode else BottomTriggerMode.TOUCH,
            onReplayTap = if (edge == Edge.BOTTOM) { x, y ->
                GestureAccessibilityService.getInstance()?.dispatchTap(x, y)
            } else null,
        )
    }

    /** Map of edge → current sensor length in pixels, updated when overlays are created. */
    private val edgeLengths = mutableMapOf<Edge, Float>()

    private fun handleGestureResult(result: GestureResult) {
        val actionNode = matchViaRuleSet(result) ?: return
        scope.launch { actionDispatcher.dispatch(actionNode) }
    }

    private fun matchViaRuleSet(result: GestureResult): ActionNode? {
        val (edge, gestureType, touchPx) = when (result) {
            is GestureResult.EdgeSwipe -> Triple(
                result.edge,
                GestureType.SWIPE,
                result.touchAlongEdgePx
            )
            is GestureResult.VerticalSwipe -> Triple(
                result.edge,
                GestureType.SWIPE,
                result.touchAlongEdgePx
            )
            is GestureResult.Tap -> Triple(
                result.edge,
                GestureType.SWIPE,
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
        return detectors[edge]?.onTouchEvent(event) ?: false
    }
}
