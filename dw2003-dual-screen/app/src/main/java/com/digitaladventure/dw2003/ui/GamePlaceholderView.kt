package com.digitaladventure.dw2003.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import kotlin.math.min

class GamePlaceholderView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.rgb(2, 9, 15))
        paint.strokeWidth = dp(1f)
        paint.color = Color.rgb(7, 42, 56)
        val grid = dp(42f)
        var x = 0f
        while (x < width) { canvas.drawLine(x, 0f, x, height.toFloat(), paint); x += grid }
        var y = 0f
        while (y < height) { canvas.drawLine(0f, y, width.toFloat(), y, paint); y += grid }

        val radius = min(width, height) * 0.19f
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(3f)
        paint.color = Color.rgb(31, 213, 242)
        canvas.drawCircle(width / 2f, height / 2f - dp(18f), radius, paint)
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        paint.textSize = dp(22f)
        canvas.drawText("VISTA DEL JUEGO", width / 2f, height / 2f, paint)
        paint.color = Color.rgb(137, 174, 190)
        paint.textSize = dp(12f)
        canvas.drawText("MODO DEMOSTRACIÓN · SELECCIONA TU BIN PARA JUGAR", width / 2f, height / 2f + dp(32f), paint)
    }

    private fun dp(value: Float) = value * resources.displayMetrics.density
}
