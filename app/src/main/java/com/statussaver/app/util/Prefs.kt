package com.statussaver.app.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Ultra-lightweight preference helper.
 * No DataStore overhead — perfect for a simple toggle.
 */
object Prefs {
    private const val NAME = "status_saver_prefs"
    private const val KEY_AUTO_SAVE = "auto_save_enabled"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun isAutoSaveEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AUTO_SAVE, false)

    fun setAutoSaveEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_AUTO_SAVE, enabled).apply()
    }
}
