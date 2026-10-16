package com.statussaver.app.data.model

import java.io.File

/**
 * Represents a single WhatsApp status media item.
 * Immutable data class for thread-safety and Compose stability.
 */
data class StatusMedia(
    val id: String,
    val file: File,
    val type: MediaType,
    val lastModified: Long,
    val size: Long
) {
    val name: String get() = file.name
    val path: String get() = file.absolutePath
    val isVideo: Boolean get() = type == MediaType.VIDEO
    val isImage: Boolean get() = type == MediaType.IMAGE

    enum class MediaType {
        IMAGE, VIDEO
    }

    companion object {
        fun fromFile(file: File): StatusMedia? {
            val name = file.name.lowercase()
            val type = when {
                name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                name.endsWith(".png") || name.endsWith(".webp") -> MediaType.IMAGE
                name.endsWith(".mp4") || name.endsWith(".3gp") ||
                name.endsWith(".mkv") -> MediaType.VIDEO
                else -> return null
            }
            return StatusMedia(
                id = file.absolutePath.hashCode().toString(),
                file = file,
                type = type,
                lastModified = file.lastModified(),
                size = file.length()
            )
        }
    }
}
