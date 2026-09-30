package com.tman1an.binaryclock

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.format.DateFormat
import android.widget.RemoteViews
import java.util.Calendar

class BinaryClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        render(context, manager, appWidgetIds)
        scheduleNextTick(context)
    }

    override fun onEnabled(context: Context) {
        scheduleNextTick(context)
    }

    override fun onDisabled(context: Context) {
        alarmManager(context).cancel(tickIntent(context))
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_TICK,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED -> updateAll(context)
            else -> super.onReceive(context, intent)
        }
    }

    companion object {
        private const val ACTION_TICK = "com.tman1an.binaryclock.action.TICK"

        /** Dot view ids indexed as [column][row], rows ordered 8-4-2-1. */
        private val DOT_IDS = arrayOf(
            intArrayOf(R.id.dot_h1_8, R.id.dot_h1_4, R.id.dot_h1_2, R.id.dot_h1_1),
            intArrayOf(R.id.dot_h2_8, R.id.dot_h2_4, R.id.dot_h2_2, R.id.dot_h2_1),
            intArrayOf(R.id.dot_m1_8, R.id.dot_m1_4, R.id.dot_m1_2, R.id.dot_m1_1),
            intArrayOf(R.id.dot_m2_8, R.id.dot_m2_4, R.id.dot_m2_2, R.id.dot_m2_1),
        )

        /** Redraws every placed widget and schedules the next minute tick. */
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, BinaryClockWidgetProvider::class.java)
            )
            if (ids.isEmpty()) return
            render(context, manager, ids)
            scheduleNextTick(context)
        }

        private fun render(context: Context, manager: AppWidgetManager, ids: IntArray) {
            val now = Calendar.getInstance()
            val lit = BinaryTime.litDots(
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(context),
            )

            val views = RemoteViews(context.packageName, R.layout.widget_binary_clock)
            for (column in DOT_IDS.indices) {
                for (row in DOT_IDS[column].indices) {
                    views.setImageViewResource(
                        DOT_IDS[column][row],
                        if (lit[column][row]) R.drawable.dot_on else R.drawable.dot_off,
                    )
                }
            }

            val openApp = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.widget_root, openApp)

            manager.updateAppWidget(ids, views)
        }

        /**
         * Schedules a redraw at the start of the next minute. Uses a non-wakeup
         * alarm: while the screen is off nothing is visible, so the alarm may
         * wait until the device wakes up.
         */
        private fun scheduleNextTick(context: Context) {
            val nextMinute = Calendar.getInstance().apply {
                add(Calendar.MINUTE, 1)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val alarms = alarmManager(context)
            val pending = tickIntent(context)
            if (canScheduleExactAlarms(context)) {
                alarms.setExact(AlarmManager.RTC, nextMinute, pending)
            } else {
                // Without the exact-alarm permission Android may deliver this late.
                alarms.set(AlarmManager.RTC, nextMinute, pending)
            }
        }

        fun canScheduleExactAlarms(context: Context): Boolean =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                alarmManager(context).canScheduleExactAlarms()

        private fun alarmManager(context: Context) =
            context.getSystemService(AlarmManager::class.java)

        private fun tickIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
            context, 0,
            Intent(context, BinaryClockWidgetProvider::class.java).setAction(ACTION_TICK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
