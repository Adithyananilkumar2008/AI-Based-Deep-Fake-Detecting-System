package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.WarningAmber
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.MintTeal
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElaExplanationSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CyberSurface,
        modifier = Modifier.testTag("ela_explanation_sheet")
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
                    text = "HOW FORENSIC ELA WORKS",
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

            // Key concept card
            Surface(
                color = CyberCyan.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "The Core Principle:",
                        fontFamily = SpaceGrotesk,
                        fontSize = 14.sp,
                        color = CyberCyan
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Brighter areas recompress differently from the rest of the image, which can indicate editing or pasted regions.",
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            InfoSection(
                icon = Icons.Default.Layers,
                iconTint = MintTeal,
                title = "Compression Error Rates",
                description = "Every time a JPEG image is saved, 8x8 pixel blocks lose fine high-frequency details. When an image is unmodified, all blocks reach an error equilibrium. Spliced objects, face swaps, or inpainting introduce different compression levels that stand out brightly against untouched areas."
            )

            Spacer(modifier = Modifier.height(14.dp))

            InfoSection(
                icon = Icons.Default.AutoAwesome,
                iconTint = CyberCyan,
                title = "AI-Generated Diffusion Fingerprints",
                description = "Modern generative AI models (Midjourney, DALL-E, Stable Diffusion) generate smooth, synthetic gradients that lack physical camera sensor noise (PRNU). In the Noise Residual map, authentic photos show grainy sensor noise, while AI skin often appears abnormally smooth and uniform."
            )

            Spacer(modifier = Modifier.height(14.dp))

            InfoSection(
                icon = Icons.Default.WarningAmber,
                iconTint = AmberAlert,
                title = "Common False Alarms",
                description = "Heuristic forensics is not infallible. High-contrast natural edges, social media re-encoding (WhatsApp, Instagram), screenshot cropping, and heavy photography filters can induce uneven error levels. Always inspect the context before drawing conclusions."
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun InfoSection(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            color = iconTint.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = SpaceGrotesk,
                fontSize = 14.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = TextSecondary
            )
        }
    }
}
