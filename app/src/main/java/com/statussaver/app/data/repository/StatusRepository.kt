package com.statussaver.app.data.repository

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import com.statussaver.app.data.model.StatusMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest

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

    fun getGalleryDir(): File {
        val pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val dir = File(pictures, "Status Update")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    suspend fun getAllStatuses(): List<StatusMedia> = withContext(Dispatchers.IO) {
        val results = mutableListOf<StatusMedia>()

        for (dir in possibleStatusDirs) {
            if (!dir.exists() || !dir.isDirectory || !dir.canRead()) continue

            dir.listFiles()?.forEach { file ->
                if (file.name.startsWith(".") || !file.isFile || file.length() < 100) return@forEach
                StatusMedia.fromFile(file)?.let { results.add(it) }
            }
        }

        results.distinctBy { it.path }.sortedByDescending { it.lastModified }
    }

    suspend fun getImages(): List<StatusMedia> = getAllStatuses().filter { it.isImage }
    suspend fun getVideos(): List<StatusMedia> = getAllStatuses().filter { it.isVideo }

    private fun getFileHash(file: File): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun saveToGallery(media: StatusMedia): Result<File> = withContext(Dispatchers.IO) {
        try {
            val galleryDir = getGalleryDir()
            val newFileHash = getFileHash(media.file)
            
            // Check if exact same content already exists
            val existingFiles = galleryDir.listFiles() ?: emptyArray()
            for (existing in existingFiles) {
                if (existing.isFile && existing.length() == media.file.length()) {
                    if (getFileHash(existing) == newFileHash) {
                        return@withContext Result.success(existing)
                    }
                }
            }

            // Unique name
            val timestamp = System.currentTimeMillis()
            val extension = media.name.substringAfterLast('.', "jpg")
            val uniqueName = "WAE_${timestamp}.${extension}"
            val targetFile = File(galleryDir, uniqueName)

            FileInputStream(media.file).use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output, bufferSize = 8192)
                }
            }

            MediaScannerConnection.scanFile(context, arrayOf(targetFile.absolutePath), null, null)
            Result.success(targetFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFromGallery(media: StatusMedia): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val galleryDir = getGalleryDir()
            val targetHash = getFileHash(media.file)
            
            galleryDir.listFiles()?.forEach { file ->
                if (file.isFile && file.length() == media.file.length() && getFileHash(file) == targetHash) {
                    file.delete()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun clearAppCache(): Long {
        val cacheDir = context.cacheDir
        var bytes = 0L
        cacheDir.listFiles()?.forEach { file ->
            bytes += file.length()
            file.deleteRecursively()
        }
        return bytes
    }

    fun getCacheSize(): Long {
        val cacheDir = context.cacheDir
        var bytes = 0L
        cacheDir.listFiles()?.forEach { file ->
            bytes += file.length()
        }
        return bytes
    }

    fun hasAccessibleStatusFolder(): Boolean {
        return possibleStatusDirs.any { it.exists() && it.canRead() }
    }
}
