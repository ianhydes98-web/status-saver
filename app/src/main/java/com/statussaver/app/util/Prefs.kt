package com.statussaver.app.util

import android.content.Context

object Prefs {

    private const val PREFS_NAME = "status_saver_prefs"
    private const val KEY_AUTO_SAVE = "auto_save_enabled"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isAutoSaveEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AUTO_SAVE, true)

    fun setAutoSaveEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_AUTO_SAVE, enabled).apply()
    }
}
