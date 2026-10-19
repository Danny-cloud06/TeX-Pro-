package com.example.ui.screens.studio.composition

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameplayPreset
import com.example.ui.theme.CueBlack
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameplayPresetCanvas(
    preset: GameplayPreset,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gameplay_motion")
    val motionProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isPlaying) 2400 else 60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "motion"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isPlaying) 600 else 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(modifier = modifier.fillMaxSize().background(CueBlack)) {
        when (preset) {
            GameplayPreset.CYBER_RUNNER -> {
                CyberRunnerCanvas(motionProgress = motionProgress, pulseScale = pulseScale)
            }
            GameplayPreset.SPACE_ODYSSEY -> {
                SpaceOdysseyCanvas(motionProgress = motionProgress, pulseScale = pulseScale)
            }
            GameplayPreset.VOXEL_ADVENTURE -> {
                VoxelAdventureCanvas(motionProgress = motionProgress, pulseScale = pulseScale)
            }
        }

        // Gameplay HUD Watermark / FPS Telemetry
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${preset.title.uppercase()} • 60 FPS • REC 1080p",
                color = preset.themeColor,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CyberRunnerCanvas(motionProgress: Float, pulseScale: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val horizonY = h * 0.52f

        // 1. Synthwave Sky Gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0D0221),
                    Color(0xFF261447),
                    Color(0xFF7209B7),
                    Color(0xFFF72585)
                ),
                startY = 0f,
                endY = horizonY
            ),
            size = Size(w, horizonY)
        )

        // 2. Horizon Glowing Neon Sun
        val sunRadius = w * 0.18f * pulseScale
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFEE55), Color(0xFFFF007F), Color(0x00FF007F)),
                center = Offset(w * 0.5f, horizonY),
                radius = sunRadius * 1.5f
            ),
            radius = sunRadius,
            center = Offset(w * 0.5f, horizonY)
        )

        // Sun Horizontal Scanlines
        for (i in 0..7) {
            val lineY = horizonY - sunRadius * 0.7f + i * (sunRadius * 0.16f)
            if (lineY < horizonY) {
                drawLine(
                    color = Color(0xFF261447),
                    start = Offset(w * 0.35f, lineY),
                    end = Offset(w * 0.65f, lineY),
                    strokeWidth = 3f + i * 1.5f
                )
            }
        }

        // 3. Ground Plane
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0F051D), Color(0xFF19002E)),
                startY = horizonY,
                endY = h
            ),
            topLeft = Offset(0f, horizonY),
            size = Size(w, h - horizonY)
        )

        // 4. Perspective Grid Lines (Verticals to vanishing center)
        val vanishing = Offset(w * 0.5f, horizonY)
        val gridLines = 14
        for (i in 0..gridLines) {
            val bottomX = (w / gridLines) * i
            drawLine(
                color = Color(0xFF00FF9D).copy(alpha = 0.45f),
                start = vanishing,
                end = Offset(bottomX, h),
                strokeWidth = 1.5f
            )
        }

        // 5. Moving Horizontal Grid Lines
        val horizCount = 10
        for (i in 0 until horizCount) {
            val progress = (i.toFloat() / horizCount + motionProgress) % 1f
            // Exponential curve for 3D depth perception
            val curvedY = horizonY + (h - horizonY) * (progress * progress)
            val alpha = (progress * 0.8f).coerceIn(0.1f, 0.85f)
            drawLine(
                color = Color(0xFF00E5FF).copy(alpha = alpha),
                start = Offset(0f, curvedY),
                end = Offset(w, curvedY),
                strokeWidth = 2f * progress + 0.5f
            )
        }

        // 6. Cyber Runner Vehicle Outline in Center
        val carX = w * 0.5f
        val carY = h * 0.84f
        val carWidth = w * 0.22f
        val carHeight = carWidth * 0.45f

        // Vehicle glow
        drawCircle(
            color = Color(0xFF00FF9D).copy(alpha = 0.25f),
            radius = carWidth * 0.8f,
            center = Offset(carX, carY)
        )

        // Vehicle Chassis
        val carPath = Path().apply {
            moveTo(carX - carWidth * 0.5f, carY + carHeight * 0.5f)
            lineTo(carX - carWidth * 0.4f, carY - carHeight * 0.2f)
            lineTo(carX - carWidth * 0.2f, carY - carHeight * 0.5f)
            lineTo(carX + carWidth * 0.2f, carY - carHeight * 0.5f)
            lineTo(carX + carWidth * 0.4f, carY - carHeight * 0.2f)
            lineTo(carX + carWidth * 0.5f, carY + carHeight * 0.5f)
            close()
        }
        drawPath(carPath, color = Color(0xFF0A0F1D))
        drawPath(carPath, color = Color(0xFF00FF9D), style = Stroke(width = 3f))

        // Neon taillight bar
        drawLine(
            color = Color(0xFFFF0055),
            start = Offset(carX - carWidth * 0.38f, carY + carHeight * 0.3f),
            end = Offset(carX + carWidth * 0.38f, carY + carHeight * 0.3f),
            strokeWidth = 5f
        )
    }
}

