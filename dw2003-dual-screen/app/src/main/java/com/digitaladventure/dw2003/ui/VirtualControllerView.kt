package com.digitaladventure.dw2003.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Typeface
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.min

enum class QuickAction { SAVE_STATE, LOAD_STATE, TOGGLE_SPEED, TOGGLE_MUTE }

@SuppressLint("ViewConstructor")
class VirtualControllerView(
    context: Context,
    private val keySink: (Int, Int) -> Unit,
    private val quickActionSink: (QuickAction) -> Unit
) : View(context) {
    private data class KeyTarget(val bounds: RectF, val keyCode: Int, val label: String)
    private data class ActionTarget(val bounds: RectF, val action: QuickAction)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val keyTargets = mutableListOf<KeyTarget>()
    private val actionTargets = mutableListOf<ActionTarget>()
    private val pointerPositions = mutableMapOf<Int, PointF>()
    private val dpadPointers = mutableSetOf<Int>()
    private val quickStarts = mutableMapOf<Int, QuickAction>()
    private var pressedKeys = emptySet<Int>()
    private var dpadCenter = PointF()
    private var dpadRadius = 0f

    var gamepadVisible: Boolean = true
        set(value) { field = value; releaseAll(); invalidate() }
    var fastForward: Boolean = false
        set(value) { field = value; invalidate() }
    var muted: Boolean = false
        set(value) { field = value; invalidate() }
    var stateAvailable: Boolean = false
        set(value) { field = value; invalidate() }

    init {
        isClickable = true
        contentDescription = "Controles virtuales de PlayStation y acciones rápidas"
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        keyTargets.clear()
        actionTargets.clear()
        drawQuickBar(canvas)
        if (gamepadVisible) drawGamepad(canvas)
    }

    private fun drawQuickBar(canvas: Canvas) {
        val margin = dp(8f)
        val gap = dp(5f)
        val top = dp(8f)
        val bottom = top + dp(36f)
        val available = width - margin * 2 - gap * 3
        val itemWidth = available / 4f
        val labels = listOf(
            QuickAction.SAVE_STATE to "GUARDAR",
            QuickAction.LOAD_STATE to if (stateAvailable) "CARGAR" else "SIN ESTADO",
            QuickAction.TOGGLE_SPEED to if (fastForward) "2× ACTIVO" else "VELOCIDAD 1×",
            QuickAction.TOGGLE_MUTE to if (muted) "SONIDO OFF" else "SONIDO ON"
        )
        labels.forEachIndexed { index, item ->
            val left = margin + index * (itemWidth + gap)
            val rect = RectF(left, top, left + itemWidth, bottom)
            val active = (item.first == QuickAction.TOGGLE_SPEED && fastForward) ||
                (item.first == QuickAction.TOGGLE_MUTE && muted)
            paint.color = if (active) Color.argb(225, 8, 105, 126) else Color.argb(205, 4, 31, 43)
            canvas.drawRoundRect(rect, dp(7f), dp(7f), paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp(1f)
            paint.color = if (item.first == QuickAction.LOAD_STATE && !stateAvailable) DIM else CYAN
            canvas.drawRoundRect(rect, dp(7f), dp(7f), paint)
            paint.style = Paint.Style.FILL
            drawText(canvas, item.second, rect.centerX(), rect.centerY() + dp(3.5f), dp(if (width < dp(500f)) 7.5f else 9f), if (item.first == QuickAction.LOAD_STATE && !stateAvailable) DIM else WHITE)
            actionTargets += ActionTarget(rect, item.first)
        }
    }

    private fun drawGamepad(canvas: Canvas) {
        val shortSide = min(width, height).toFloat()
        dpadRadius = (shortSide * .145f).coerceIn(dp(46f), dp(86f))
        dpadCenter = PointF(dp(22f) + dpadRadius, height - dp(20f) - dpadRadius)
        val arm = dpadRadius * .40f
        paint.color = CONTROL
        canvas.drawRoundRect(RectF(dpadCenter.x - arm, dpadCenter.y - dpadRadius, dpadCenter.x + arm, dpadCenter.y + dpadRadius), dp(8f), dp(8f), paint)
        canvas.drawRoundRect(RectF(dpadCenter.x - dpadRadius, dpadCenter.y - arm, dpadCenter.x + dpadRadius, dpadCenter.y + arm), dp(8f), dp(8f), paint)
        drawText(canvas, "▲", dpadCenter.x, dpadCenter.y - dpadRadius * .57f, dp(15f), WHITE)
        drawText(canvas, "▼", dpadCenter.x, dpadCenter.y + dpadRadius * .68f, dp(15f), WHITE)
        drawText(canvas, "◀", dpadCenter.x - dpadRadius * .62f, dpadCenter.y + dp(5f), dp(15f), WHITE)
        drawText(canvas, "▶", dpadCenter.x + dpadRadius * .62f, dpadCenter.y + dp(5f), dp(15f), WHITE)

        val faceRadius = (shortSide * .055f).coerceIn(dp(23f), dp(36f))
        val faceCenterX = width - dp(24f) - dpadRadius
        val faceCenterY = dpadCenter.y
        addRoundKey(canvas, faceCenterX, faceCenterY + faceRadius * 1.45f, faceRadius, KeyEvent.KEYCODE_BUTTON_B, "×")
        addRoundKey(canvas, faceCenterX + faceRadius * 1.45f, faceCenterY, faceRadius, KeyEvent.KEYCODE_BUTTON_A, "○")
        addRoundKey(canvas, faceCenterX - faceRadius * 1.45f, faceCenterY, faceRadius, KeyEvent.KEYCODE_BUTTON_Y, "□")
        addRoundKey(canvas, faceCenterX, faceCenterY - faceRadius * 1.45f, faceRadius, KeyEvent.KEYCODE_BUTTON_X, "△")

        val shoulderTop = dp(54f)
        addRectKey(canvas, RectF(dp(12f), shoulderTop, dp(92f), shoulderTop + dp(34f)), KeyEvent.KEYCODE_BUTTON_L1, "L1")
        addRectKey(canvas, RectF(width - dp(92f), shoulderTop, width - dp(12f), shoulderTop + dp(34f)), KeyEvent.KEYCODE_BUTTON_R1, "R1")

        val centerY = height - dp(34f)
        addRectKey(canvas, RectF(width / 2f - dp(96f), centerY - dp(18f), width / 2f - dp(8f), centerY + dp(14f)), KeyEvent.KEYCODE_BUTTON_SELECT, "SELECT")
        addRectKey(canvas, RectF(width / 2f + dp(8f), centerY - dp(18f), width / 2f + dp(96f), centerY + dp(14f)), KeyEvent.KEYCODE_BUTTON_START, "START")
    }

    private fun addRoundKey(canvas: Canvas, x: Float, y: Float, radius: Float, keyCode: Int, label: String) {
        val rect = RectF(x - radius, y - radius, x + radius, y + radius)
        paint.color = CONTROL
        canvas.drawOval(rect, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1.2f)
        paint.color = CYAN
        canvas.drawOval(rect, paint)
        paint.style = Paint.Style.FILL
        drawText(canvas, label, x, y + dp(7f), dp(20f), WHITE)
        keyTargets += KeyTarget(rect, keyCode, label)
    }

    private fun addRectKey(canvas: Canvas, rect: RectF, keyCode: Int, label: String) {
        paint.color = CONTROL
        canvas.drawRoundRect(rect, dp(9f), dp(9f), paint)
        drawText(canvas, label, rect.centerX(), rect.centerY() + dp(4f), dp(10f), WHITE)
        keyTargets += KeyTarget(rect, keyCode, label)
    }

    private fun keysAt(pointerId: Int, point: PointF): Set<Int> {
        if (gamepadVisible) {
            val dx = point.x - dpadCenter.x
            val dy = point.y - dpadCenter.y
            val directions = VirtualPadMath.dpadDirectionsForPointer(
                dx = dx,
                dy = dy,
                deadZone = dpadRadius * .22f,
                radius = dpadRadius,
                captured = pointerId in dpadPointers
            )
            if (directions != null) {
                return directions.mapTo(mutableSetOf()) {
                    when (it) {
                        PadDirection.UP -> KeyEvent.KEYCODE_DPAD_UP
                        PadDirection.DOWN -> KeyEvent.KEYCODE_DPAD_DOWN
                        PadDirection.LEFT -> KeyEvent.KEYCODE_DPAD_LEFT
                        PadDirection.RIGHT -> KeyEvent.KEYCODE_DPAD_RIGHT
                    }
                }
            }
        }
        return keyTargets.lastOrNull { it.bounds.contains(point.x, point.y) }?.let { setOf(it.keyCode) } ?: emptySet()
    }

    private fun actionAt(x: Float, y: Float): QuickAction? = actionTargets.lastOrNull { it.bounds.contains(x, y) }?.action

    private fun updatePressedKeys() {
        val next = pointerPositions.entries.flatMapTo(mutableSetOf()) { (id, point) -> keysAt(id, point) }
        (pressedKeys - next).forEach { keySink(KeyEvent.ACTION_UP, it) }
        (next - pressedKeys).forEach { keySink(KeyEvent.ACTION_DOWN, it) }
        pressedKeys = next
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val index = event.actionIndex
        val id = event.getPointerId(index)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val action = actionAt(event.getX(index), event.getY(index))
                if (action != null) quickStarts[id] = action
                else {
                    val point = PointF(event.getX(index), event.getY(index))
                    pointerPositions[id] = point
                    val dx = point.x - dpadCenter.x
                    val dy = point.y - dpadCenter.y
                    if (dx * dx + dy * dy <= dpadRadius * dpadRadius) dpadPointers += id
                }
                updatePressedKeys()
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val pointerId = event.getPointerId(i)
                    if (pointerId in pointerPositions) pointerPositions[pointerId] = PointF(event.getX(i), event.getY(i))
                }
                updatePressedKeys()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                pointerPositions.remove(id)
                dpadPointers.remove(id)
                updatePressedKeys()
                val started = quickStarts.remove(id)
                if (started != null && started == actionAt(event.getX(index), event.getY(index))) {
                    if (started != QuickAction.LOAD_STATE || stateAvailable) quickActionSink(started)
                    performClick()
                }
            }
            MotionEvent.ACTION_CANCEL -> releaseAll()
        }
        return true
    }

    override fun performClick(): Boolean { super.performClick(); return true }

    private fun releaseAll() {
        pressedKeys.forEach { keySink(KeyEvent.ACTION_UP, it) }
        pressedKeys = emptySet()
        pointerPositions.clear()
        dpadPointers.clear()
        quickStarts.clear()
    }

    override fun onDetachedFromWindow() { releaseAll(); super.onDetachedFromWindow() }

    private fun drawText(canvas: Canvas, text: String, x: Float, y: Float, size: Float, color: Int) {
        paint.color = color
        paint.textSize = size
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText(text, x, y, paint)
    }

    private fun dp(value: Float) = value * resources.displayMetrics.density

    companion object {
        private val CONTROL = Color.argb(178, 4, 31, 43)
        private val CYAN = Color.rgb(31, 213, 242)
        private val WHITE = Color.rgb(228, 246, 250)
        private val DIM = Color.rgb(91, 124, 136)
    }
}

enum class PadDirection { UP, DOWN, LEFT, RIGHT }

object VirtualPadMath {
    /**
     * Returns null when this pointer does not belong to the D-pad. Once a
     * pointer starts inside it, `captured` keeps directions active outside the
     * drawn circle until that same finger is released.
     */
    fun dpadDirectionsForPointer(
        dx: Float,
        dy: Float,
        deadZone: Float,
        radius: Float,
        captured: Boolean
    ): Set<PadDirection>? {
        if (!captured && dx * dx + dy * dy > radius * radius) return null
        return dpadDirections(dx, dy, deadZone)
    }

    fun dpadDirections(dx: Float, dy: Float, deadZone: Float): Set<PadDirection> {
        if (abs(dx) < deadZone && abs(dy) < deadZone) return emptySet()
        val result = mutableSetOf<PadDirection>()
        if (abs(dx) >= deadZone) result += if (dx < 0) PadDirection.LEFT else PadDirection.RIGHT
        if (abs(dy) >= deadZone) result += if (dy < 0) PadDirection.UP else PadDirection.DOWN
        return result
    }
}
