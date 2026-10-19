package com.example.ui.screens.teleprompter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PrompterSettings
import com.example.model.Script
import com.example.ui.theme.CueBlack
import com.example.ui.theme.CueBorder
import com.example.ui.theme.CueCharcoal
import com.example.ui.theme.CueGreen
import com.example.ui.theme.CueGreenGlow
import com.example.ui.theme.CueSurface
import com.example.ui.theme.CueTextMuted
import com.example.ui.theme.CueTextPrimary
import com.example.ui.theme.CueTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun PrompterReadingViewport(
    script: Script,
    settings: PrompterSettings,
    isPlaying: Boolean,
    onPlayStateChange: (Boolean) -> Unit,
    onProgressUpdate: (progressPercent: Float, currentWordOffset: Int) -> Unit,
    scrollState: androidx.compose.foundation.ScrollState,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isCountdownActive by remember { mutableStateOf(false) }
    var countdownRemaining by remember { mutableIntStateOf(settings.countdownSeconds) }
    var isScriptEnded by remember { mutableStateOf(false) }

    // Start countdown if playing is requested while at top and countdown > 0
    fun handleStartPlay() {
        if (settings.countdownSeconds > 0 && scrollState.value == 0) {
            countdownRemaining = settings.countdownSeconds
            isCountdownActive = true
        } else {
            isCountdownActive = false
            onPlayStateChange(true)
        }
    }

    // Countdown timer ticker
    LaunchedEffect(isCountdownActive) {
        if (isCountdownActive) {
            while (countdownRemaining > 0 && isCountdownActive) {
                delay(1000L)
                countdownRemaining -= 1
            }
            if (isCountdownActive) {
                isCountdownActive = false
                onPlayStateChange(true)
            }
        }
    }

    // High-resolution delta-time smooth scrolling loop
    val speedPxPerSec = settings.pixelsPerSecond
    LaunchedEffect(isPlaying, isCountdownActive, speedPxPerSec) {
        if (isPlaying && !isCountdownActive) {
            var lastTime = System.nanoTime()
            while (isActive && isPlaying) {
                withFrameNanos { now ->
                    val elapsedNanos = now - lastTime
                    lastTime = now
                    // Cap to 100ms to avoid jumps when switching apps or browser tabs
                    val clampedSec = (elapsedNanos / 1_000_000_000f).coerceIn(0f, 0.1f)
                    val delta = speedPxPerSec * clampedSec

                    coroutineScope.launch {
                        if (scrollState.maxValue > 0 && scrollState.value >= scrollState.maxValue) {
                            onPlayStateChange(false)
                            isScriptEnded = true
                        } else {
                            scrollState.scrollBy(delta)
                        }
                    }
                }
            }
        }
    }

    // Calculate reading progress
    val progressPercent = if (scrollState.maxValue > 0) {
        (scrollState.value.toFloat() / scrollState.maxValue).coerceIn(0f, 1f)
    } else 0f

    val currentEstimatedWord = (script.wordCount * progressPercent).toInt().coerceIn(0, script.wordCount)

    LaunchedEffect(scrollState.value, scrollState.maxValue) {
        onProgressUpdate(progressPercent, currentEstimatedWord)
        if (scrollState.value < scrollState.maxValue) {
            isScriptEnded = false
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(CueBlack)
            .testTag("prompter_reading_viewport")
    ) {
        val viewportHeight = maxHeight
        val guideOffset = viewportHeight * settings.guidePositionPercent

        // Scrolling script text area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    // Apply mirror scaleX ONLY to the reading text!
                    .graphicsLayer {
                        if (settings.isMirrored) {
                            scaleX = -1f
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top spacer so first line starts right at the cue line
                Spacer(modifier = Modifier.height(guideOffset))

                // Script title header
                Text(
                    text = script.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (settings.fontSizeSp * 0.75f).sp
                    ),
                    color = CueGreen,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Main body text
                if (script.content.isBlank()) {
                    Text(
                        text = "(This script has no text content. Add text in the Script Manager to prompt.)",
                        style = MaterialTheme.typography.bodyLarge,
                        color = CueTextMuted,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = script.content,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = settings.fontSizeSp.sp,
                            lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier).sp,
                            fontWeight = if (settings.isBold) FontWeight.Bold else FontWeight.Normal,
                            letterSpacing = 0.5.sp
                        ),
                        color = CueTextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Completion banner
                if (isScriptEnded || (scrollState.maxValue > 0 && scrollState.value >= scrollState.maxValue)) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CueSurface)
                            .border(1.dp, CueGreen, RoundedCornerShape(12.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = CueGreen,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Script Completed",
                                style = MaterialTheme.typography.titleMedium,
                                color = CueTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "You have reached the end of \"${script.title}\".",
                                style = MaterialTheme.typography.bodySmall,
                                color = CueTextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        scrollState.scrollTo(0)
                                        handleStartPlay()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CueGreen,
                                    contentColor = CueBlack
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restart Playback", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Bottom spacer so last paragraph can scroll all the way through the cue line
                Spacer(modifier = Modifier.height(viewportHeight - guideOffset))
            }
        }

        // Reading Guide Overlay (Cue Line)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = guideOffset)
        ) {
            // Subtle gradient cue beam across the screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                CueGreen.copy(alpha = 0.9f),
                                CueGreenGlow.copy(alpha = 0.7f),
                                CueGreen.copy(alpha = 0.9f)
                            )
                        )
                    )
            )

            // Distinctive Cue Line Badge
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = 12.dp, y = (-12).dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CueGreen)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(CueBlack)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "READING LINE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = CueBlack
                )
            }
        }

        // Countdown Timer Overlay (Feature 7)
        AnimatedVisibility(
            visible = isCountdownActive,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CueBlack.copy(alpha = 0.88f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "GET READY",
                        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
                        color = CueGreen
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "$countdownRemaining",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 96.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = CueTextPrimary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedButton(
                        onClick = {
                            isCountdownActive = false
                            onPlayStateChange(false)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CueTextSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CueBorder),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("cancel_countdown_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cancel Countdown")
                    }
                }
            }
        }
    }
}
