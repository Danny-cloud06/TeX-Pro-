package com.example.ui.screens.studio

import android.graphics.Bitmap
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiBackgroundSettings
import com.example.model.BackgroundMode
import com.example.model.CameraLensFacing
import com.example.ui.theme.CueBlack
import com.example.ui.theme.CueBorder
import com.example.ui.theme.CueGreen
import com.example.ui.theme.CueSurface
import com.example.ui.theme.CueTextMuted
import com.example.ui.theme.CueTextPrimary
import com.example.ui.theme.CueTextSecondary

/**
 * Composite AI Background Processing View.
 * Renders the chosen background effect (Chroma Green Screen, Depth Blur,
 * Solid Colors, Virtual Studio Sets, or Custom Photos) with realistic
 * presenter foreground compositing.
 */
@Composable
fun StudioAiBackgroundView(
    aiSettings: AiBackgroundSettings,
    lensFacing: CameraLensFacing,
    isHardwareFeedActive: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("studio_ai_background_view")
    ) {
        if (aiSettings.isEffectActive) {
            // 1. Virtual Background Layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("ai_background_layer")
            ) {
                when (aiSettings.mode) {
                    BackgroundMode.ORIGINAL -> {
                        // Direct camera pass-through
                    }

                    BackgroundMode.GREEN_SCREEN -> {
                        ChromaGreenScreenBackdrop()
                    }

                    BackgroundMode.SOLID_COLOR -> {
                        SolidColorBackdrop(color = aiSettings.solidColor)
                    }

                    BackgroundMode.VIRTUAL_STUDIO -> {
                        VirtualStudioBackdrop(preset = aiSettings.virtualStudio)
                    }

                    BackgroundMode.CUSTOM_IMAGE -> {
                        CustomImageBackdrop(bitmap = aiSettings.customImageBitmap)
                    }

                    BackgroundMode.BLUR -> {
                        // Blurred studio background layer
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .blur(aiSettings.blurIntensity.blurRadius)
                        ) {
                            VirtualStudioBackdrop(preset = aiSettings.virtualStudio)
                        }
                    }
                }
            }

            // 2. Foreground Presenter Layer
            // Renders the presenter silhouette extracted via AI segmentation
            PresenterForegroundLayer(
                aiSettings = aiSettings,
                lensFacing = lensFacing,
                modifier = Modifier.fillMaxSize()
            )

            // 3. Top-Right AI Mode Status Badge
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 64.dp, end = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CueSurface.copy(alpha = 0.90f))
                    .border(1.dp, CueGreen.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = CueGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = aiSettings.statusBadgeText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = CueGreen
                )
            }
        }
    }
}

@Composable
fun ChromaGreenScreenBackdrop(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF00FF00))
    ) {
        // Subtle broadcast chroma key alignment grid
        val step = 80f
        val gridColor = Color(0xFF00DD00).copy(alpha = 0.45f)
        var x = 0f
        while (x < size.width) {
            drawLine(gridColor, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 1f)
            x += step
        }
        var y = 0f
        while (y < size.height) {
            drawLine(gridColor, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1f)
            y += step
        }
    }
}

@Composable
fun SolidColorBackdrop(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Delicate studio ambient vignette
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f)),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.width * 0.9f
                )
            )
        }
    }
}

@Composable
fun CustomImageBackdrop(bitmap: Bitmap?, modifier: Modifier = Modifier) {
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Custom Studio Backdrop",
            contentScale = ContentScale.Crop,
            modifier = modifier.fillMaxSize()
        )
    } else {
        // Placeholder when custom image is not yet picked
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = CueGreen,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No Custom Photo Selected",
                    style = MaterialTheme.typography.titleMedium,
                    color = CueTextPrimary
                )
                Text(
                    text = "Tap AI Effects below to select a photo from your gallery",
                    style = MaterialTheme.typography.bodySmall,
                    color = CueTextSecondary
                )
            }
        }
    }
}

