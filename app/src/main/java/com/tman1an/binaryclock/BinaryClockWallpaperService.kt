package com.tman1an.binaryclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.RectF
import android.service.wallpaper.WallpaperService
import android.text.format.DateFormat
import android.view.SurfaceHolder
import androidx.core.content.ContextCompat
import java.util.Calendar

/**
 * Live wallpaper showing the binary clock. Unlike widgets, a live wallpaper also appears
 * on the lock screen of every Android phone. It only redraws while visible, once a minute.
 */
class BinaryClockWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = ClockEngine()

    private inner class ClockEngine : Engine() {

        private val renderer = BinaryClockRenderer()
        private val bounds = RectF()
        private var receiverRegistered = false

        private val timeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) = draw()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                registerTimeReceiver()
                draw()
            } else {
                unregisterTimeReceiver()
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            draw()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            unregisterTimeReceiver()
            super.onSurfaceDestroyed(holder)
        }

        override fun onDestroy() {
            unregisterTimeReceiver()
            super.onDestroy()
        }

        private fun registerTimeReceiver() {
            if (receiverRegistered) return
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_TIME_TICK)
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
            }
            ContextCompat.registerReceiver(
                this@BinaryClockWallpaperService, timeReceiver, filter,
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
            receiverRegistered = true
        }

        private fun unregisterTimeReceiver() {
            if (!receiverRegistered) return
            unregisterReceiver(timeReceiver)
            receiverRegistered = false
        }

        private fun draw() {
            val holder = surfaceHolder
            val canvas = holder.lockCanvas() ?: return
            try {
                val context = this@BinaryClockWallpaperService
                // Colours are read on every draw so Material You changes are picked up.
                canvas.drawColor(context.getColor(R.color.wallpaper_surface))

                val now = Calendar.getInstance()
                val lit = BinaryTime.litDots(
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(context),
                )
                val size = minOf(canvas.width, canvas.height) * GRID_FRACTION
                val cx = canvas.width / 2f
                val cy = canvas.height / 2f
                bounds.set(cx - size / 2, cy - size / 2, cx + size / 2, cy + size / 2)
                renderer.draw(canvas, bounds, lit, context.getColor(R.color.wallpaper_accent))
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }

    private companion object {
        const val GRID_FRACTION = 0.6f
    }
}
