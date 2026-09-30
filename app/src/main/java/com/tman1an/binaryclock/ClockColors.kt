package com.tman1an.binaryclock

import android.content.Context
import androidx.annotation.StringRes

/**
 * The user's clock colour choice. [AUTO] follows the theme (Material You on Android 12+,
 * the default teal on older versions); any other value is an opaque ARGB preset colour.
 */
object ClockColors {

    const val AUTO = 0

    data class Preset(val color: Int, @StringRes val name: Int)

    val PRESETS = listOf(
        Preset(0xFF00ACC1.toInt(), R.string.color_teal),
        Preset(0xFF1E88E5.toInt(), R.string.color_blue),
        Preset(0xFF5C6BC0.toInt(), R.string.color_indigo),
        Preset(0xFF8E24AA.toInt(), R.string.color_purple),
        Preset(0xFFD81B60.toInt(), R.string.color_pink),
        Preset(0xFFE53935.toInt(), R.string.color_red),
        Preset(0xFFFB8C00.toInt(), R.string.color_orange),
        Preset(0xFFFFB300.toInt(), R.string.color_amber),
        Preset(0xFF43A047.toInt(), R.string.color_green),
    )

    private const val PREFS = "clock_colors"
    private const val KEY_ACCENT = "accent"

    /** The saved choice: [AUTO] or a preset colour. */
    fun selected(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_ACCENT, AUTO)

    fun select(context: Context, color: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_ACCENT, color).apply()
    }

    /** The chosen colour, or null when the clock should follow the theme. */
    fun custom(context: Context): Int? = selected(context).takeIf { it != AUTO }
}
