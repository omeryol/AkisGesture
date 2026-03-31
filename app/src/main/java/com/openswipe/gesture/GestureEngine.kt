package com.openswipe.gesture

import android.content.res.Configuration
import android.content.res.Resources
import android.util.TypedValue
import android.view.MotionEvent
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
import kotlinx.coroutines.launch

class GestureEngine(
    private val config: GestureConfig,
    private val actionDispatcher: ActionDispatcher,
    private val overlayManager: OverlayManager,
) : EdgeSensorView.OnEdgeTouchListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val detectors = mutableMapOf<Edge, EdgeGestureDetector>()

    fun start() {
        createOverlayWindows()
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

    private fun createOverlayWindows() {
        val resources = Resources.getSystem()
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val screenWidth = displayMetrics.widthPixels

        val edgeWidthPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, config.edgeTriggerWidthDp, displayMetrics
        ).toInt()

        val bottomHeightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, config.bottomTriggerHeightDp, displayMetrics
        ).toInt()

        if (config.leftEnabled) {
            val detector = createDetector(Edge.LEFT, screenHeight.toFloat())
            val window = OverlayWindowFactory.createEdgeSensor(
                overlayManager.context, Edge.LEFT, edgeWidthPx, screenHeight,
                onTouchListener = this
            )
            detectors[Edge.LEFT] = detector
            overlayManager.addWindow("sensor_left", window)
        }

        if (config.rightEnabled) {
            val detector = createDetector(Edge.RIGHT, screenHeight.toFloat())
            val window = OverlayWindowFactory.createEdgeSensor(
                overlayManager.context, Edge.RIGHT, edgeWidthPx, screenHeight,
                onTouchListener = this
            )
            detectors[Edge.RIGHT] = detector
            overlayManager.addWindow("sensor_right", window)
        }

        if (config.bottomEnabled) {
            val detector = createDetector(Edge.BOTTOM, screenWidth.toFloat())
            val window = OverlayWindowFactory.createEdgeSensor(
                overlayManager.context, Edge.BOTTOM, screenWidth, bottomHeightPx,
                onTouchListener = this
            )
            detectors[Edge.BOTTOM] = detector
            overlayManager.addWindow("sensor_bottom", window)
        }
    }

    private fun createDetector(edge: Edge, sensorLength: Float): EdgeGestureDetector {
        val configCopy = config.copy(sensorLength = sensorLength)
        return EdgeGestureDetector(
            edge = edge,
            config = configCopy,
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

