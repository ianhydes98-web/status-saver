package com.statussaver.app.data.repository

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import com.statussaver.app.data.local.BlacklistStore
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

    // The folder where we save the files
    fun getGalleryDir(): File {
        val pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val dir = File(pictures, "Status Update")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    suspend fun getAllStatuses(): List<StatusMedia> = withContext(Dispatchers.IO) {
        val blacklist = BlacklistStore.getAll(context)
        val results = mutableListOf<StatusMedia>()

        for (dir in possibleStatusDirs) {
            if (!dir.exists() || !dir.isDirectory || !dir.canRead()) continue

            dir.listFiles()?.forEach { file ->
                // Skip hidden temp files and empty files
                if (file.name.startsWith(".") || !file.isFile || file.length() < 100) return@forEach
                
                // If we've hidden it, skip it
                if (file.absolutePath in blacklist) return@forEach

                StatusMedia.fromFile(file)?.let { results.add(it) }
            }
        }

        results.distinctBy { it.path }.sortedByDescending { it.lastModified }
    }

    suspend fun getImages(): List<StatusMedia> = getAllStatuses().filter { it.isImage }
    suspend fun getVideos(): List<StatusMedia> = getAllStatuses().filter { it.isVideo }

    /**
     * Generates a unique hash based on the file content. 
     * This prevents saving the same status twice, even if WhatsApp reuses the filename.
     */
    private fun generateHash(file: File): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                // Read first 64KB to generate hash quickly
                var totalRead = 0
                while (fis.read(buffer).also { bytesRead = it } != -1 && totalRead < 65536) {
                    digest.update(buffer, 0, bytesRead)
                    totalRead += bytesRead
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }

    // Direct File Copy - highly reliable
    suspend fun saveToGallery(media: StatusMedia): Result<File> = withContext(Dispatchers.IO) {
        try {
            val galleryDir = getGalleryDir()
            val newHash = generateHash(media.file)
            
            // Check if we already have a file with the exact same content
            val existingFiles = galleryDir.listFiles() ?: emptyArray()
            for (existing in existingFiles) {
                if (existing.isFile && existing.length() == media.file.length()) {
                    if (generateHash(existing) == newHash) {
                        // File already exists, do nothing
                        return@withContext Result.success(existing)
                    }
                }
            }

            // Create unique filename with timestamp
            val timestamp = System.currentTimeMillis()
            val extension = media.name.substringAfterLast('.', "jpg")
            val newName = "WAE_${timestamp}.${extension}"
            val targetFile = File(galleryDir, newName)

            // Copy file
            FileInputStream(media.file).use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output, bufferSize = 8192)
                }
            }

            // Notify Android Gallery to scan the new file
            MediaScannerConnection.scanFile(
                context,
                arrayOf(targetFile.absolutePath),
                null,
                null
            )

            Result.success(targetFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFromGallery(media: StatusMedia): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Since we renamed files with WAE_timestamp, we need to find them by content hash or size
            val galleryDir = getGalleryDir()
            val targetHash = generateHash(media.file)
            
            galleryDir.listFiles()?.forEach { file ->
                if (file.isFile && file.length() == media.file.length() && generateHash(file) == targetHash) {
                    file.delete()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun hideFromApp(path: String) {
        BlacklistStore.add(context, path)
    }

    fun hideAllFromApp(paths: Collection<String>) {
        BlacklistStore.addAll(context, paths)
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
