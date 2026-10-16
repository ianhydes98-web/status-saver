package com.statussaver.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.statussaver.app.util.Prefs

/**
 * Restarts the auto-save service after device reboot if the user had it enabled.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            if (Prefs.isAutoSaveEnabled(context)) {
                AutoSaveService.start(context)
            }
        }
    }
}
