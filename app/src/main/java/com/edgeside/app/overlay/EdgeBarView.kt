package com.edgeside.app.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.edgeside.app.data.entity.PanelEdge
import timber.log.Timber
import kotlin.math.abs

class EdgeBarView @JvmOverloads constructor(
    ctx: Context,
    private val edge: PanelEdge,
    private val heightDp: Int = 100
) : View(ctx) {

    var onPanelToggle: ((Boolean) -> Unit)? = null
    var onPositionChanged: ((Int) -> Unit)? = null
    var onEdgeChanged: ((PanelEdge) -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF0A84FF.toInt()
        style = Paint.Style.FILL
    }
    private val rect = RectF()

    private var downRawX: Float = 0f
    private var downRawY: Float = 0f
    private var lastRawX: Float = 0f
    private var lastRawY: Float = 0f
    private var downTime: Long = 0L
    private var isDragging: Boolean = false
    private var panelShown: Boolean = false
    private var moveAccumX: Float = 0f
    private var moveAccumY: Float = 0f

    private val density = resources.displayMetrics.density
    private val touchSlopPx = (12 * density)
    private val longPressTimeoutMs = 250L

    fun setPanelShown(shown: Boolean) {
        panelShown = shown
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        rect.set(0f, 0f, w, h)
        canvas.drawRoundRect(rect, w / 2, w / 2, paint)
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            strokeWidth = 2 * density
        }
        val cx = w / 2
        canvas.drawLine(cx, h * 0.35f, cx, h * 0.65f, linePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downRawX = event.rawX
                downRawY = event.rawY
                lastRawX = event.rawX
                lastRawY = event.rawY
                downTime = System.currentTimeMillis()
                isDragging = false
                moveAccumX = 0f
                moveAccumY = 0f
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - lastRawX
                val dy = event.rawY - lastRawY
                lastRawX = event.rawX
                lastRawY = event.rawY
                moveAccumX += abs(dx)
                moveAccumY += abs(dy)

                // NOTE: No "swipe-to-open" detection here.
                // Detecting an inward swipe stole the system's edge-back gesture
                // (which lives in the same edge zone). The panel is now opened by
                // tapping the bar only — see ACTION_UP click handling below.

                // Dragging to move the bar (only after a clear drag intent, so a
                // tap never accidentally starts a drag).
                if (isDragging || moveAccumY > touchSlopPx || moveAccumX > touchSlopPx * 1.5f) {
                    if (!isDragging) {
                        isDragging = true
                    }
                    moveBy(dx.toInt(), dy.toInt())
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val elapsed = System.currentTimeMillis() - downTime
                val totalDx = event.rawX - downRawX
                val totalDy = event.rawY - downRawY
                val dist = abs(totalDx) + abs(totalDy)

                if (!isDragging && dist < touchSlopPx && elapsed < longPressTimeoutMs) {
                    // Treat as click - toggle panel
                    panelShown = !panelShown
                    onPanelToggle?.invoke(panelShown)
                } else {
                    // Dragging ended - snap to nearest edge and save position
                    val params = layoutParams as? WindowManager.LayoutParams
                    if (params != null) {
                        val metrics = resources.displayMetrics
                        val screenWidth = metrics.widthPixels
                        val screenHeight = metrics.heightPixels
                        // Decide which edge based on current X center of screen
                        val centerX = params.x + width / 2
                        val newEdge = if (centerX < screenWidth / 2) PanelEdge.LEFT else PanelEdge.RIGHT
                        // Snap X to 0 (gravity handles exact edge)
                        params.x = 0
                        params.y = params.y.coerceIn(0, (screenHeight - height).coerceAtLeast(0))
                        try {
                            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                                .updateViewLayout(this, params)
                            onPositionChanged?.invoke(params.y)
                            if (newEdge != edge) {
                                onEdgeChanged?.invoke(newEdge)
                            }
                        } catch (e: Throwable) {
                            Timber.e(e, "Failed to update EdgeBarView layout")
                        }
                    }
                }
            }
        }
        return true
    }

    private fun moveBy(dx: Int, dy: Int) {
        val params = layoutParams as? WindowManager.LayoutParams ?: return
        params.x += dx
        params.y += dy
        val metrics = resources.displayMetrics
        params.y = params.y.coerceIn(0, (metrics.heightPixels - height).coerceAtLeast(0))
        params.x = params.x.coerceIn(-width, metrics.widthPixels)
        try {
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                .updateViewLayout(this, params)
        } catch (_: Throwable) {}
    }
}
