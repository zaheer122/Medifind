package com.app.medifindfinal

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class MediFindApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Apply the saved theme preference when the app starts
        val sharedPref = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        val savedTheme = sharedPref.getInt("theme_setting", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        val nightMode = when (savedTheme) {
            0 -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            1 -> AppCompatDelegate.MODE_NIGHT_NO
            2 -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}