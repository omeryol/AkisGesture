package com.openswipe.gesture

import android.content.res.Configuration
import android.content.res.Resources
import android.util.TypedValue
import android.view.MotionEvent
import android.view.ViewConfiguration
import com.openswipe.action.ActionDispatcher
import com.openswipe.action.ActionType
import com.openswipe.gesture.model.GestureResult
import com.openswipe.overlay.Edge
import com.openswipe.overlay.EdgeSensorView
import com.openswipe.overlay.OverlayManager
import com.openswipe.overlay.OverlayWindowFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GestureEngine(
    private val configFlow: StateFlow<GestureConfig>,
    private val actionDispatcher: ActionDispatcher,
    private val overlayManager: OverlayManager,
) : EdgeSensorView.OnEdgeTouchListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val detectors = mutableMapOf<Edge, EdgeGestureDetector>()
    private var currentConfig: GestureConfig = configFlow.value
    private var started = false

    fun start() {
        scope.launch {
            configFlow.collect { newConfig ->
                val old = currentConfig
                currentConfig = newConfig
                if (!started) {
                    started = true
                    createOverlayWindows()
                } else {
                    applyConfigDiff(old, newConfig)
                }
            }
        }
    }

    fun stop() {
        overlayManager.removeAll()
        detectors.clear()
        scope.cancel()
    }

    fun onForegroundAppChanged(packageName: String) {
        // Phase 2: 黑名单/白名单检测
    }

    fun onConfigurationChanged(newConfig: Configuration) {
        overlayManager.removeAll()
        detectors.clear()
        createOverlayWindows()
    }

    private fun applyConfigDiff(old: GestureConfig, new: GestureConfig) {
        for (edge in listOf(Edge.LEFT, Edge.RIGHT, Edge.BOTTOM)) {
            val wasEnabled = isEdgeEnabled(old, edge)
            val nowEnabled = isEdgeEnabled(new, edge)
            if (wasEnabled && !nowEnabled) {
                removeEdge(edge)
            } else if (!wasEnabled && nowEnabled) {
                addEdgeOverlay(edge)
            }
        }
    }

    private fun isEdgeEnabled(config: GestureConfig, edge: Edge): Boolean {
        return when (edge) {
            Edge.LEFT -> config.leftEnabled
            Edge.RIGHT -> config.rightEnabled
            Edge.BOTTOM -> config.bottomEnabled
        }
    }

    private fun removeEdge(edge: Edge) {
        val tag = "sensor_${edge.name.lowercase()}"
        overlayManager.removeWindow(tag)
        detectors.remove(edge)
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
                overlayManager.addWindow(tag, window)
            }
            Edge.RIGHT -> {
                val detector = createDetector(Edge.RIGHT, screenHeight.toFloat())
                val window = OverlayWindowFactory.createEdgeSensor(
                    overlayManager.context, Edge.RIGHT, edgeWidthPx, screenHeight,
                    onTouchListener = this
                )
                detectors[Edge.RIGHT] = detector
                overlayManager.addWindow(tag, window)
            }
            Edge.BOTTOM -> {
                val detector = createDetector(Edge.BOTTOM, screenWidth.toFloat())
                val window = OverlayWindowFactory.createEdgeSensor(
                    overlayManager.context, Edge.BOTTOM, screenWidth, bottomHeightPx,
                    onTouchListener = this
                )
                detectors[Edge.BOTTOM] = detector
                overlayManager.addWindow(tag, window)
            }
        }
    }

    private fun createOverlayWindows() {
        if (currentConfig.leftEnabled) addEdgeOverlay(Edge.LEFT)
        if (currentConfig.rightEnabled) addEdgeOverlay(Edge.RIGHT)
        if (currentConfig.bottomEnabled) addEdgeOverlay(Edge.BOTTOM)
    }

    private fun createDetector(edge: Edge, sensorLength: Float): EdgeGestureDetector {
        val configCopy = currentConfig.copy(sensorLength = sensorLength)
        val touchSlop = ViewConfiguration.get(overlayManager.context).scaledTouchSlop
        return EdgeGestureDetector(
            edge = edge,
            config = configCopy,
            scaledTouchSlop = touchSlop,
            onGestureResult = { result -> handleGestureResult(result) }
        )
    }

    private fun handleGestureResult(result: GestureResult) {
        val action = mapResultToAction(result)
        if (action != ActionType.None) {
            scope.launch {
                actionDispatcher.dispatch(action)
            }
        }
    }

    private fun mapResultToAction(result: GestureResult): ActionType {
        return when (result) {
            is GestureResult.EdgeSwipe -> {
                when (result.edge) {
                    Edge.LEFT, Edge.RIGHT -> {
                        if (result.isPrimary) ActionType.Navigation.Back
                        else ActionType.Navigation.SwitchLastApp
                    }
                    Edge.BOTTOM -> {
                        if (result.isPrimary) ActionType.Navigation.Home
                        else ActionType.Navigation.Recents
                    }
                }
            }
            is GestureResult.VerticalSwipe -> ActionType.None
            is GestureResult.Tap -> ActionType.None
        }
    }

    override fun onEdgeTouch(edge: Edge, event: MotionEvent): Boolean {
        return detectors[edge]?.onTouchEvent(event) ?: false
    }
}
