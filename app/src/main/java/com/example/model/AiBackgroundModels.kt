package com.example.model

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class BackgroundMode(val label: String, val badgeLabel: String, val description: String) {
    ORIGINAL("Original", "AI: OFF", "Direct camera feed without alterations"),
    BLUR("Depth Blur", "AI: BLUR", "Soft cinematic depth of field behind presenter"),
    GREEN_SCREEN("Green Screen", "AI: CHROMA", "Broadcast chroma key for video editing"),
    SOLID_COLOR("Solid Color", "AI: SOLID", "Minimalist studio backdrops"),
    VIRTUAL_STUDIO("Virtual Set", "AI: VIRTUAL", "Curated broadcast newsroom & creator sets"),
    CUSTOM_IMAGE("Custom Photo", "AI: CUSTOM", "User-selected photo from device gallery")
}

enum class BlurIntensity(val label: String, val blurRadius: Dp, val blurSigma: Float) {
    LIGHT("Light", 8.dp, 8f),
    MEDIUM("Medium", 18.dp, 18f),
    STRONG("Strong", 32.dp, 32f)
}

enum class VirtualStudioPreset(
    val title: String,
    val subtitle: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color
) {
    NEWSROOM(
        title = "Prime Newsroom",
        subtitle = "Modern anchor desk with deep blue digital panels",
        primaryColor = Color(0xFF0A192F),
        secondaryColor = Color(0xFF1E3A8A),
        accentColor = Color(0xFF00E676)
    ),
    EXECUTIVE_LIBRARY(
        title = "Executive Library",
        subtitle = "Rich warm mahogany wood & scholarly shelves",
        primaryColor = Color(0xFF1C130D),
        secondaryColor = Color(0xFF382315),
        accentColor = Color(0xFFD97706)
    ),
    CREATOR_LOFT(
        title = "Creator Loft",
        subtitle = "Minimal brick wall with warm ambient rim lighting",
        primaryColor = Color(0xFF18181B),
        secondaryColor = Color(0xFF27272A),
        accentColor = Color(0xFF10B981)
    ),
    CYBERPUNK_STUDIO(
        title = "Cyber Neon",
        subtitle = "Atmospheric dark tech studio with dual-tone neon",
        primaryColor = Color(0xFF09090B),
        secondaryColor = Color(0xFF3B0764),
        accentColor = Color(0xFF06B6D4)
    ),
    MINIMAL_STUDIO(
        title = "Minimal Studio",
        subtitle = "Clean architectural slate backdrop with subtle spotlight",
        primaryColor = Color(0xFF1E293B),
        secondaryColor = Color(0xFF334155),
        accentColor = Color(0xFF38BDF8)
    )
}

data class SolidColorOption(
    val name: String,
    val color: Color
)

object SolidColorPresets {
    val options = listOf(
        SolidColorOption("Chroma Green", Color(0xFF00FF00)),
        SolidColorOption("Chroma Blue", Color(0xFF0044FF)),
        SolidColorOption("Studio Charcoal", Color(0xFF15181E)),
        SolidColorOption("Clean White", Color(0xFFF8FAFC)),
        SolidColorOption("Midnight Slate", Color(0xFF0F172A)),
        SolidColorOption("Sunset Amber", Color(0xFFFF7A00)),
        SolidColorOption("Broadcast Magenta", Color(0xFFD946EF))
    )
}

data class AiBackgroundSettings(
    val mode: BackgroundMode = BackgroundMode.ORIGINAL,
    val blurIntensity: BlurIntensity = BlurIntensity.MEDIUM,
    val virtualStudio: VirtualStudioPreset = VirtualStudioPreset.NEWSROOM,
    val solidColor: Color = Color(0xFF00FF00),
    val customImageUri: Uri? = null,
    val customImageBitmap: Bitmap? = null,
    val edgeConfidenceThreshold: Float = 0.55f,
    val isAiEnabled: Boolean = false
) {
    val isEffectActive: Boolean
        get() = isAiEnabled && mode != BackgroundMode.ORIGINAL

    val statusBadgeText: String
        get() = if (!isAiEnabled || mode == BackgroundMode.ORIGINAL) {
            "AI: OFF"
        } else {
            when (mode) {
                BackgroundMode.ORIGINAL -> "AI: OFF"
                BackgroundMode.BLUR -> "AI: BLUR (${blurIntensity.label.uppercase()})"
                BackgroundMode.GREEN_SCREEN -> "AI: CHROMA GREEN"
                BackgroundMode.SOLID_COLOR -> "AI: SOLID"
                BackgroundMode.VIRTUAL_STUDIO -> "AI: ${virtualStudio.title.uppercase()}"
                BackgroundMode.CUSTOM_IMAGE -> "AI: CUSTOM PHOTO"
            }
        }
}
