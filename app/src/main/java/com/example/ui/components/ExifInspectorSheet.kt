package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forensics.ExifMetadata
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExifInspectorSheet(
    metadata: ExifMetadata,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CyberSurface,
        modifier = Modifier.testTag("exif_inspector_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "METADATA & PROVENANCE",
                    fontFamily = SpaceGrotesk,
                    fontSize = 18.sp,
                    color = CyberCyan
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close sheet",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // AI tags warning if present
            if (metadata.hasAiPromptTags) {
                Surface(
                    color = DangerRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = DangerRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "AI Generator Signature Detected!",
                                fontFamily = SpaceGrotesk,
                                fontSize = 14.sp,
                                color = DangerRed
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Found AI tags: ${metadata.aiTagsFound.joinToString(", ")}",
                                fontFamily = JetBrainsMono,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Exif Table
            ExifRow("Camera Make", metadata.cameraMake ?: "Not recorded")
            ExifRow("Camera Model", metadata.cameraModel ?: "Not recorded")
            ExifRow("Software Tag", metadata.software ?: "Not specified")
            ExifRow("Exposure Time", metadata.exposureTime ?: "N/A")
            ExifRow("Aperture / F-Stop", metadata.fNumber?.let { "f/$it" } ?: "N/A")
            ExifRow("ISO Rating", metadata.iso ?: "N/A")
            ExifRow("Capture Date", metadata.dateTime ?: "N/A")
            ExifRow("Format / Compression", metadata.compressionType)

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ExifRow(label: String, value: String) {
    Surface(
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, CyberBorder.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = TextSecondary
            )
            Text(
                text = value,
                fontFamily = JetBrainsMono,
                fontSize = 12.sp,
                color = if (value.contains("Not") || value == "N/A") TextMuted else Color.White
            )
        }
    }
}
