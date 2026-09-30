package com.tman1an.binaryclock

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SweepGradient
import android.view.View
import com.google.android.material.color.MaterialColors
import kotlin.math.min

/**
 * A round colour swatch with a selection ring. A swatch with [color] == [ClockColors.AUTO]
 * is drawn as a colour wheel to signal "follow the theme".
 */
class ColorSwatchView(context: Context, val color: Int) : View(context) {

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f * resources.displayMetrics.density
    }

    init {
        isClickable = true
        isFocusable = true
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (color == ClockColors.AUTO) {
            fill.shader = SweepGradient(
                w / 2f, h / 2f,
                intArrayOf(
                    0xFF00ACC1.toInt(), 0xFF5C6BC0.toInt(), 0xFFD81B60.toInt(),
                    0xFFFB8C00.toInt(), 0xFF43A047.toInt(), 0xFF00ACC1.toInt(),
                ),
                null,
            )
        } else {
            fill.color = color
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = (48 * resources.displayMetrics.density).toInt()
        setMeasuredDimension(
            resolveSize(size, widthMeasureSpec),
            resolveSize(size, heightMeasureSpec),
        )
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val outer = min(width, height) / 2f - ring.strokeWidth
        val inner = if (isSelected) outer - ring.strokeWidth * 2 else outer
        canvas.drawCircle(cx, cy, inner, fill)
        if (isSelected) {
            ring.color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface, Color.BLACK)
            canvas.drawCircle(cx, cy, outer, ring)
        }
    }
}
