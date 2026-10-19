package com.example.model

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class CameraLensFacing {
    FRONT,
    BACK
}

enum class RecordingQuality(val label: String, val resolutionLabel: String) {
    STANDARD("Standard (720p)", "1280x720"),
    HIGH("High Quality (1080p)", "1920x1080")
}

data class StudioSettings(
    val lensFacing: CameraLensFacing = CameraLensFacing.FRONT,
    val isAudioEnabled: Boolean = true,
    val isPreviewMirrored: Boolean = true,
    val recordingQuality: RecordingQuality = RecordingQuality.STANDARD,
    val countdownSeconds: Int = 3,
    val prompterOpacity: Float = 0.75f
)

data class RecordedVideo(
    val file: File,
    val durationSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedDuration: String
        get() {
            val min = durationSeconds / 60
            val sec = durationSeconds % 60
            return String.format(Locale.US, "%02d:%02d", min, sec)
        }

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    val fileSizeMb: String
        get() {
            val bytes = if (file.exists()) file.length() else 0L
            val mb = bytes / (1024f * 1024f)
            return String.format(Locale.US, "%.1f MB", mb)
        }
}
