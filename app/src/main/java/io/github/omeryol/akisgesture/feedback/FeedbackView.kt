package io.github.omeryol.akisgesture.feedback

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Bitmap
import android.graphics.Color
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.PathInterpolator
import io.github.omeryol.akisgesture.overlay.Edge
import io.github.omeryol.akisgesture.gesture.model.SwipeDirection
import io.github.omeryol.akisgesture.model.GestureType
import io.github.omeryol.akisgesture.model.ActionIconColorMode
import io.github.omeryol.akisgesture.model.ActionIconPack
import io.github.omeryol.akisgesture.model.ActionNode

/**
 * Dokunmayı engellemeyen erişilebilirlik katmanında akıcı hareket geri bildirimi.
 * Eylem simgesi desteği: gesture bir eylemle eşleştiğinde o eylemin simgesi gösterilir.
 */
class FeedbackView(context: Context) : View(context) {

    private val renderer = BezierStretchRenderer()
    private val appSwitchRenderer = AppSwitchFeedbackRenderer()
    private val ringMenuRenderer = RingMenuRenderer()
    private var releaseAnimator: ValueAnimator? = null

    var edge: Edge = Edge.LEFT

    var stretchDistance: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    var touchPosition: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    var peakThreshold: Float = 30f
    var feedbackColor: Int
        get() = renderer.primaryColor
        set(value) {
            renderer.primaryColor = value
            renderer.baseColor = value
            invalidate()
        }
    var primaryColor: Int
        get() = renderer.primaryColor
        set(value) {
            renderer.primaryColor = value
            renderer.baseColor = value
            invalidate()
        }
    var secondaryColor: Int
        get() = renderer.secondaryColor
        set(value) {
            renderer.secondaryColor = value
            invalidate()
        }
    var lSwipeColor: Int
        get() = renderer.lSwipeColor
        set(value) {
            renderer.lSwipeColor = value
            invalidate()
        }
    var feedbackOpacity: Float
        get() = renderer.opacity
        set(value) {
            renderer.opacity = value.coerceIn(0.1f, 1f)
            invalidate()
        }
    var feedbackAnimation: FeedbackAnimation
        get() = renderer.animation
        set(value) {
            renderer.animation = value
            invalidate()
        }
    private val actionIconCache = android.util.LruCache<String, Bitmap>(96)

    fun setAction(
        action: ActionNode?,
        pack: ActionIconPack,
        colorMode: ActionIconColorMode = ActionIconColorMode.FUNCTIONAL,
    ) {
        renderer.actionIcon = action?.let { loadActionIcon(it, pack, colorMode) }
        invalidate()
    }

    fun showFinalAction(
        action: ActionNode,
        pack: ActionIconPack,
        colorMode: ActionIconColorMode = ActionIconColorMode.FUNCTIONAL,
    ) {
        renderer.showFinalActionIcon(loadActionIcon(action, pack, colorMode))
        invalidate()
    }
    /** Animasyon hız ve boyut çarpanları */
    var animationSpeed: Float
        get() = renderer.animSpeed
        set(value) {
            renderer.animSpeed = value.coerceIn(0.5f, 2f)
            invalidate()
        }
    var animationSize: Float
        get() = renderer.animSize
        set(value) {
            renderer.animSize = value.coerceIn(0.5f, 2f)
            invalidate()
        }
    var iconSize: Float
        get() = renderer.iconSize
        set(value) {
            renderer.iconSize = value.coerceIn(0.5f, 2f)
            invalidate()
        }
    var showIndicatorBar: Boolean
        get() = renderer.showIndicatorBar
        set(value) {
            renderer.showIndicatorBar = value
            invalidate()
        }
    @Deprecated("setAction ile değiştirildi")
    var quickIcon: FeedbackIcon = FeedbackIcon.CHEVRON
        set(value) { renderer.quickIcon = value; invalidate() }
    @Deprecated("setAction ile değiştirildi")
    var holdIcon: FeedbackIcon = FeedbackIcon.STAR
        set(value) { renderer.holdIcon = value; invalidate() }
    var isActive: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    var isArmed: Boolean = false
        set(value) {
            field = value
            renderer.armed = value
            invalidate()
        }

    var isHoldArmed: Boolean = false
        set(value) {
            field = value
            renderer.holdArmed = value
            invalidate()
        }

    var appSwitchDirection: SwipeDirection? = null
        set(value) {
            field = value
            invalidate()
        }

    var ringIcons: List<Bitmap?> = emptyList()
    var ringSelectedIndex: Int = -1
    var ringGroupInsetDp: Float = 100f
    var ringGroupSpacingDp: Float = 60f
    var ringSizeDp: Float = 58f
    var ringArc: Float = 0.92f
    private var ringPreviewToken: Int = 0
    private var ringActive: Boolean = false

    private val arrowAlpha: Float
        get() = if (peakThreshold > 0f) {
            (stretchDistance / peakThreshold).coerceIn(0f, 1f)
        } else {
            0f
        }

