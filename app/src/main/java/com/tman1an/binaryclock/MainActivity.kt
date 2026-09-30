package com.tman1an.binaryclock

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button

/**
 * Minimal launcher screen: explains how to add the widget and, on Android 12+,
 * lets the user grant the exact-alarm permission so the clock ticks on time.
 */
class MainActivity : Activity() {

    private lateinit var grantButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        grantButton = findViewById(R.id.grant_exact_alarm)
        grantButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                startActivity(
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        .setData(Uri.parse("package:$packageName"))
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val granted = BinaryClockWidgetProvider.canScheduleExactAlarms(this)
        grantButton.visibility = if (granted) View.GONE else View.VISIBLE
        BinaryClockWidgetProvider.updateAll(this)
    }
}
