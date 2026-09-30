package com.tman1an.binaryclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.RectF
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.color.MaterialColors
import java.util.Calendar

/** A live, theme-coloured binary clock used as the preview in the app. */
class BinaryClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val renderer = BinaryClockRenderer()
    private val bounds = RectF()

    private val timeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = invalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        ContextCompat.registerReceiver(
            context, timeReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED
        )
        invalidate()
    }

    override fun onDetachedFromWindow() {
        context.unregisterReceiver(timeReceiver)
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        val now = Calendar.getInstance()
        val lit = BinaryTime.litDots(
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            DateFormat.is24HourFormat(context),
        )
        bounds.set(
            paddingLeft.toFloat(), paddingTop.toFloat(),
            (width - paddingRight).toFloat(), (height - paddingBottom).toFloat(),
        )
        val accent = ClockColors.custom(context)
            ?: MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary)
        renderer.draw(canvas, bounds, lit, accent)
    }
}
