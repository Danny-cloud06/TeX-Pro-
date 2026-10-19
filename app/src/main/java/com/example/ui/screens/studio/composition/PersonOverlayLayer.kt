package com.example.ui.screens.studio.composition

import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.camera.CameraManager
import com.example.model.CompositionConfig
import com.example.model.LayerBorderAccent
import com.example.model.PersonLayerShape
import kotlin.math.roundToInt

@Composable
fun PersonOverlayLayer(
    config: CompositionConfig,
    containerWidthPx: Float,
    containerHeightPx: Float,
    isCameraActive: Boolean,
    cameraManager: CameraManager?,
    onPositionChanged: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!config.isPersonVisible || containerWidthPx <= 0f || containerHeightPx <= 0f) return

    val layerWidthPx = (containerWidthPx * config.personScale).coerceIn(120f, containerWidthPx * 0.90f)
    // 4:3 or 1:1 aspect ratio depending on shape
    val layerHeightPx = if (config.personShape == PersonLayerShape.CIRCLE) layerWidthPx else layerWidthPx * 1.30f

    val currentPixelX = (config.personNormalizedX * (containerWidthPx - layerWidthPx)).coerceIn(0f, containerWidthPx - layerWidthPx)
    val currentPixelY = (config.personNormalizedY * (containerHeightPx - layerHeightPx)).coerceIn(0f, containerHeightPx - layerHeightPx)

    val targetShape: Shape = when (config.personShape) {
        PersonLayerShape.CUTOUT -> RoundedCornerShape(16.dp)
        PersonLayerShape.ROUNDED_CARD -> RoundedCornerShape(18.dp)
        PersonLayerShape.CIRCLE -> CircleShape
    }

    Box(
        modifier = modifier
            .offset { IntOffset(currentPixelX.roundToInt(), currentPixelY.roundToInt()) }
            .size((layerWidthPx / 2.5f).dp, (layerHeightPx / 2.5f).dp) // approximate dp conversion
            .graphicsLayer {
                scaleX = if (config.isPersonMirrored) -1f else 1f
            }
            .pointerInput(containerWidthPx, containerHeightPx, layerWidthPx, layerHeightPx) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val maxX = containerWidthPx - layerWidthPx
                    val maxY = containerHeightPx - layerHeightPx

                    val newPixelX = (currentPixelX + dragAmount.x).coerceIn(0f, maxX.coerceAtLeast(1f))
                    val newPixelY = (currentPixelY + dragAmount.y).coerceIn(0f, maxY.coerceAtLeast(1f))

                    val normX = if (maxX > 0f) newPixelX / maxX else 0f
                    val normY = if (maxY > 0f) newPixelY / maxY else 0f
                    onPositionChanged(normX, normY)
                }
            }
            .testTag("composition_person_layer")
    ) {
        // Shadow for depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(elevation = 12.dp, shape = targetShape, clip = false)
        )

        // Clipped Person Content Container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(targetShape)
                .background(
                    if (config.personShape == PersonLayerShape.CUTOUT) Color.Transparent else Color(0xFF161A22)
                )
        ) {
            if (isCameraActive && cameraManager != null) {
                val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                // Live Viewfinder
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            cameraManager.startPreview(
                                lifecycleOwner = lifecycleOwner,
                                previewView = this,
                                lensFacing = com.example.model.CameraLensFacing.FRONT,
                                quality = com.example.model.RecordingQuality.STANDARD,
                                onBoundSuccess = {},
                                onBoundFailure = {}
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Procedural Presenter Avatar for Emulators/Simulated Studio
                PresenterAvatarSilhouette(
                    isCutoutMode = config.personShape == PersonLayerShape.CUTOUT,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Optional Border Accent Ring/Outline
        if (config.borderAccent != LayerBorderAccent.NONE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = config.borderAccent.strokeWidth,
                        color = config.borderAccent.color,
                        shape = targetShape
                    )
            )
        }

        // Live Presenter Badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-4).dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black.copy(alpha = 0.70f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (isCameraActive) "CAM ON" else "PRESENTER",
                color = if (isCameraActive) Color(0xFF00FF9D) else Color(0xFF00E5FF),
                fontSize = 9.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PresenterAvatarSilhouette(
    isCutoutMode: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "presenter_gesture")
    val speechPulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speech"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        if (!isCutoutMode) {
            // Subtle studio lighting backdrop for PiP card
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF263238), Color(0xFF101418)),
                    center = Offset(w * 0.5f, h * 0.45f),
                    radius = w * 0.75f
                )
            )
        }

        // Head and Torso coordinates
        val headCenterX = w * 0.5f
        val headCenterY = h * 0.38f + speechPulse * 0.3f
        val headRadius = w * 0.22f

        // Presenter Torso / Shoulders
        val torsoPath = Path().apply {
            moveTo(headCenterX - w * 0.45f, h)
            cubicTo(
                headCenterX - w * 0.40f, h * 0.65f,
                headCenterX - w * 0.28f, headCenterY + headRadius * 0.95f,
                headCenterX - w * 0.12f, headCenterY + headRadius * 1.05f
            )
            lineTo(headCenterX + w * 0.12f, headCenterY + headRadius * 1.05f)
            cubicTo(
                headCenterX + w * 0.28f, headCenterY + headRadius * 0.95f,
                headCenterX + w * 0.40f, h * 0.65f,
                headCenterX + w * 0.45f, h
            )
            close()
        }

        // Torso gradient
        drawPath(
            path = torsoPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF2D3748), Color(0xFF1A202C)),
                startY = headCenterY + headRadius,
                endY = h
            )
        )

        // Head / Face
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8)),
                center = Offset(headCenterX - headRadius * 0.2f, headCenterY - headRadius * 0.2f),
                radius = headRadius * 1.2f
            ),
            radius = headRadius,
            center = Offset(headCenterX, headCenterY)
        )

        // Presenter Headset / Broadcast Mic
        val micEndX = headCenterX + headRadius * 0.65f
        val micEndY = headCenterY + headRadius * 0.60f
        drawLine(
            color = Color(0xFF00FF9D),
            start = Offset(headCenterX + headRadius * 0.95f, headCenterY),
            end = Offset(micEndX, micEndY),
            strokeWidth = 3f
        )
        drawCircle(
            color = Color(0xFF00FF9D),
            radius = 5f,
            center = Offset(micEndX, micEndY)
        )
    }
}
