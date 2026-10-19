package com.example.model

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class CompositionSourceType(val label: String) {
    GAMEPLAY_PRESET("Preset Gameplay"),
    CUSTOM_VIDEO("Custom Video"),
    CUSTOM_IMAGE("Custom Image")
}

enum class GameplayPreset(
    val title: String,
    val subtitle: String,
    val themeColor: Color,
    val description: String
) {
    CYBER_RUNNER(
        title = "Neon Cyber Runner",
        subtitle = "Sci-fi Retro Synthwave",
        themeColor = Color(0xFF00FF9D),
        description = "High-speed arcade vehicle with neon horizon grid, cyber towers, and audio pulse."
    ),
    SPACE_ODYSSEY(
        title = "Space Odyssey 60FPS",
        subtitle = "Galactic Fighter Action",
        themeColor = Color(0xFF00E5FF),
        description = "Deep space starfield with asteroid dodging, laser shields, and dynamic HUD telemetry."
    ),
    VOXEL_ADVENTURE(
        title = "Voxel Block Realm",
        subtitle = "Open World Sandbox",
        themeColor = Color(0xFFFFB300),
        description = "Procedural cubic landscape with drifting clouds, daylight cycle, and inventory bar."
    )
}

enum class PersonLayerShape(val label: String) {
    CUTOUT("AI Cutout"),
    ROUNDED_CARD("PiP Box"),
    CIRCLE("Circle Bubble")
}

enum class LayerBorderAccent(val label: String, val color: Color, val strokeWidth: Dp) {
    NONE("None", Color.Transparent, 0.dp),
    CUE_GREEN("Emerald", Color(0xFF00FF9D), 2.5.dp),
    NEON_CYAN("Cyan Neon", Color(0xFF00E5FF), 2.5.dp),
    STUDIO_WHITE("Pure White", Color(0xFFFFFFFF), 2.dp),
    SUNSET_GOLD("Gold", Color(0xFFFFD54F), 2.5.dp)
}

enum class PersonPresetPosition(val label: String, val targetOffset: Offset) {
    BOTTOM_RIGHT("Bottom Right", Offset(0.68f, 0.68f)),
    BOTTOM_LEFT("Bottom Left", Offset(0.06f, 0.68f)),
    TOP_RIGHT("Top Right", Offset(0.68f, 0.12f)),
    TOP_LEFT("Top Left", Offset(0.06f, 0.12f)),
    CENTER("Center Stage", Offset(0.36f, 0.40f))
}

data class BackgroundMedia(
    val sourceType: CompositionSourceType = CompositionSourceType.GAMEPLAY_PRESET,
    val uri: Uri? = null,
    val bitmap: Bitmap? = null,
    val fileName: String = "Cyber_Runner_60FPS.mp4",
    val durationSeconds: Int = 180,
    val gameplayPreset: GameplayPreset = GameplayPreset.CYBER_RUNNER,
    val resolutionLabel: String = "1080p 60fps"
)

data class CompositionConfig(
    val backgroundMedia: BackgroundMedia = BackgroundMedia(),
    val personNormalizedX: Float = 0.68f,
    val personNormalizedY: Float = 0.68f,
    val personScale: Float = 0.36f, // 20% to 80% screen width
    val personShape: PersonLayerShape = PersonLayerShape.CUTOUT,
    val borderAccent: LayerBorderAccent = LayerBorderAccent.CUE_GREEN,
    val isPersonMirrored: Boolean = true,
    val isPersonVisible: Boolean = true,
    val isLiveCameraSource: Boolean = true,
    
    // Video Playback Controls
    val isVideoPlaying: Boolean = true,
    val videoPositionSeconds: Int = 24,
    val isVideoMuted: Boolean = false,
    val isVideoLooping: Boolean = true,
    
    // Teleprompter Integration
    val isPrompterVisible: Boolean = true,
    val prompterOpacity: Float = 0.75f,
    val prompterSpeedWpm: Int = 130,
    val isPrompterScrolling: Boolean = false
) {
    val isCustomVideo: Boolean
        get() = backgroundMedia.sourceType == CompositionSourceType.CUSTOM_VIDEO && backgroundMedia.uri != null
        
    val isCustomImage: Boolean
        get() = backgroundMedia.sourceType == CompositionSourceType.CUSTOM_IMAGE
}

data class CompositionExportResult(
    val file: java.io.File,
    val durationSeconds: Int,
    val title: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedDuration: String
        get() {
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            return String.format("%02d:%02d", mins, secs)
        }
}