    init {
        isClickable = false
        isFocusable = false
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isActive || stretchDistance < 0.5f) return
        // Natural motion uses a continuous time phase. Keep drawing while the
        // finger is held still instead of only advancing on MOVE events.
        postInvalidateOnAnimation()
        appSwitchDirection?.let { direction ->
            appSwitchRenderer.draw(
                canvas = canvas,
                direction = direction,
                touchX = touchPosition,
                progress = arrowAlpha,
                armed = isArmed,
                color = feedbackColor,
                opacity = feedbackOpacity,
            )
            return
        }
        renderer.draw(
            canvas = canvas,
            edge = edge,
            stretch = stretchDistance,
            touchPosition = touchPosition,
            peak = peakThreshold,
            canvasWidth = width.toFloat(),
            canvasHeight = height.toFloat(),
            arrowAlpha = arrowAlpha,
        )
        if (ringActive) {
            ringMenuRenderer.draw(
                canvas = canvas,
                edge = edge,
                touch = touchPosition,
                width = width.toFloat(),
                height = height.toFloat(),
                stretch = stretchDistance,
                threshold = peakThreshold,
                extraInsetPx = ringGroupInsetDp * resources.displayMetrics.density,
                spreadPx = ringGroupSpacingDp * resources.displayMetrics.density,
                color = feedbackColor,
                opacity = feedbackOpacity,
                icons = ringIcons,
                selectedIndex = ringSelectedIndex,
                iconScale = iconSize,
                ringSizeDp = ringSizeDp,
                ringArc = ringArc,
            )
        } else {
            ringMenuRenderer.resetAnimation()
        }
    }

    fun updateGestureState(
        edge: Edge,
        stretch: Float,
        touchPos: Float,
        active: Boolean,
        armed: Boolean,
        holdArmed: Boolean,
        appSwitchDirection: SwipeDirection? = null,
        isLUp: Boolean = false,
        isLDown: Boolean = false,
        bendStartY: Float = 0f,
        lColorProgress: Float = 0f,
        lPreviewGesture: GestureType? = null,
        ringActive: Boolean = false,
    ) {
        this.edge = edge
        // During the vertical leg of an L gesture, keep the visual anchored at
        // the bend. Finger travel controls color/progress without dragging the
        // whole animation up or down the edge.
        // Once the inward leg of a side-edge L gesture is armed, keep every
        // feedback layer anchored at the bend. When the user reverses the
        // second leg, lColorProgress may temporarily return to zero; using
        // touchPos in that moment makes the whole visual stack jump with the
        // finger instead of staying at the original turn point.
        this.touchPosition = if (
            (edge == Edge.LEFT || edge == Edge.RIGHT) &&
            bendStartY > 0f
        ) bendStartY else touchPos
        this.isArmed = armed
        this.isHoldArmed = holdArmed
        this.appSwitchDirection = appSwitchDirection
        if (active && !isActive) {
            renderer.clearPinnedActionIcon()
        }
        renderer.isLUp = isLUp
        renderer.isLDown = isLDown
        renderer.bendStartY = bendStartY
        renderer.lColorProgress = lColorProgress
        renderer.lPreviewGesture = lPreviewGesture
        this.ringActive = ringActive
        if (active) {
            releaseAnimator?.cancel()
            isActive = true
            stretchDistance = stretch
        } else {
            animateRelease()
        }
    }

    private var lastRingActions: List<ActionNode>? = null
    private var lastRingPack: ActionIconPack? = null
    private var lastRingColorMode: ActionIconColorMode? = null

    /** Shows the real overlay rings while the user adjusts ring settings. */
    fun setRingActions(
        actions: List<ActionNode>,
        pack: ActionIconPack,
        colorMode: ActionIconColorMode = ActionIconColorMode.FUNCTIONAL,
    ) {
        if (actions == lastRingActions && pack == lastRingPack && colorMode == lastRingColorMode) {
            return
        }
        lastRingActions = actions
        lastRingPack = pack
        lastRingColorMode = colorMode
        ringIcons = actions.map { loadActionIcon(it, pack, colorMode) }
        invalidate()
    }

    fun showRingPreview(
        edge: Edge,
        actions: List<ActionNode>,
        pack: ActionIconPack,
        colorMode: ActionIconColorMode = ActionIconColorMode.FUNCTIONAL,
    ) {
        ringPreviewToken += 1
        val token = ringPreviewToken
        this.edge = edge
        setRingActions(actions, pack, colorMode)
        this.ringSelectedIndex = -1
        this.ringActive = actions.isNotEmpty()
        this.appSwitchDirection = null
        this.isActive = true
        this.stretchDistance = peakThreshold + 220f
        this.touchPosition = when (edge) {
            Edge.LEFT, Edge.RIGHT -> height * 0.5f
            Edge.BOTTOM -> width * 0.5f
        }
        postInvalidateOnAnimation()
        postDelayed({
            if (ringPreviewToken == token) {
                ringActive = false
                isActive = false
                invalidate()
            }
        }, 2_500L)
    }

    private fun animateRelease() {
        releaseAnimator?.cancel()
        if (stretchDistance < 0.5f) {
            isActive = false
            appSwitchDirection = null
            renderer.actionIcon = null
            return
        }
        releaseAnimator = ValueAnimator.ofFloat(stretchDistance, 0f).apply {
            duration = 200L
            interpolator = PathInterpolator(0.16f, 1f, 0.3f, 1f)
            addUpdateListener {
                stretchDistance = it.animatedValue as Float
                if (stretchDistance < 0.5f) {
                    isActive = false
                    appSwitchDirection = null
                    renderer.actionIcon = null
                }
            }
            start()
        }
    }

    private fun loadActionIcon(
        action: ActionNode,
        pack: ActionIconPack,
        colorMode: ActionIconColorMode = ActionIconColorMode.FUNCTIONAL,
    ): Bitmap? {
        val key = "${pack.id}:${colorMode.id}:${action.id}"
        actionIconCache.get(key)?.let { return it }
        val tintColor = colorMode.resolveColorInt(action)
        return ActionBitmapLoader.load(this.context, action, pack, sizePx = 128, tint = tintColor)
            ?.also { actionIconCache.put(key, it) }
    }
}
