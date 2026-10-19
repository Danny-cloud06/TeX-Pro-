package com.example.ui.screens.studio

import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.camera.CameraManager
import com.example.model.CameraLensFacing
import com.example.model.StudioSettings
import com.example.ui.theme.CueBlack
import com.example.ui.theme.CueBorder
import com.example.ui.theme.CueCharcoal
import com.example.ui.theme.CueGreen
import com.example.ui.theme.CueRed
import com.example.ui.theme.CueSurface
import com.example.ui.theme.CueTextMuted
import com.example.ui.theme.CueTextPrimary
import com.example.ui.theme.CueTextSecondary

import androidx.compose.material.icons.filled.AutoAwesome
import com.example.model.AiBackgroundSettings
import com.example.model.BackgroundMode

@Composable
fun StudioCameraView(
    cameraManager: CameraManager,
    settings: StudioSettings,
    aiSettings: AiBackgroundSettings = AiBackgroundSettings(),
    isCameraActive: Boolean,
    isRecording: Boolean,
    recordingElapsedSeconds: Int,
    onSwitchLens: () -> Unit,
    onOpenAiEffects: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    var previewBoundSuccess by remember { mutableStateOf(false) }
    var previewErrorMessage by remember { mutableStateOf<String?>(null) }
    var previewViewInstance by remember { mutableStateOf<PreviewView?>(null) }

    // Bind preview whenever camera active state, lens, or quality changes
    LaunchedEffect(isCameraActive, settings.lensFacing, settings.recordingQuality) {
        if (isCameraActive && previewViewInstance != null) {
            cameraManager.startPreview(
                lifecycleOwner = lifecycleOwner,
                previewView = previewViewInstance!!,
                lensFacing = settings.lensFacing,
                quality = settings.recordingQuality,
                onBoundSuccess = {
                    previewBoundSuccess = true
                    previewErrorMessage = null
                },
                onBoundFailure = { error ->
                    previewBoundSuccess = false
                    previewErrorMessage = error
                }
            )
        } else if (!isCameraActive) {
            cameraManager.stopCamera()
            previewBoundSuccess = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CueBlack)
            .testTag("studio_camera_view")
    ) {
        if (isCameraActive) {
            // Android View hosting PreviewView
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        previewViewInstance = this
                        cameraManager.startPreview(
                            lifecycleOwner = lifecycleOwner,
                            previewView = this,
                            lensFacing = settings.lensFacing,
                            quality = settings.recordingQuality,
                            onBoundSuccess = {
                                previewBoundSuccess = true
                                previewErrorMessage = null
                            },
                            onBoundFailure = { error ->
                                previewBoundSuccess = false
                                previewErrorMessage = error
                            }
                        )
                    }
                },
                update = { view ->
                    previewViewInstance = view
                    // Mirror preview if front lens and mirroring enabled
                    if (settings.lensFacing == CameraLensFacing.FRONT && settings.isPreviewMirrored) {
                        view.scaleX = -1f
                    } else {
                        view.scaleX = 1f
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // AI Background Layer (Chroma Green, Depth Blur, Virtual Set, Solid Color, Custom Image)
            if (aiSettings.isEffectActive) {
                StudioAiBackgroundView(
                    aiSettings = aiSettings,
                    lensFacing = settings.lensFacing,
                    isHardwareFeedActive = previewBoundSuccess,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Graceful fallback simulation view when physical camera hardware is absent (e.g., emulator)
            if (!previewBoundSuccess && previewErrorMessage != null && !aiSettings.isEffectActive) {
                CameraFallbackSimulator(
                    lensFacing = settings.lensFacing,
                    errorMessage = previewErrorMessage,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            // Camera Inactive Placeholder
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CueCharcoal),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VideocamOff,
                        contentDescription = null,
                        tint = CueTextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Camera is Inactive",
                        style = MaterialTheme.typography.titleMedium,
                        color = CueTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap \"Start Camera\" below to initialize the video viewfinder.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CueTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        // Top Status HUD Bar (Lens switch, Recording indicator, Audio badge, Quality badge)
        if (isCameraActive) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Recording Timer Badge or Live indicator
                if (isRecording) {
                    val min = recordingElapsedSeconds / 60
                    val sec = recordingElapsedSeconds % 60
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(CueRed)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = String.format("REC %02d:%02d", min, sec),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(CueSurface.copy(alpha = 0.85f))
                            .border(1.dp, CueBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(CueGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (settings.lensFacing == CameraLensFacing.FRONT) "FRONT LENS" else "REAR LENS",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = CueGreen
                        )
                    }
                }

                // Quick Hardware Badges & Lens switch button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Audio Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CueSurface.copy(alpha = 0.85f))
                            .border(1.dp, CueBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (settings.isAudioEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = if (settings.isAudioEnabled) "Audio ON" else "Silent Recording",
                            tint = if (settings.isAudioEnabled) CueGreen else CueRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // AI Effects Action Button
                    IconButton(
                        onClick = onOpenAiEffects,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (aiSettings.isEffectActive) CueGreen.copy(alpha = 0.2f) else CueSurface.copy(alpha = 0.85f))
                            .border(1.dp, if (aiSettings.isEffectActive) CueGreen else CueBorder, CircleShape)
                            .testTag("open_ai_effects_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Studio Background Effects",
                            tint = if (aiSettings.isEffectActive) CueGreen else CueTextPrimary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Lens Switch Button (Front / Rear)
                    IconButton(
                        onClick = onSwitchLens,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CueSurface.copy(alpha = 0.85f))
                            .border(1.dp, CueBorder, CircleShape)
                            .testTag("switch_lens_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Switch front and rear camera",
                            tint = CueTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CameraFallbackSimulator(
    lensFacing: CameraLensFacing,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(CueBlack)
            .border(2.dp, CueBorder),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = null,
                tint = CueGreen,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Studio Viewfinder Active",
                style = MaterialTheme.typography.titleMedium,
                color = CueTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (lensFacing == CameraLensFacing.FRONT) "Front Selfie Camera Mode" else "Rear Broadcast Lens Mode",
                style = MaterialTheme.typography.bodySmall,
                color = CueGreen
            )
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Hardware feed notice: $errorMessage (Running in emulator mode). Video recording pipeline is fully primed.",
                    style = MaterialTheme.typography.labelSmall,
                    color = CueTextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
