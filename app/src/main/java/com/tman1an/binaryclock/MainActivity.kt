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
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
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
    private lateinit var preview: View
    private val swatches = mutableListOf<ColorSwatchView>()

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

        preview = findViewById(R.id.preview)
        setUpColorPicker()

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
        findViewById<TextView>(R.id.version).text =
            getString(R.string.version_label, versionName)
    }

    override fun onResume() {
        super.onResume()
        val granted = BinaryClockWidgetProvider.canScheduleExactAlarms(this)
        exactAlarmCard.visibility = if (granted) View.GONE else View.VISIBLE
        BinaryClockWidgetProvider.updateAll(this)
    }

    private fun setUpColorPicker() {
        val grid = findViewById<GridLayout>(R.id.color_grid)
        val options = listOf(ClockColors.AUTO to R.string.color_auto) +
            ClockColors.PRESETS.map { it.color to it.name }
        for ((color, name) in options) {
            val swatch = ColorSwatchView(this, color).apply {
                contentDescription = getString(name)
                setOnClickListener { selectColor(color) }
            }
            val params = GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED),
                GridLayout.spec(GridLayout.UNDEFINED, 1f),
            ).apply {
                width = 0
                setGravity(Gravity.CENTER)
            }
            grid.addView(swatch, params)
            swatches += swatch
        }
        showSelectedColor()
    }

    private fun selectColor(color: Int) {
        ClockColors.select(this, color)
        showSelectedColor()
        preview.invalidate()
        BinaryClockWidgetProvider.updateAll(this)
    }

    private fun showSelectedColor() {
        val selected = ClockColors.selected(this)
        swatches.forEach { it.isSelected = it.color == selected }

        val preset = ClockColors.PRESETS.firstOrNull { it.color == selected }
        findViewById<TextView>(R.id.color_name).setText(preset?.name ?: R.string.color_auto)
        findViewById<TextView>(R.id.color_hint).setText(
            when {
                preset != null -> R.string.color_custom_hint
                DynamicColors.isDynamicColorAvailable() -> R.string.color_auto_hint_dynamic
                else -> R.string.color_auto_hint_static
            }
        )
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
