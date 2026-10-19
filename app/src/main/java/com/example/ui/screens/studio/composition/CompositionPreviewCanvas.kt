package com.example.ui.screens.studio.composition

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.camera.CameraManager
import com.example.model.CompositionConfig
import com.example.model.CompositionSourceType
import com.example.model.PrompterSettings
import com.example.model.Script
import com.example.ui.theme.CueBlack
import com.example.ui.theme.CueBorder
import com.example.ui.theme.CueGreen
import com.example.ui.theme.CueRed
import com.example.ui.theme.CueSurface
import com.example.ui.theme.CueTextPrimary
import com.example.ui.theme.CueTextSecondary
import kotlinx.coroutines.delay

@Composable
fun CompositionPreviewCanvas(
    config: CompositionConfig,
    isCameraActive: Boolean,
    cameraManager: CameraManager?,
    isRecording: Boolean,
    recordingDurationSeconds: Int,
    selectedScript: Script?,
    prompterSettings: PrompterSettings,
    onConfigChange: (CompositionConfig) -> Unit,
    onPersonPositionChanged: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var playbackPositionSec by remember { mutableIntStateOf(config.videoPositionSeconds) }
    val totalDurationSec = config.backgroundMedia.durationSeconds.coerceAtLeast(1)

    // Simulated playback progress ticker for gameplay presets
    LaunchedEffect(config.isVideoPlaying) {
        if (config.isVideoPlaying) {
            while (config.isVideoPlaying) {
                delay(1000L)
                playbackPositionSec = (playbackPositionSec + 1) % totalDurationSec
                onConfigChange(config.copy(videoPositionSeconds = playbackPositionSec))
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(CueBlack)
            .testTag("composition_preview_canvas")
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        // ==========================================
        // 1. BASE BACKGROUND LAYER
        // ==========================================
        when (config.backgroundMedia.sourceType) {
            CompositionSourceType.GAMEPLAY_PRESET -> {
                GameplayPresetCanvas(
                    preset = config.backgroundMedia.gameplayPreset,
                    isPlaying = config.isVideoPlaying,
                    modifier = Modifier.fillMaxSize()
                )
            }
            CompositionSourceType.CUSTOM_IMAGE -> {
                val bmp = config.backgroundMedia.bitmap
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Custom Background Media",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1E2638)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Custom Image Loaded", color = CueGreen)
                    }
                }
            }
            CompositionSourceType.CUSTOM_VIDEO -> {
                val videoUri = config.backgroundMedia.uri
                if (videoUri != null) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                setVideoURI(videoUri)
                                setOnPreparedListener { mp ->
                                    mp.isLooping = config.isVideoLooping
                                    if (config.isVideoMuted) {
                                        mp.setVolume(0f, 0f)
                                    } else {
                                        mp.setVolume(1f, 1f)
                                    }
                                    if (config.isVideoPlaying) {
                                        start()
                                    }
                                }
                                videoViewRef = this
                            }
                        },
                        update = { view ->
                            if (config.isVideoPlaying && !view.isPlaying) {
                                view.start()
                            } else if (!config.isVideoPlaying && view.isPlaying) {
                                view.pause()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // ==========================================
        // 2. FOREGROUND / PERSON LAYER (DRAGGABLE & RESIZABLE)
        // ==========================================
        PersonOverlayLayer(
            config = config,
            containerWidthPx = widthPx,
            containerHeightPx = heightPx,
            isCameraActive = isCameraActive,
            cameraManager = cameraManager,
            onPositionChanged = onPersonPositionChanged,
            modifier = Modifier.fillMaxSize()
        )

        // ==========================================
        // 3. FLOATING TELEPROMPTER OVERLAY BANNER
        // ==========================================
        if (config.isPrompterVisible && selectedScript != null) {
            CompositionPrompterOverlay(
                script = selectedScript,
                settings = prompterSettings.copy(scrollSpeedWpm = config.prompterSpeedWpm),
                opacity = config.prompterOpacity,
                isPlaying = config.isPrompterScrolling,
                onPlayPauseToggle = {
                    onConfigChange(config.copy(isPrompterScrolling = !config.isPrompterScrolling))
                },
                onClose = {
                    onConfigChange(config.copy(isPrompterVisible = false))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            )
        }

        // ==========================================
        // 4. ACTIVE RECORDING HUD INDICATOR
        // ==========================================
        AnimatedVisibility(
            visible = isRecording,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.85f))
                    .border(1.dp, CueRed, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FiberManualRecord,
                    contentDescription = null,
                    tint = CueRed,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                val mins = recordingDurationSeconds / 60
                val secs = recordingDurationSeconds % 60
                Text(
                    text = String.format("REC %02d:%02d", mins, secs),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ==========================================
        // 5. VIDEO PLAYBACK CONTROLLER TRANSPORT HUD
        // ==========================================
        if (config.backgroundMedia.sourceType != CompositionSourceType.CUSTOM_IMAGE) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.82f))
                    .border(1.dp, CueBorder.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Play / Pause Button
                IconButton(
                    onClick = {
                        val newPlayState = !config.isVideoPlaying
                        onConfigChange(config.copy(isVideoPlaying = newPlayState))
                        videoViewRef?.let { vv ->
                            if (newPlayState) vv.start() else vv.pause()
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (config.isVideoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (config.isVideoPlaying) "Pause Video" else "Play Video",
                        tint = CueGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Timecode
                val curM = playbackPositionSec / 60
                val curS = playbackPositionSec % 60
                val totM = totalDurationSec / 60
                val totS = totalDurationSec % 60
                Text(
                    text = String.format("%02d:%02d / %02d:%02d", curM, curS, totM, totS),
                    color = CueTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                // Timeline Scrubber
                Slider(
                    value = playbackPositionSec.toFloat(),
                    onValueChange = { newVal ->
                        playbackPositionSec = newVal.toInt()
                        videoViewRef?.seekTo(newVal.toInt() * 1000)
                        onConfigChange(config.copy(videoPositionSeconds = playbackPositionSec))
                    },
                    valueRange = 0f..totalDurationSec.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = CueGreen,
                        activeTrackColor = CueGreen,
                        inactiveTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )

                // Mute / Unmute
                IconButton(
                    onClick = {
                        onConfigChange(config.copy(isVideoMuted = !config.isVideoMuted))
                    },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = if (config.isVideoMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = "Toggle Mute",
                        tint = if (config.isVideoMuted) CueRed else CueTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Looping toggle
                IconButton(
                    onClick = {
                        onConfigChange(config.copy(isVideoLooping = !config.isVideoLooping))
                    },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Toggle Loop",
                        tint = if (config.isVideoLooping) CueGreen else CueTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
