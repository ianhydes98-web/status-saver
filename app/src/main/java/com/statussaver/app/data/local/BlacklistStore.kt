package com.statussaver.app.data.local

import android.content.Context

/**
 * Stores file paths that the user chose to "Remove from App Only".
 * These files stay in the Gallery and in WhatsApp's .Statuses folder,
 * but the app hides them from its lists.
 */
object BlacklistStore {

    private const val PREFS_NAME = "status_saver_blacklist"
    private const val KEY_PATHS = "hidden_paths"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getAll(context: Context): Set<String> {
        return prefs(context).getStringSet(KEY_PATHS, emptySet()) ?: emptySet()
    }

    fun add(context: Context, path: String) {
        val current = getAll(context).toMutableSet()
        current.add(path)
        prefs(context).edit().putStringSet(KEY_PATHS, current).apply()
    }

    fun addAll(context: Context, paths: Collection<String>) {
        val current = getAll(context).toMutableSet()
        current.addAll(paths)
        prefs(context).edit().putStringSet(KEY_PATHS, current).apply()
    }

    fun remove(context: Context, path: String) {
        val current = getAll(context).toMutableSet()
        current.remove(path)
        prefs(context).edit().putStringSet(KEY_PATHS, current).apply()
    }

    fun clear(context: Context) {
        prefs(context).edit().remove(KEY_PATHS).apply()
    }
}