@Composable
private fun SpaceOdysseyCanvas(motionProgress: Float, pulseScale: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Deep cosmos background
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF0A1128), Color(0xFF000411)),
                center = Offset(w * 0.5f, h * 0.5f),
                radius = w * 0.8f
            )
        )

        // Drifting Starfield (Warp motion)
        val numStars = 48
        for (i in 0 until numStars) {
            val seedAngle = (i * 37.3f) % 360f
            val baseSpeed = 0.2f + (i % 5) * 0.2f
            val distProgress = ((i * 0.021f) + motionProgress * baseSpeed) % 1f
            val dist = distProgress * (w * 0.75f)
            val rad = Math.toRadians(seedAngle.toDouble())
            val starX = w * 0.5f + (cos(rad) * dist).toFloat()
            val starY = h * 0.45f + (sin(rad) * dist).toFloat()

            if (starX in 0f..w && starY in 0f..h) {
                val starSize = 1.5f + distProgress * 4.5f
                val alpha = (distProgress * 0.9f).coerceIn(0.2f, 1f)
                drawCircle(
                    color = if (i % 3 == 0) Color(0xFF00E5FF).copy(alpha = alpha) else Color.White.copy(alpha = alpha),
                    radius = starSize,
                    center = Offset(starX, starY)
                )
            }
        }

        // Alien Asteroid Hazard
        val asteroidX = (w * 0.25f + motionProgress * (w * 0.4f)) % w
        val asteroidY = h * 0.25f + sin(motionProgress * 6.28f) * 15f
        drawCircle(
            color = Color(0xFF37474F),
            radius = 28f,
            center = Offset(asteroidX, asteroidY)
        )
        drawCircle(
            color = Color(0xFFCFD8DC),
            radius = 28f,
            center = Offset(asteroidX, asteroidY),
            style = Stroke(width = 2f)
        )

        // Player Space Interceptor
        val shipX = w * 0.5f + sin(motionProgress * 6.28f * 1.5f) * 35f
        val shipY = h * 0.72f
        val shipSize = w * 0.16f

        // Thruster flame
        val flameLen = shipSize * 0.6f * pulseScale
        val flamePath = Path().apply {
            moveTo(shipX - shipSize * 0.2f, shipY + shipSize * 0.4f)
            lineTo(shipX, shipY + shipSize * 0.4f + flameLen)
            lineTo(shipX + shipSize * 0.2f, shipY + shipSize * 0.4f)
            close()
        }
        drawPath(flamePath, color = Color(0xFF00E5FF).copy(alpha = 0.85f))

        // Spaceship Body
        val shipPath = Path().apply {
            moveTo(shipX, shipY - shipSize * 0.6f)
            lineTo(shipX + shipSize * 0.45f, shipY + shipSize * 0.4f)
            lineTo(shipX, shipY + shipSize * 0.2f)
            lineTo(shipX - shipSize * 0.45f, shipY + shipSize * 0.4f)
            close()
        }
        drawPath(shipPath, color = Color(0xFF1E293B))
        drawPath(shipPath, color = Color(0xFF00E5FF), style = Stroke(width = 2.5f))

        // Shield HUD Ring
        drawCircle(
            color = Color(0xFF00E5FF).copy(alpha = 0.25f),
            radius = shipSize * 0.75f,
            center = Offset(shipX, shipY),
            style = Stroke(width = 1.5f)
        )
    }
}

