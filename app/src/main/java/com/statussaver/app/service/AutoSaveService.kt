package com.statussaver.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.FileObserver
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.statussaver.app.MainActivity
import com.statussaver.app.data.model.StatusMedia
import com.statussaver.app.data.repository.StatusRepository
import com.statussaver.app.util.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File

class AutoSaveService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val repository by lazy { StatusRepository(this) }
    private val observers = mutableListOf<FileObserver>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Prefs.isAutoSaveEnabled(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, createNotification())
        startWatching()

        return START_STICKY
    }

    private fun startWatching() {
        stopWatching()

        val dirs = getStatusDirectories()
        for (dir in dirs) {
            if (!dir.exists()) continue

            // Listen to CREATE, MOVED_TO, and CLOSE_WRITE (which triggers when WhatsApp finishes saving the image/video)
            val observer = object : FileObserver(dir.absolutePath, CREATE or MOVED_TO or CLOSE_WRITE) {
                override fun onEvent(event: Int, path: String?) {
                    if (path == null) return
                    if (!Prefs.isAutoSaveEnabled(this@AutoSaveService)) return
                    val file = File(dir, path)
                    
                    // Ignore hidden files and tiny temporary files
                    if (file.name.startsWith(".") || !file.isFile || file.length() < 100) return

                    serviceScope.launch {
                        try {
                            // Reduced delay from 800ms to 200ms to catch images faster
                            Thread.sleep(200) 
                            if (file.exists() && file.length() > 100) {
                                val media = StatusMedia.fromFile(file) ?: return@launch
                                val result = repository.saveToGallery(media)
                                if (result.isSuccess) {
                                    updateNotification("Saved: ${media.name}")
                                }
                            }
                        } catch (_: Exception) { }
                    }
                }
            }
            observer.startWatching()
            observers.add(observer)
        }
    }

    private fun getStatusDirectories(): List<File> {
        val base = Environment.getExternalStorageDirectory()
        return listOf(
            File(base, "WhatsApp/Media/.Statuses"),
            File(base, "WhatsApp Business/Media/.Statuses"),
            File(base, "Android/media/com.whatsapp/WhatsApp/Media/.Statuses"),
            File(base, "Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses")
        )
    }

    private fun stopWatching() {
        observers.forEach { it.stopWatching() }
        observers.clear()
    }

    private fun createNotification(contentText: String = "Watching for new statuses…"): Notification {
        createChannel()

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WAE – Auto Save")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, createNotification(text))
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Auto Save",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when auto-saving WhatsApp statuses"
                setShowBadge(false)
            }
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopWatching()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "auto_save_channel"
        private const val NOTIFICATION_ID = 1001

        private var isRunning = false

        fun start(context: android.content.Context) {
            if (!Prefs.isAutoSaveEnabled(context)) return
            val intent = Intent(context, AutoSaveService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            isRunning = true
        }

        fun startSafely(context: android.content.Context) {
            if (isRunning) return
            try {
                start(context)
            } catch (_: Exception) { }
        }

        fun stop(context: android.content.Context) {
            context.stopService(Intent(context, AutoSaveService::class.java))
            isRunning = false
        }
    }
}
