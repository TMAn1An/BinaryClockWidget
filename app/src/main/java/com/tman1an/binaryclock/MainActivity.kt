package com.tman1an.binaryclock

import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.color.DynamicColors
import com.google.android.material.snackbar.Snackbar

/**
 * Launcher screen: a live preview, a short guide, and one-tap setup for the
 * home screen widget, the lock screen wallpaper and exact-alarm permission.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var exactAlarmCard: View

    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivityIfAvailable(this)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val scroll = findViewById<View>(R.id.scroll)
        ViewCompat.setOnApplyWindowInsetsListener(scroll) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.updatePadding(left = bars.left, top = bars.top, right = bars.right, bottom = bars.bottom)
            insets
        }

        findViewById<View>(R.id.add_widget).setOnClickListener { requestPinWidget(it) }
        findViewById<View>(R.id.set_wallpaper).setOnClickListener { openWallpaperPicker(it) }

        exactAlarmCard = findViewById(R.id.exact_alarm_card)
        findViewById<View>(R.id.grant_exact_alarm).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                startActivity(
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        .setData(Uri.parse("package:$packageName"))
                )
            }
        }

        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        findViewById<android.widget.TextView>(R.id.version).text =
            getString(R.string.version_label, versionName)
    }

    override fun onResume() {
        super.onResume()
        val granted = BinaryClockWidgetProvider.canScheduleExactAlarms(this)
        exactAlarmCard.visibility = if (granted) View.GONE else View.VISIBLE
        BinaryClockWidgetProvider.updateAll(this)
    }

    private fun requestPinWidget(anchor: View) {
        val manager = AppWidgetManager.getInstance(this)
        if (manager.isRequestPinAppWidgetSupported) {
            manager.requestPinAppWidget(
                ComponentName(this, BinaryClockWidgetProvider::class.java), null, null
            )
        } else {
            Snackbar.make(anchor, R.string.home_manual, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun openWallpaperPicker(anchor: View) {
        val direct = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(
            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
            ComponentName(this, BinaryClockWallpaperService::class.java),
        )
        try {
            startActivity(direct)
        } catch (e: ActivityNotFoundException) {
            try {
                startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))
            } catch (e: ActivityNotFoundException) {
                Snackbar.make(anchor, R.string.lock_unavailable, Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
