package com.example.ui.screens.studio

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.model.RecordedVideo
import com.example.ui.theme.CueBlack
import com.example.ui.theme.CueBorder
import com.example.ui.theme.CueGreen
import com.example.ui.theme.CueRed
import com.example.ui.theme.CueSurface
import com.example.ui.theme.CueSurfaceElevated
import com.example.ui.theme.CueTextMuted
import com.example.ui.theme.CueTextPrimary
import com.example.ui.theme.CueTextSecondary

@Composable
fun RecordedVideoPreviewDialog(
    video: RecordedVideo,
    onDismiss: () -> Unit,
    onDiscard: (RecordedVideo) -> Unit
) {
    val context = LocalContext.current
    var showDiscardConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Recording Complete",
                    style = MaterialTheme.typography.titleLarge,
                    color = CueTextPrimary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(CueSurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = video.formattedDuration,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = CueGreen
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Video Media Card Preview Mockup
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CueBlack)
                        .border(1.dp, CueBorder, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleOutline,
                            contentDescription = "Play recording",
                            tint = CueGreen,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "MP4 Video Ready",
                            style = MaterialTheme.typography.titleMedium,
                            color = CueTextPrimary
                        )
                    }
                }

                // File metadata details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CueSurfaceElevated)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("File:", style = MaterialTheme.typography.bodySmall, color = CueTextMuted)
                        Text(video.file.name, style = MaterialTheme.typography.bodySmall, color = CueTextPrimary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Size:", style = MaterialTheme.typography.bodySmall, color = CueTextMuted)
                        Text(video.fileSizeMb, style = MaterialTheme.typography.bodySmall, color = CueTextPrimary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Recorded:", style = MaterialTheme.typography.bodySmall, color = CueTextMuted)
                        Text(video.formattedDate, style = MaterialTheme.typography.bodySmall, color = CueTextPrimary)
                    }
                }

                var isSavedToGallery by remember { mutableStateOf(false) }

                if (isSavedToGallery) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CueGreen.copy(alpha = 0.15f))
                            .border(1.dp, CueGreen, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = CueGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Saved to Movies/TeX in your gallery.", color = CueGreen, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Text(
                    text = "Your recording is stored locally on device. Save directly to your gallery or share it.",
                    style = MaterialTheme.typography.labelSmall,
                    color = CueTextSecondary
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Share button
                OutlinedButton(
                    onClick = { shareOrSaveVideo(context, video) },
                    border = androidx.compose.foundation.BorderStroke(1.dp, CueBorder)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = CueGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", color = CueTextPrimary)
                }

                // Export / Save button to Movies/TeX
                Button(
                    onClick = {
                        val saved = saveVideoToTeXGallery(context, video.file)
                        if (saved) {
                            android.widget.Toast.makeText(context, "Saved to Movies/TeX", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CueGreen,
                        contentColor = CueBlack
                    ),
                    modifier = Modifier.testTag("export_video_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save to Phone", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = { showDiscardConfirm = true },
                    modifier = Modifier.testTag("discard_video_button")
                ) {
                    Text("Discard", color = CueRed)
                }

                TextButton(onClick = onDismiss) {
                    Text("Close", color = CueTextSecondary)
                }
            }
        },
        containerColor = CueSurface,
        modifier = Modifier.testTag("recorded_video_preview_dialog")
    )

    if (showDiscardConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirm = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = CueRed,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Discard Recording?",
                    style = MaterialTheme.typography.titleLarge,
                    color = CueTextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to discard this video recording? The video file will be removed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CueTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDiscardConfirm = false
                        onDiscard(video)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CueRed,
                        contentColor = CueTextPrimary
                    )
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirm = false }) {
                    Text("Cancel", color = CueTextSecondary)
                }
            },
            containerColor = CueSurface
        )
    }
}

private fun shareOrSaveVideo(context: Context, video: RecordedVideo) {
    try {
        val uri: Uri = try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                video.file
            )
        } catch (e: Exception) {
            Uri.fromFile(video.file)
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Save or Share TeX Video"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun saveVideoToTeXGallery(context: Context, sourceFile: java.io.File): Boolean {
    return try {
        val resolver = context.contentResolver
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Video.Media.DISPLAY_NAME, sourceFile.name)
            put(android.provider.MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(android.provider.MediaStore.Video.Media.RELATIVE_PATH, "${android.os.Environment.DIRECTORY_MOVIES}/TeX")
                put(android.provider.MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val videoUri = resolver.insert(android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (videoUri != null) {
            resolver.openOutputStream(videoUri)?.use { outStream ->
                java.io.FileInputStream(sourceFile).use { inStream ->
                    inStream.copyTo(outStream)
                }
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(android.provider.MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(videoUri, contentValues, null, null)
            }
            true
        } else {
            false
        }
    } catch (e: Exception) {
        false
    }
}
