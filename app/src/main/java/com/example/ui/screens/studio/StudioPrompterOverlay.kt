package com.example.ui.screens.studio

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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PrompterSettings
import com.example.model.Script
import com.example.ui.theme.CueBlack
import com.example.ui.theme.CueBorder
import com.example.ui.theme.CueGreen
import com.example.ui.theme.CueGreenGlow
import com.example.ui.theme.CueSurface
import com.example.ui.theme.CueTextMuted
import com.example.ui.theme.CueTextPrimary
import com.example.ui.theme.CueTextSecondary
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun StudioPrompterOverlay(
    script: Script,
    settings: PrompterSettings,
    opacity: Float,
    isPrompterPlaying: Boolean,
    isEyeGuideVisible: Boolean = true,
    onPrompterPlayToggle: () -> Unit,
    onSpeedChange: (Int) -> Unit,
    onFontSizeChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Smooth scrolling animation loop during recording
    val speedPxPerSec = settings.pixelsPerSecond
    LaunchedEffect(isPrompterPlaying, speedPxPerSec) {
        if (isPrompterPlaying) {
            var lastTime = System.nanoTime()
            while (isActive && isPrompterPlaying) {
                withFrameNanos { now ->
                    val elapsedNanos = now - lastTime
                    lastTime = now
                    val clampedSec = (elapsedNanos / 1_000_000_000f).coerceIn(0f, 0.1f)
                    val delta = speedPxPerSec * clampedSec
                    coroutineScope.launch {
                        if (scrollState.maxValue > 0 && scrollState.value >= scrollState.maxValue) {
                            // Reached end of script
                        } else {
                            scrollState.scrollBy(delta)
                        }
                    }
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(CueBlack.copy(alpha = opacity))
            .testTag("studio_prompter_overlay")
    ) {
        val viewportHeight = maxHeight
        // Place reading zone directly near top camera lens (22% from top) for natural eye contact
        val guideOffset = viewportHeight * 0.22f

        // Text Scroll Viewport (occupies large readable area)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(guideOffset))

                Text(
                    text = script.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CueGreen,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = script.content.ifEmpty { "(No script content available. Tap Select Script to choose a script.)" },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = settings.fontSizeSp.sp,
                        lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = CueTextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(viewportHeight - guideOffset))
            }
        }

        // Reading Guide Beam positioned near top camera lens
        if (isEyeGuideVisible) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = guideOffset)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    CueGreen.copy(alpha = 0.9f),
                                    CueGreenGlow,
                                    CueGreen.copy(alpha = 0.9f)
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = 12.dp, y = (-12).dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(CueGreen)
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EYE CONTACT • LOOK HERE",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.ExtraBold),
                        color = CueBlack
                    )
                }
            }
        }

        // Floating In-Viewfinder Prompter Controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(CueSurface.copy(alpha = 0.94f))
                .border(1.dp, CueBorder, RoundedCornerShape(28.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Rewind
            IconButton(
                onClick = {
                    coroutineScope.launch { scrollState.scrollTo(0) }
                },
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = "Rewind to top",
                    tint = CueTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Play / Pause
            IconButton(
                onClick = onPrompterPlayToggle,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CueGreen)
            ) {
                Icon(
                    imageVector = if (isPrompterPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPrompterPlaying) "Pause scroll" else "Start scroll",
                    tint = CueBlack,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Speed Stepper
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onSpeedChange((settings.scrollSpeedWpm - 10).coerceAtLeast(60)) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Text("-", color = CueGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Text(
                    text = "${settings.scrollSpeedWpm} WPM",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = CueTextPrimary,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
                IconButton(
                    onClick = { onSpeedChange((settings.scrollSpeedWpm + 10).coerceAtMost(300)) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Text("+", color = CueGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            // Text Size Stepper
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onFontSizeChange((settings.fontSizeSp - 2).coerceAtLeast(16)) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Text("A-", color = CueTextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Text(
                    text = "${settings.fontSizeSp}sp",
                    style = MaterialTheme.typography.labelSmall,
                    color = CueTextSecondary
                )
                IconButton(
                    onClick = { onFontSizeChange((settings.fontSizeSp + 2).coerceAtMost(48)) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Text("A+", color = CueTextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
