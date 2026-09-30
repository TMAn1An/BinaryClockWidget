package com.tman1an.binaryclock

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.ColorUtils
import kotlin.math.min

/**
 * Draws the 4x4 BCD dot grid onto a [Canvas], centred in the given bounds.
 * Shared by the in-app preview and the live wallpaper.
 */
class BinaryClockRenderer {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    fun draw(
        canvas: Canvas,
        bounds: RectF,
        lit: Array<BooleanArray>,
        onColor: Int,
        offAlpha: Float = OFF_ALPHA,
    ) {
        val columns = lit.size
        val rows = BinaryTime.BIT_WEIGHTS.size
        // The gap between the hour pair and the minute pair is half a cell wide.
        val cell = min(bounds.width() / (columns + PAIR_GAP), bounds.height() / rows)
        val gridWidth = cell * (columns + PAIR_GAP)
        val left = bounds.centerX() - gridWidth / 2
        val top = bounds.centerY() - cell * rows / 2
        val radius = cell * DOT_RADIUS
        val offColor = ColorUtils.setAlphaComponent(onColor, (offAlpha * 255).toInt())

        for (column in 0 until columns) {
            val gap = if (column >= columns / 2) cell * PAIR_GAP else 0f
            val cx = left + gap + cell * (column + 0.5f)
            for (row in 0 until rows) {
                if (!BinaryTime.isUsed(column, row)) continue
                paint.color = if (lit[column][row]) onColor else offColor
                canvas.drawCircle(cx, top + cell * (row + 0.5f), radius, paint)
            }
        }
    }

    private companion object {
        const val PAIR_GAP = 0.5f
        const val DOT_RADIUS = 0.36f
        const val OFF_ALPHA = 0.18f
    }
}