@Composable
private fun VoxelAdventureCanvas(motionProgress: Float, pulseScale: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Sunny Sky
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF4FC3F7), Color(0xFF81D4FA)),
                startY = 0f,
                endY = h * 0.55f
            ),
            size = Size(w, h * 0.55f)
        )

        // Pixel Sun
        drawRect(
            color = Color(0xFFFFEB3B),
            topLeft = Offset(w * 0.75f, h * 0.08f),
            size = Size(50f, 50f)
        )

        // Drifting Pixel Cloud
        val cloudX = (w * motionProgress * 0.7f) % (w + 120f) - 60f
        drawRect(color = Color.White.copy(alpha = 0.9f), topLeft = Offset(cloudX, h * 0.14f), size = Size(90f, 26f))
        drawRect(color = Color.White.copy(alpha = 0.9f), topLeft = Offset(cloudX + 20f, h * 0.11f), size = Size(50f, 20f))

        // Rolling Voxel Green Terrain
        val groundY = h * 0.55f
        drawRect(
            color = Color(0xFF4CAF50),
            topLeft = Offset(0f, groundY),
            size = Size(w, 24f)
        )
        // Dirt under grass
        drawRect(
            color = Color(0xFF795548),
            topLeft = Offset(0f, groundY + 24f),
            size = Size(w, h - groundY - 24f)
        )

        // Isometric Voxel Blocks on Landscape
        val blockSize = w * 0.12f
        for (i in 0..6) {
            val blockX = i * blockSize * 1.3f - (motionProgress * blockSize * 2f) % (blockSize * 1.3f)
            val blockY = groundY - blockSize + sin((i + motionProgress * 4f)) * 8f
            drawVoxelCube(
                origin = Offset(blockX, blockY),
                size = blockSize * 0.9f,
                topColor = Color(0xFF8BC34A),
                leftColor = Color(0xFF689F38),
                rightColor = Color(0xFF558B2F)
            )
        }

        // Hotbar Overlay at bottom
        val hotbarW = w * 0.75f
        val hotbarH = 38f
        val hotbarX = (w - hotbarW) * 0.5f
        val hotbarY = h * 0.90f

        drawRect(
            color = Color(0xFF212121).copy(alpha = 0.85f),
            topLeft = Offset(hotbarX, hotbarY),
            size = Size(hotbarW, hotbarH)
        )
        drawRect(
            color = Color(0xFFBDBDBD),
            topLeft = Offset(hotbarX, hotbarY),
            size = Size(hotbarW, hotbarH),
            style = Stroke(width = 2f)
        )

        // Hotbar 7 slots
        val slotW = hotbarW / 7f
        for (s in 0 until 7) {
            val isSelected = s == 2
            val sx = hotbarX + s * slotW
            drawRect(
                color = if (isSelected) Color(0xFFFFD54F).copy(alpha = 0.5f) else Color.Transparent,
                topLeft = Offset(sx, hotbarY),
                size = Size(slotW, hotbarH)
            )
            drawRect(
                color = if (isSelected) Color(0xFFFFD54F) else Color(0xFF616161),
                topLeft = Offset(sx, hotbarY),
                size = Size(slotW, hotbarH),
                style = Stroke(width = if (isSelected) 3f else 1f)
            )
        }
    }
}

private fun DrawScope.drawVoxelCube(
    origin: Offset,
    size: Float,
    topColor: Color,
    leftColor: Color,
    rightColor: Color
) {
    val halfW = size * 0.5f
    val quarterH = size * 0.28f

    // Top face
    val topPath = Path().apply {
        moveTo(origin.x + halfW, origin.y)
        lineTo(origin.x + size, origin.y + quarterH)
        lineTo(origin.x + halfW, origin.y + quarterH * 2f)
        lineTo(origin.x, origin.y + quarterH)
        close()
    }
    drawPath(topPath, color = topColor)

    // Left face
    val leftPath = Path().apply {
        moveTo(origin.x, origin.y + quarterH)
        lineTo(origin.x + halfW, origin.y + quarterH * 2f)
        lineTo(origin.x + halfW, origin.y + quarterH * 2f + size * 0.5f)
        lineTo(origin.x, origin.y + quarterH + size * 0.5f)
        close()
    }
    drawPath(leftPath, color = leftColor)

    // Right face
    val rightPath = Path().apply {
        moveTo(origin.x + halfW, origin.y + quarterH * 2f)
        lineTo(origin.x + size, origin.y + quarterH)
        lineTo(origin.x + size, origin.y + quarterH + size * 0.5f)
        lineTo(origin.x + halfW, origin.y + quarterH * 2f + size * 0.5f)
        close()
    }
    drawPath(rightPath, color = rightColor)
}
