package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.InspectorPixelInfo
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SpaceGrotesk

@Composable
fun PixelInspectorCard(
    info: InspectorPixelInfo,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CyberSurface.copy(alpha = 0.95f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("pixel_inspector_card")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "Inspector point",
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = " INSPECTOR (X: ${info.x}, Y: ${info.y})",
                        fontFamily = SpaceGrotesk,
                        fontSize = 12.sp,
                        color = CyberCyan
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close inspector",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color swatch
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                Color(info.originalR, info.originalG, info.originalB),
                                shape = CircleShape
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    )
                    Text(
                        text = " RGB(${info.originalR}, ${info.originalG}, ${info.originalB})",
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }

                // Error level badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ELA Error: ",
                        fontFamily = SpaceGrotesk,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${info.errorValue}/255",
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        color = if (info.isHotspot) DangerRed else SafeGreen
                    )
                    if (info.isHotspot) {
                        Surface(
                            color = DangerRed.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Text(
                                text = "ANOMALY",
                                color = DangerRed,
                                fontFamily = JetBrainsMono,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