@Composable
fun PresenterForegroundLayer(
    aiSettings: AiBackgroundSettings,
    lensFacing: CameraLensFacing,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "presenter_pulse")
    val breathingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Presenter dimensions and positioning (Chest-up broadcaster portrait)
        val centerX = w * 0.5f
        val headRadius = w * 0.17f
        val headCenterY = h * 0.44f + breathingOffset
        val shoulderY = headCenterY + headRadius * 1.05f

        // Subtle rim light glow around the presenter reacting to active mode
        val rimGlowColor = when (aiSettings.mode) {
            BackgroundMode.GREEN_SCREEN -> Color(0xFF00FF00).copy(alpha = 0.25f)
            BackgroundMode.VIRTUAL_STUDIO -> aiSettings.virtualStudio.accentColor.copy(alpha = 0.35f)
            BackgroundMode.SOLID_COLOR -> aiSettings.solidColor.copy(alpha = 0.35f)
            else -> CueGreen.copy(alpha = 0.3f)
        }

        // 1. Shoulders & Torso Silhouette
        val torsoPath = Path().apply {
            moveTo(centerX - w * 0.45f, h)
            cubicTo(
                centerX - w * 0.42f, shoulderY + h * 0.10f,
                centerX - w * 0.28f, shoulderY,
                centerX - headRadius * 0.70f, shoulderY - 8f
            )
            lineTo(centerX + headRadius * 0.70f, shoulderY - 8f)
            cubicTo(
                centerX + w * 0.28f, shoulderY,
                centerX + w * 0.42f, shoulderY + h * 0.10f,
                centerX + w * 0.45f, h
            )
            close()
        }

        // Draw Torso with smooth studio lighting gradient
        drawPath(
            path = torsoPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF2E384D),
                    Color(0xFF1E2536),
                    Color(0xFF111622)
                ),
                startY = shoulderY,
                endY = h
            )
        )

        // 2. Neck
        val neckWidth = headRadius * 0.65f
        val neckHeight = headRadius * 0.5f
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFD4A373), Color(0xFFBC8A5F))
            ),
            topLeft = Offset(centerX - neckWidth / 2f, headCenterY + headRadius * 0.65f),
            size = Size(neckWidth, neckHeight)
        )

        // 3. Head & Face
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFE8B298),
                    Color(0xFFD4A373),
                    Color(0xFFBC8A5F)
                ),
                center = Offset(centerX, headCenterY - 4f),
                radius = headRadius
            ),
            radius = headRadius,
            center = Offset(centerX, headCenterY)
        )

        // 4. Hair / Crown
        val hairPath = Path().apply {
            moveTo(centerX - headRadius * 1.05f, headCenterY - 6f)
            cubicTo(
                centerX - headRadius * 1.15f, headCenterY - headRadius * 1.25f,
                centerX + headRadius * 1.15f, headCenterY - headRadius * 1.25f,
                centerX + headRadius * 1.05f, headCenterY - 6f
            )
            cubicTo(
                centerX + headRadius * 0.8f, headCenterY - headRadius * 0.6f,
                centerX - headRadius * 0.8f, headCenterY - headRadius * 0.6f,
                centerX - headRadius * 1.05f, headCenterY - 6f
            )
            close()
        }
        drawPath(
            path = hairPath,
            color = Color(0xFF1F1A18)
        )

        // 5. Broadcaster Lapel Collar
        val collarPath = Path().apply {
            moveTo(centerX, shoulderY + 45f)
            lineTo(centerX - 35f, shoulderY - 8f)
            lineTo(centerX, shoulderY + 10f)
            lineTo(centerX + 35f, shoulderY - 8f)
            close()
        }
        drawPath(collarPath, color = Color(0xFFE2E8F0))

        // 6. Broadcaster Studio Lavalier Mic Badge
        drawCircle(
            color = Color(0xFF0F172A),
            radius = 6f,
            center = Offset(centerX, shoulderY + 32f)
        )
        drawLine(
            color = Color(0xFF475569),
            start = Offset(centerX, shoulderY + 32f),
            end = Offset(centerX, shoulderY + 65f),
            strokeWidth = 2f
        )

        // 7. Subtle Rim Light Edge on Presenter
        drawPath(
            path = torsoPath,
            color = rimGlowColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f)
        )
    }
}
