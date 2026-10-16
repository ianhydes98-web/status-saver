package com.statussaver.app.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.statussaver.app.data.model.StatusMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatusRepository(private val context: Context) {

    private val possibleStatusDirs: List<File> by lazy {
        val base = Environment.getExternalStorageDirectory()
        listOf(
            File(base, "WhatsApp/Media/.Statuses"),
            File(base, "WhatsApp Business/Media/.Statuses"),
            File(base, "Android/media/com.whatsapp/WhatsApp/Media/.Statuses"),
            File(base, "Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses"),
            File(base, "Android/media/com.whatsapp/WhatsApp/Media/Statuses"),
            File(base, "WhatsApp/Media/Statuses")
        )
    }

    suspend fun getAllStatuses(): List<StatusMedia> = withContext(Dispatchers.IO) {
        val results = mutableListOf<StatusMedia>()

        for (dir in possibleStatusDirs) {
            if (!dir.exists() || !dir.isDirectory || !dir.canRead()) continue

            dir.listFiles()?.forEach { file ->
                if (file.isFile && file.canRead() && file.length() > 0) {
                    StatusMedia.fromFile(file)?.let { results.add(it) }
                }
            }
        }

        results
            .distinctBy { it.path }
            .sortedByDescending { it.lastModified }
    }

    suspend fun getImages(): List<StatusMedia> =
        getAllStatuses().filter { it.isImage }

    suspend fun getVideos(): List<StatusMedia> =
        getAllStatuses().filter { it.isVideo }

    suspend fun saveToGallery(media: StatusMedia): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val collection = if (media.isVideo) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val extension = media.name.substringAfterLast('.', "jpg")
            val baseName = media.name.substringBeforeLast('.', media.name)
            val uniqueName = "${baseName}_${timestamp}.${extension}"

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, uniqueName)
                put(MediaStore.MediaColumns.MIME_TYPE, if (media.isVideo) "video/mp4" else "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Status Update")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val uri = resolver.insert(collection, values)
                ?: return@withContext Result.failure(Exception("Failed to create MediaStore entry"))

            resolver.openOutputStream(uri)?.use { output ->
                FileInputStream(media.file).use { input ->
                    input.copyTo(output, bufferSize = 8192)
                }
            } ?: return@withContext Result.failure(Exception("Failed to open output stream"))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun hasAccessibleStatusFolder(): Boolean {
        return possibleStatusDirs.any { it.exists() && it.canRead() }
    }
}
