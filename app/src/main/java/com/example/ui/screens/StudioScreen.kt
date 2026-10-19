package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.video.VideoRecordEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.camera.CameraManager
import com.example.model.AiBackgroundSettings
import com.example.model.BackgroundMode
import com.example.model.CameraLensFacing
import com.example.model.PrompterSettings
import com.example.model.RecordedVideo
import com.example.model.RecordingQuality
import com.example.model.Script
import com.example.model.StudioSettings
import com.example.ui.screens.studio.RecordedVideoPreviewDialog
import com.example.ui.screens.studio.StudioCameraView
import com.example.ui.screens.studio.StudioPrompterOverlay
import com.example.ui.theme.CueBlack
import com.example.ui.theme.CueBorder
import com.example.ui.theme.CueCharcoal
import com.example.ui.theme.CueGreen
import com.example.ui.theme.CueGreenContainer
import com.example.ui.theme.CueRed
import com.example.ui.theme.CueSurface
import com.example.ui.theme.CueSurfaceElevated
import com.example.ui.theme.CueTextMuted
import com.example.ui.theme.CueTextPrimary
import com.example.ui.theme.CueTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun StudioScreen(
    scripts: List<Script> = emptyList(),
    prompterSettings: PrompterSettings = PrompterSettings(),
    onActionNotice: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val cameraManager = remember { CameraManager(context) }

    // Permissions State
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var hasAudioPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasCameraPermission = perms[Manifest.permission.CAMERA] ?: hasCameraPermission
        hasAudioPermission = perms[Manifest.permission.RECORD_AUDIO] ?: hasAudioPermission
        if (hasCameraPermission) {
            onActionNotice("Camera access ready.")
        } else {
            onActionNotice("Camera permission is required for the teleprompter viewfinder.")
        }
    }

    // Studio Configuration & Recording States
    var studioSettings by remember { mutableStateOf(StudioSettings()) }
    var activePrompterSettings by remember { mutableStateOf(prompterSettings) }
    var isGreenScreenMode by remember { mutableStateOf(false) }
    var aiBackgroundSettings by remember { mutableStateOf(AiBackgroundSettings()) }
    var isEyeGuideActive by remember { mutableStateOf(true) }
    var isEyeContactModalOpen by remember { mutableStateOf(false) }

    var isCameraActive by remember { mutableStateOf(true) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingElapsedSeconds by remember { mutableIntStateOf(0) }
    var activeRecordingFile by remember { mutableStateOf<File?>(null) }
    var completedVideo by remember { mutableStateOf<RecordedVideo?>(null) }

    // Countdown before recording
    var isRecordingCountdownActive by remember { mutableStateOf(false) }
    var recordingCountdownSec by remember { mutableIntStateOf(studioSettings.countdownSeconds) }

    // Teleprompter Integration State in Studio
    var selectedScript by remember { mutableStateOf<Script?>(scripts.firstOrNull()) }
    var isPrompterVisible by remember { mutableStateOf(true) }
    var isPrompterScrolling by remember { mutableStateOf(false) }
    var prompterOpacity by remember { mutableStateOf(0.70f) }
    var isScriptMenuExpanded by remember { mutableStateOf(false) }

    // Initialize CameraManager lifecycle
    DisposableEffect(Unit) {
        cameraManager.initialize { /* ready */ }
        onDispose {
            cameraManager.stopCamera()
        }
    }

    // Intercept back button when recording
    BackHandler(enabled = isRecording) {
        onActionNotice("Recording is currently active! Tap stop before leaving.")
    }

    // Recording duration timer ticker
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingElapsedSeconds = 0
            while (isRecording) {
                delay(1000L)
                recordingElapsedSeconds += 1
            }
        }
    }

    // Recording countdown timer
    LaunchedEffect(isRecordingCountdownActive) {
        if (isRecordingCountdownActive) {
            recordingCountdownSec = studioSettings.countdownSeconds.coerceAtLeast(3)
            while (recordingCountdownSec > 0 && isRecordingCountdownActive) {
                delay(1000L)
                recordingCountdownSec -= 1
            }
            if (isRecordingCountdownActive) {
                isRecordingCountdownActive = false
                val file = cameraManager.startRecording(
                    recordAudio = studioSettings.isAudioEnabled && hasAudioPermission
                ) { event ->
                    when (event) {
                        is VideoRecordEvent.Finalize -> {
                            isRecording = false
                            isPrompterScrolling = false
                            val currentFile = activeRecordingFile
                            if (!event.hasError() && currentFile != null) {
                                completedVideo = RecordedVideo(
                                    file = currentFile,
                                    durationSeconds = recordingElapsedSeconds.coerceAtLeast(1)
                                )
                            }
                        }
                    }
                }
                activeRecordingFile = file
                isRecording = true
                isPrompterScrolling = true
                onActionNotice("Recording started.")
            }
        }
    }

    // Main Studio Layout
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CueBlack)
    ) {
        if (!hasCameraPermission) {
            // Permission Request State
            CameraPermissionCard(
                onRequestPermissions = {
                    permissionLauncher.launch(
                        arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                // 1. Full-screen Camera Viewfinder
                StudioCameraView(
                    cameraManager = cameraManager,
                    settings = studioSettings,
                    aiSettings = if (isGreenScreenMode) {
                        aiBackgroundSettings.copy(
                            mode = BackgroundMode.GREEN_SCREEN,
                            isAiEnabled = true
                        )
                    } else {
                        aiBackgroundSettings.copy(
                            mode = BackgroundMode.ORIGINAL,
                            isAiEnabled = false
                        )
                    },
                    isCameraActive = isCameraActive,
                    isRecording = isRecording,
                    recordingElapsedSeconds = recordingElapsedSeconds,
                    onSwitchLens = {
                        val newLens = if (studioSettings.lensFacing == CameraLensFacing.FRONT) {
                            CameraLensFacing.BACK
                        } else {
                            CameraLensFacing.FRONT
                        }
                        studioSettings = studioSettings.copy(lensFacing = newLens)
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // 2. Large Readable Teleprompter Overlay positioned close to top camera
                if (isCameraActive && isPrompterVisible && selectedScript != null) {
                    StudioPrompterOverlay(
                        script = selectedScript!!,
                        settings = activePrompterSettings,
                        opacity = prompterOpacity,
                        isPrompterPlaying = isPrompterScrolling,
                        isEyeGuideVisible = isEyeGuideActive,
                        onPrompterPlayToggle = { isPrompterScrolling = !isPrompterScrolling },
                        onSpeedChange = { newWpm ->
                            activePrompterSettings = activePrompterSettings.copy(scrollSpeedWpm = newWpm)
                        },
                        onFontSizeChange = { newSize ->
                            activePrompterSettings = activePrompterSettings.copy(fontSizeSp = newSize)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // 3. Top Studio Action Bar: Script Selector & Studio Tools
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Script Selection Dropdown Chip
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(CueSurface.copy(alpha = 0.90f))
                                .border(1.dp, CueBorder, RoundedCornerShape(20.dp))
                                .clickable { isScriptMenuExpanded = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = CueGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedScript?.title?.take(16) ?: "Select Script",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = CueTextPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = CueTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = isScriptMenuExpanded,
                            onDismissRequest = { isScriptMenuExpanded = false },
                            modifier = Modifier.background(CueSurface)
                        ) {
                            scripts.forEach { script ->
                                DropdownMenuItem(
                                    text = { Text(script.title, color = CueTextPrimary) },
                                    onClick = {
                                        selectedScript = script
                                        isScriptMenuExpanded = false
                                        onActionNotice("Loaded script: ${script.title}")
                                    }
                                )
                            }
                        }
                    }

                    // Studio Controls Strip
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Eye Contact Assistant Button
                        IconButton(
                            onClick = { isEyeContactModalOpen = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isEyeGuideActive) CueGreenContainer else CueSurface.copy(alpha = 0.90f))
                                .border(1.dp, if (isEyeGuideActive) CueGreen else CueBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RemoveRedEye,
                                contentDescription = "Eye Contact Guide",
                                tint = if (isEyeGuideActive) CueGreen else CueTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Green Screen Recording Mode Toggle
                        IconButton(
                            onClick = {
                                isGreenScreenMode = !isGreenScreenMode
                                onActionNotice(if (isGreenScreenMode) "Chroma Green Screen Mode Active" else "Standard Viewfinder Active")
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isGreenScreenMode) Color(0xFF00FF00).copy(alpha = 0.25f) else CueSurface.copy(alpha = 0.90f))
                                .border(1.dp, if (isGreenScreenMode) Color(0xFF00FF00) else CueBorder, CircleShape)
                        ) {
                            Text(
                                text = "GS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp
                                ),
                                color = if (isGreenScreenMode) Color(0xFF00FF00) else CueTextSecondary
                            )
                        }

                        // Camera Lens Toggle (Front / Back)
                        IconButton(
                            onClick = {
                                val nextLens = if (studioSettings.lensFacing == CameraLensFacing.FRONT) {
                                    CameraLensFacing.BACK
                                } else {
                                    CameraLensFacing.FRONT
                                }
                                studioSettings = studioSettings.copy(lensFacing = nextLens)
                                onActionNotice("Switched to ${nextLens.name.lowercase()} camera")
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CueSurface.copy(alpha = 0.90f))
                                .border(1.dp, CueBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cameraswitch,
                                contentDescription = "Switch Camera Lens",
                                tint = CueTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Audio Mic Toggle
                        IconButton(
                            onClick = {
                                studioSettings = studioSettings.copy(isAudioEnabled = !studioSettings.isAudioEnabled)
                                onActionNotice(if (studioSettings.isAudioEnabled) "Microphone active" else "Microphone muted")
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (studioSettings.isAudioEnabled) CueGreenContainer else CueSurface.copy(alpha = 0.90f))
                                .border(1.dp, if (studioSettings.isAudioEnabled) CueGreen else CueBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (studioSettings.isAudioEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = "Toggle Audio",
                                tint = if (studioSettings.isAudioEnabled) CueGreen else CueRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Prompter Overlay Visibility Toggle
                        IconButton(
                            onClick = { isPrompterVisible = !isPrompterVisible },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isPrompterVisible) CueGreenContainer else CueSurface.copy(alpha = 0.90f))
                                .border(1.dp, if (isPrompterVisible) CueGreen else CueBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Toggle Prompter",
                                tint = if (isPrompterVisible) CueGreen else CueTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // 4. Recording HUD (Pulsing Red REC Timer)
                AnimatedVisibility(
                    visible = isRecording,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 58.dp, end = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.85f))
                            .border(1.dp, CueRed, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FiberManualRecord,
                            contentDescription = null,
                            tint = CueRed,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val mins = recordingElapsedSeconds / 60
                        val secs = recordingElapsedSeconds % 60
                        Text(
                            text = String.format("REC %02d:%02d", mins, secs),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 5. Bottom Studio Controller (Record Button & Viewfinder Adjustments)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(32.dp))
                            .background(CueCharcoal.copy(alpha = 0.92f))
                            .border(1.dp, CueBorder, RoundedCornerShape(32.dp))
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mirror preview toggle
                        IconButton(
                            onClick = {
                                studioSettings = studioSettings.copy(isPreviewMirrored = !studioSettings.isPreviewMirrored)
                                onActionNotice(if (studioSettings.isPreviewMirrored) "Preview mirrored" else "Preview normal")
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flip,
                                contentDescription = "Mirror Camera",
                                tint = if (studioSettings.isPreviewMirrored) CueGreen else CueTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Main Record Button
                        Button(
                            onClick = {
                                if (isRecording) {
                                    cameraManager.stopRecording()
                                    isRecording = false
                                    isPrompterScrolling = false
                                } else {
                                    if (studioSettings.countdownSeconds > 0) {
                                        isRecordingCountdownActive = true
                                    } else {
                                        val file = cameraManager.startRecording(
                                            recordAudio = studioSettings.isAudioEnabled && hasAudioPermission
                                        ) { event ->
                                            when (event) {
                                                is VideoRecordEvent.Finalize -> {
                                                    isRecording = false
                                                    isPrompterScrolling = false
                                                    val currentFile = activeRecordingFile
                                                    if (!event.hasError() && currentFile != null) {
                                                        completedVideo = RecordedVideo(
                                                            file = currentFile,
                                                            durationSeconds = recordingElapsedSeconds.coerceAtLeast(1)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        activeRecordingFile = file
                                        isRecording = true
                                        isPrompterScrolling = true
                                        onActionNotice("Recording started.")
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRecording) CueRed else CueGreen,
                                contentColor = CueBlack
                            ),
                            shape = CircleShape,
                            modifier = Modifier
                                .size(62.dp)
                                .testTag("studio_record_button")
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Videocam,
                                contentDescription = if (isRecording) "Stop Recording" else "Record",
                                modifier = Modifier.size(30.dp),
                                tint = if (isRecording) Color.White else CueBlack
                            )
                        }

                        // Prompter Opacity Toggle
                        IconButton(
                            onClick = {
                                prompterOpacity = if (prompterOpacity > 0.5f) 0.35f else 0.75f
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Opacity,
                                contentDescription = "Prompter Opacity",
                                tint = CueTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // 6. 3-Second Recording Countdown Overlay
        AnimatedVisibility(
            visible = isRecordingCountdownActive,
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
                        text = "RECORDING IN",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                        color = CueGreen
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "$recordingCountdownSec",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 90.sp, fontWeight = FontWeight.Bold),
                        color = CueTextPrimary
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedButton(
                        onClick = { isRecordingCountdownActive = false },
                        border = androidx.compose.foundation.BorderStroke(1.dp, CueBorder)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cancel Countdown", color = CueTextSecondary)
                    }
                }
            }
        }

        // 7. Recorded Video Preview & Local Save Dialog
        completedVideo?.let { video ->
            RecordedVideoPreviewDialog(
                video = video,
                onDismiss = { completedVideo = null },
                onDiscard = { vid ->
                    vid.file.delete()
                    completedVideo = null
                    onActionNotice("Recording discarded.")
                }
            )
        }

        // 8. Eye-Contact Assistant Information Modal
        if (isEyeContactModalOpen) {
            EyeContactAssistantDialog(
                isEyeGuideActive = isEyeGuideActive,
                onToggleEyeGuide = { isEyeGuideActive = it },
                onDismiss = { isEyeContactModalOpen = false }
            )
        }
    }
}

@Composable
fun EyeContactAssistantDialog(
    isEyeGuideActive: Boolean,
    onToggleEyeGuide: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.RemoveRedEye,
                    contentDescription = null,
                    tint = CueGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Eye-Contact Alignment", color = CueTextPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Optical alignment card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CueSurfaceElevated)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Camera-Proximity Reading Zone",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = CueGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "TeX positions your script directly beneath the selfie camera lens. Reading near the top edge reduces your gaze divergence to under 3 degrees, ensuring your audience sees natural, direct eye contact.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CueTextSecondary
                    )
                }

                // Technical status note
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CueSurfaceElevated)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "AI Gaze Redirection Status",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = CueTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Real-time generative eyeball pixel synthesis requires dedicated hardware neural processing (NPU). TeX utilizes broadcast optical lens proximity to guarantee zero visual artifacts and smooth 60FPS recording.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CueTextMuted
                    )
                }

                // Eye line guide toggle switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CueSurfaceElevated)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Show Eye-Line Guide Beam",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CueTextPrimary
                    )
                    Switch(
                        checked = isEyeGuideActive,
                        onCheckedChange = onToggleEyeGuide,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CueGreen,
                            checkedTrackColor = CueGreenContainer
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CueGreen,
                    contentColor = CueBlack
                )
            ) {
                Text("Got It", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = CueSurface
    )
}

@Composable
fun CameraPermissionCard(
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CueSurface)
                .border(1.dp, CueBorder, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = CueGreen,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Camera & Audio Access",
                style = MaterialTheme.typography.titleLarge,
                color = CueTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "TeX requires Camera access to display your live recording viewfinder, and Microphone access to record synchronized talking-head video with your teleprompter presentation.",
                style = MaterialTheme.typography.bodyMedium,
                color = CueTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onRequestPermissions,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CueGreen,
                    contentColor = CueBlack
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("grant_permissions_button")
            ) {
                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enable Camera & Microphone", fontWeight = FontWeight.Bold)
            }
        }
    }
}
