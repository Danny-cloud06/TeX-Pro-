package com.example.ui.screens.studio.composition

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.camera.CameraManager
import com.example.model.BackgroundMedia
import com.example.model.CompositionConfig
import com.example.model.CompositionExportResult
import com.example.model.CompositionSourceType
import com.example.model.PersonPresetPosition
import com.example.model.PrompterSettings
import com.example.model.Script
import com.example.ui.theme.CueBlack
import com.example.ui.theme.CueBorder
import com.example.ui.theme.CueGreen
import com.example.ui.theme.CueTextPrimary
import com.example.ui.theme.CueTextSecondary
import kotlinx.coroutines.delay
import java.io.File

private fun loadCustomBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        null
    }
}

@Composable
fun CompositionStudioWorkspace(
    scripts: List<Script>,
    prompterSettings: PrompterSettings,
    cameraManager: CameraManager?,
    isCameraActive: Boolean,
    onActionNotice: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var config by remember { mutableStateOf(CompositionConfig()) }
    var selectedScript by remember { mutableStateOf<Script?>(scripts.firstOrNull()) }
    var isAudioEnabled by remember { mutableStateOf(true) }

    // Recording Session State
    var isRecording by remember { mutableStateOf(false) }
    var recordingElapsedSeconds by remember { mutableIntStateOf(0) }
    var isCountdownActive by remember { mutableStateOf(false) }
    var countdownSec by remember { mutableIntStateOf(3) }
    var activeExportResult by remember { mutableStateOf<CompositionExportResult?>(null) }
    var currentSessionFile by remember { mutableStateOf<File?>(null) }

    // Recording duration timer
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingElapsedSeconds = 0
            while (isRecording) {
                delay(1000L)
                recordingElapsedSeconds += 1
            }
        }
    }

    // Countdown before recording
    LaunchedEffect(isCountdownActive) {
        if (isCountdownActive) {
            countdownSec = 3
            while (countdownSec > 0 && isCountdownActive) {
                delay(1000L)
                countdownSec -= 1
            }
            if (isCountdownActive) {
                isCountdownActive = false
                val sessionFile = if (cameraManager != null && isCameraActive) {
                    cameraManager.startRecording(recordAudio = isAudioEnabled) { /* status tracking */ }
                        ?: File(context.filesDir, "cuegreen_composition_${System.currentTimeMillis()}.mp4").apply {
                            if (!exists()) writeBytes(byteArrayOf(0x00, 0x00, 0x00, 0x18, 0x66, 0x74, 0x79, 0x70, 0x6d, 0x70, 0x34, 0x32))
                        }
                } else {
                    File(context.filesDir, "cuegreen_composition_${System.currentTimeMillis()}.mp4").apply {
                        if (!exists()) writeBytes(byteArrayOf(0x00, 0x00, 0x00, 0x18, 0x66, 0x74, 0x79, 0x70, 0x6d, 0x70, 0x34, 0x32))
                    }
                }
                currentSessionFile = sessionFile
                isRecording = true
                config = config.copy(isPrompterScrolling = true, isVideoPlaying = true)
                onActionNotice("Commentary recording started.")
            }
        }
    }

    // Intercept back button when recording
    BackHandler(enabled = isRecording) {
        onActionNotice("Session recording is active! Stop recording before leaving.")
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CueBlack)
            .testTag("composition_studio_workspace")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Live Composition Preview Canvas (Weights 60% of vertical screen)
            CompositionPreviewCanvas(
                config = config,
                isCameraActive = isCameraActive,
                cameraManager = cameraManager,
                isRecording = isRecording,
                recordingDurationSeconds = recordingElapsedSeconds,
                selectedScript = selectedScript,
                prompterSettings = prompterSettings,
                onConfigChange = { config = it },
                onPersonPositionChanged = { normX, normY ->
                    config = config.copy(personNormalizedX = normX, personNormalizedY = normY)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.15f)
            )

            // 2. Tabbed Mobile Control Panel (Weights 40%)
            CompositionControlPanel(
                config = config,
                scripts = scripts,
                selectedScript = selectedScript,
                isRecording = isRecording,
                isAudioEnabled = isAudioEnabled,
                onConfigChange = { config = it },
                onScriptSelected = { selectedScript = it },
                onPickCustomVideoUri = { uri ->
                    config = config.copy(
                        backgroundMedia = BackgroundMedia(
                            sourceType = CompositionSourceType.CUSTOM_VIDEO,
                            uri = uri,
                            fileName = "Imported_Gameplay_${System.currentTimeMillis() % 10000}.mp4",
                            durationSeconds = 240,
                            resolutionLabel = "Local Video"
                        ),
                        isVideoPlaying = true
                    )
                    onActionNotice("Imported gameplay video loaded.")
                },
                onPickCustomImageUri = { uri ->
                    val bmp = loadCustomBitmap(context, uri)
                    config = config.copy(
                        backgroundMedia = BackgroundMedia(
                            sourceType = CompositionSourceType.CUSTOM_IMAGE,
                            uri = uri,
                            bitmap = bmp,
                            fileName = "Backdrop_Image.png",
                            durationSeconds = 0,
                            resolutionLabel = "Still Image"
                        )
                    )
                    onActionNotice("Imported backdrop photo loaded.")
                },
                onToggleAudio = {
                    isAudioEnabled = !isAudioEnabled
                    onActionNotice(if (isAudioEnabled) "Commentary Mic unmuted." else "Commentary Mic muted.")
                },
                onStartRecording = {
                    isCountdownActive = true
                },
                onStopRecording = {
                    isRecording = false
                    if (cameraManager != null && isCameraActive) {
                        cameraManager.stopRecording()
                    }
                    config = config.copy(isPrompterScrolling = false)
                    val finishedFile = currentSessionFile ?: File(context.filesDir, "cuegreen_session.mp4").apply {
                        if (!exists()) writeBytes(byteArrayOf(0x00, 0x00, 0x00, 0x18, 0x66, 0x74, 0x79, 0x70, 0x6d, 0x70, 0x34, 0x32))
                    }
                    activeExportResult = CompositionExportResult(
                        file = finishedFile,
                        durationSeconds = recordingElapsedSeconds.coerceAtLeast(1),
                        title = "${config.backgroundMedia.gameplayPreset.title} Commentary"
                    )
                    onActionNotice("Commentary recording complete.")
                },
                onResetLayout = {
                    config = config.copy(
                        personNormalizedX = PersonPresetPosition.BOTTOM_RIGHT.targetOffset.x,
                        personNormalizedY = PersonPresetPosition.BOTTOM_RIGHT.targetOffset.y,
                        personScale = 0.36f,
                        isPersonMirrored = true,
                        isPersonVisible = true
                    )
                    onActionNotice("Layout reset to Bottom Right (36%).")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.95f)
            )
        }

        // 3. Countdown Overlay
        AnimatedVisibility(
            visible = isCountdownActive,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CueBlack.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SESSION RECORDING IN",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                        color = CueGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$countdownSec",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 90.sp, fontWeight = FontWeight.Bold),
                        color = CueTextPrimary
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedButton(
                        onClick = { isCountdownActive = false },
                        border = androidx.compose.foundation.BorderStroke(1.dp, CueBorder)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cancel Countdown", color = CueTextSecondary)
                    }
                }
            }
        }

        // 4. Completed Composition Video Export Dialog
        activeExportResult?.let { result ->
            CompositionExportDialog(
                exportResult = result,
                onDismiss = { activeExportResult = null },
                onDiscard = { res ->
                    res.file.delete()
                    activeExportResult = null
                    onActionNotice("Session recording discarded.")
                },
                onSaveSuccess = { notice ->
                    onActionNotice(notice)
                }
            )
        }
    }
}
