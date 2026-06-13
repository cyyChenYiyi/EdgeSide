package com.edgeside.app.service

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.os.SystemClock
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import com.edgeside.app.data.AppContainer
import com.edgeside.app.data.entity.BarConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Floating ball pinned to the screen edge.
 *
 * Visual: 48dp diameter circle, #1976D2 semi-transparent fill,
 *         1.5dp white rim, two small dots in center.
 * Touch:  64dp x 64dp hit area, ball center sits exactly at screen edge.
 *
 * Interactions:
 *  - Single tap: open quick panel.
 *  - Horizontal drag > 48dp: open panel.
 *  - Vertical drag: adjust Y position (snapped to bounds).
 *  - LONG PRESS 3 seconds: stop entire service (safe quit).
 *  - Release: magnet snap to nearest screen edge.
 */
@SuppressLint("ViewConstructor")
class EdgeBarView(
    context: Context,
    private val container: AppContainer
) : View(context) {

    var onPanelToggle: ((Boolean) -> Unit)? = null
    var onLongPressStop: (() -> Unit)? = null

    private val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val density = context.resources.displayMetrics.density

    // Ball metrics.
    private val ballDiameterPx = (48 * density).toInt()
    private val ballRadiusPx = ballDiameterPx / 2f
    private val touchSizePx = (64 * density).toInt()
    private val rimWidthPx = (1.5f * density)
    private val slideTriggerPx = (48 * density).toInt()
    private val longPressMs = 3000L

    // State.
    private var downRawX = 0f
    private var downRawY = 0f
    private var initialY = 0
    private var dragging = false
    private var isShowingPanel = false
    private var longPressArmed = false
    private var longPressFired = false
    private var currentEdge = "LEFT"

    private val longPressRunnable = Runnable {
        if (longPressArmed && !dragging) {
            longPressFired = true
            invalidate()
            onLongPressStop?.invoke()
        }
    }

    // Paints.
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CC1976D2")
    }
    private val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#DDEEEEEE")
        style = Paint.Style.STROKE
        strokeWidth = rimWidthPx
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#DDFFFFFF")
    }
    private val dotPaintDim = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#99FFFFFF")
    }

    init {
        isClickable = true
        isFocusable = true
    }

    fun buildLayoutParams(edge: String): WindowManager.LayoutParams {
        currentEdge = edge
        val gravity = if (edge == "LEFT")
            Gravity.START or Gravity.CENTER_VERTICAL
        else
            Gravity.END or Gravity.CENTER_VERTICAL
        // Position so ball center is exactly at screen edge.
        val xOffset = if (edge == "LEFT")
            -(touchSizePx / 2)  // view left is half-width off-screen left
        else
            touchSizePx / 2     // view right extends half-width beyond screen right
        return WindowManager.LayoutParams(
            touchSizePx,
            touchSizePx,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            this.gravity = gravity
            x = xOffset
            y = initialY
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(touchSizePx, touchSizePx)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f

        // Choose fill color based on state.
        val fillColor = when {
            longPressFired -> Color.parseColor("#FFE53935")  // red when quitting
            longPressArmed && !dragging -> Color.parseColor("#BB90CAF9") // lighter during long-press countdown
            isShowingPanel -> Color.parseColor("#CC1565C0")  // slightly darker when panel open
            else -> Color.parseColor("#CC1976D2")
        }
        fillPaint.color = fillColor

        // Draw ball fill.
        canvas.drawCircle(cx, cy, ballRadiusPx, fillPaint)
        // Draw rim.
        canvas.drawCircle(cx, cy, ballRadiusPx - rimWidthPx / 2f, rimPaint)

        // Draw double-dot icon.
        val dotRadius = (2.5f * density)
        val dotSpacing = (8f * density)
        canvas.drawCircle(cx - dotSpacing / 2f, cy, dotRadius, dotPaint)
        canvas.drawCircle(cx + dotSpacing / 2f, cy, dotRadius, dotPaintDim)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val params = layoutParams as? WindowManager.LayoutParams ?: return false
        val screenW = context.resources.displayMetrics.widthPixels
        val screenH = context.resources.displayMetrics.heightPixels
        val topMargin = (24 * density).toInt()
        val bottomMargin = topMargin

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downRawX = event.rawX
                downRawY = event.rawY
                initialY = params.y
                dragging = false
                longPressArmed = true
                longPressFired = false
                removeCallbacks(longPressRunnable)
                postDelayed(longPressRunnable, longPressMs)
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - downRawX
                val dy = event.rawY - downRawY
                if (!dragging && (abs(dx) > touchSlop || abs(dy) > touchSlop)) {
                    dragging = true
                    longPressArmed = false
                    removeCallbacks(longPressRunnable)
                    invalidate()
                }
                if (dragging) {
                    // Horizontal drag past threshold -> open panel.
                    if (abs(dx) > slideTriggerPx) {
                        triggerPanel()
                        // Reset drag so it only fires once per gesture.
                        dragging = false
                        return true
                    }
                    val newY = (initialY + dy).toInt()
                    val maxY = screenH - touchSizePx - bottomMargin
                    params.y = max(topMargin, min(newY, maxY))
                    runCatching { wm.updateViewLayout(this, params) }
                    scope.launch {
                        container.configRepository.upsertBarConfig(
                            BarConfig(
                                verticalPos = params.y,
                                edge = currentEdge
                            )
                        )
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                longPressArmed = false
                removeCallbacks(longPressRunnable)
                if (!longPressFired && !dragging) {
                    performClick()
                    triggerPanel()
                } else if (!longPressFired) {
                    // Magnetic snap to nearest edge.
                    val newEdge = if (event.rawX < screenW / 2f) "LEFT" else "RIGHT"
                    if (newEdge != currentEdge) {
                        val newParams = buildLayoutParams(newEdge).apply { y = params.y }
                        runCatching { wm.updateViewLayout(this, newParams) }
                    }
                    // Save position.
                    scope.launch {
                        container.configRepository.upsertBarConfig(
                            BarConfig(
                                verticalPos = params.y,
                                edge = if (newEdge == currentEdge) currentEdge else newEdge
                            )
                        )
                    }
                }
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun triggerPanel() {
        if (isShowingPanel) return
        isShowingPanel = true
        invalidate()
        onPanelToggle?.invoke(true)
    }

    fun setPanelShowing(showing: Boolean) {
        isShowingPanel = showing
        invalidate()
    }

    fun getCurrentEdge(): String = currentEdge
}