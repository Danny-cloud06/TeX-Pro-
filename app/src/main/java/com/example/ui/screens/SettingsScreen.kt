package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CueBlack
import com.example.ui.theme.CueBorder
import com.example.ui.theme.CueCharcoal
import com.example.ui.theme.CueGreen
import com.example.ui.theme.CueGreenContainer
import com.example.ui.theme.CueSurface
import com.example.ui.theme.CueSurfaceElevated
import com.example.ui.theme.CueTextMuted
import com.example.ui.theme.CueTextPrimary
import com.example.ui.theme.CueTextSecondary

@Composable
fun SettingsScreen(
    onActionNotice: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var defaultWpm by remember { mutableIntStateOf(130) }
    var defaultFontSize by remember { mutableIntStateOf(28) }
    var isFrontCameraDefault by remember { mutableStateOf(true) }
    var isMicrophoneEnabled by remember { mutableStateOf(true) }
    var isEyeGuideBeamEnabled by remember { mutableStateOf(true) }
    var isFullHdQuality by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        SectionHeader(
            title = "Settings",
            subtitle = "Preferences, teleprompter pacing, camera quality, and local storage."
        )

        // 1. Camera & Video Recording
        SettingsSectionHeader(title = "CAMERA & RECORDING", icon = Icons.Default.CameraAlt)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CueSurface)
                .border(1.dp, CueBorder, RoundedCornerShape(12.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Default Lens Toggle
            SettingsToggleRow(
                title = "Default to Front Selfie Camera",
                subtitle = "Opens front lens for talking-head presentation",
                isChecked = isFrontCameraDefault,
                onCheckedChange = {
                    isFrontCameraDefault = it
                    onActionNotice(if (it) "Default lens: Front Camera" else "Default lens: Rear Camera")
                }
            )

            // Quality
            SettingsToggleRow(
                title = "1080p Full HD Recording",
                subtitle = if (isFullHdQuality) "High-definition 1080p recording" else "Standard 720p HD recording",
                isChecked = isFullHdQuality,
                onCheckedChange = {
                    isFullHdQuality = it
                    onActionNotice(if (it) "Recording resolution: 1080p FHD" else "Recording resolution: 720p HD")
                }
            )

            // Microphone
            SettingsToggleRow(
                title = "Record Audio with Microphone",
                subtitle = "Captures synchronized presenter commentary",
                isChecked = isMicrophoneEnabled,
                onCheckedChange = {
                    isMicrophoneEnabled = it
                    onActionNotice(if (it) "Microphone enabled" else "Microphone muted")
                }
            )

            // Storage Location
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CueSurfaceElevated)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = CueGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Saved Videos Destination", style = MaterialTheme.typography.labelSmall, color = CueTextSecondary)
                    Text("Movies/TeX (Local Device Storage)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = CueTextPrimary)
                }
            }
        }

        // 2. Teleprompter Display & Pacing
        SettingsSectionHeader(title = "TELEPROMPTER PACING & DISPLAY", icon = Icons.Default.Speed)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CueSurface)
                .border(1.dp, CueBorder, RoundedCornerShape(12.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Scroll Speed Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Default Reading Speed", style = MaterialTheme.typography.bodyMedium, color = CueTextPrimary)
                Text("$defaultWpm WPM", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = CueGreen)
            }
            Slider(
                value = defaultWpm.toFloat(),
                onValueChange = { defaultWpm = it.toInt() },
                valueRange = 60f..300f,
                colors = SliderDefaults.colors(thumbColor = CueGreen, activeTrackColor = CueGreen)
            )

            // Font Size Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Default Text Size", style = MaterialTheme.typography.bodyMedium, color = CueTextPrimary)
                Text("${defaultFontSize}sp", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = CueGreen)
            }
            Slider(
                value = defaultFontSize.toFloat(),
                onValueChange = { defaultFontSize = it.toInt() },
                valueRange = 18f..48f,
                colors = SliderDefaults.colors(thumbColor = CueGreen, activeTrackColor = CueGreen)
            )

            // Eye Line Guide Beam
            SettingsToggleRow(
                title = "Eye-Line Alignment Guide Beam",
                subtitle = "Visual guide line pinned near camera punch-hole",
                isChecked = isEyeGuideBeamEnabled,
                onCheckedChange = {
                    isEyeGuideBeamEnabled = it
                    onActionNotice(if (it) "Eye guide active" else "Eye guide hidden")
                }
            )
        }

        // 3. Green-Screen & Chroma Key
        SettingsSectionHeader(title = "GREEN-SCREEN & CHROMA KEY", icon = Icons.Default.ColorLens)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CueSurface)
                .border(1.dp, CueBorder, RoundedCornerShape(12.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Solid Chroma Key Studio",
                style = MaterialTheme.typography.titleMedium,
                color = CueTextPrimary
            )
            Text(
                text = "TeX provides a dependable Chroma Green (#00FF00) broadcast mode. This allows creators to record clean, artifact-free footage that keys effortlessly into Premiere Pro, CapCut, DaVinci Resolve, or Final Cut without dropped frames.",
                style = MaterialTheme.typography.bodySmall,
                color = CueTextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF00FF00).copy(alpha = 0.12f))
                    .border(1.dp, Color(0xFF00FF00).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00FF00))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Broadcast Chroma Key Green • Standard #00FF00", color = Color(0xFF00DD00), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
        }

        // 4. Eye-Contact Intelligence
        SettingsSectionHeader(title = "EYE-CONTACT CORRECTION STATUS", icon = Icons.Default.RemoveRedEye)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CueSurface)
                .border(1.dp, CueBorder, RoundedCornerShape(12.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.RemoveRedEye,
                    contentDescription = null,
                    tint = CueGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Natural Optical Eye Contact Active",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = CueGreen
                )
            }
            Text(
                text = "TeX aligns the active script lines immediately adjacent to your camera sensor (< 3° divergence angle). This achieves natural, authentic eye contact with your viewers without the uncanny artificial artifacts of synthetic eye generation.",
                style = MaterialTheme.typography.bodySmall,
                color = CueTextSecondary
            )
            Text(
                text = "Note: Real-time generative pupil redirection requires dedicated hardware neural NPU acceleration. TeX uses natural optical alignment for maximum reliability on all Android devices.",
                style = MaterialTheme.typography.labelSmall,
                color = CueTextMuted
            )
        }

        // 5. About TeX
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CueCharcoal)
                .border(1.dp, CueBorder, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(CueGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TeX Teleprompter",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CueTextPrimary
                )
            }
            Text(
                text = "Version 1.0 • Professional Talking-Head Teleprompter for Mobile Creators",
                style = MaterialTheme.typography.bodySmall,
                color = CueTextSecondary
            )
            Text(
                text = "All data, scripts, and video recordings remain strictly private and local to your device.",
                style = MaterialTheme.typography.labelSmall,
                color = CueTextMuted
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CueGreen,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            color = CueGreen
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = CueTextPrimary)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = CueTextSecondary)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CueGreen,
                checkedTrackColor = CueGreenContainer
            )
        )
    }
}
