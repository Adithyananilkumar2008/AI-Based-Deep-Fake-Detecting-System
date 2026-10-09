package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.MintTeal
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.TextSecondary

@Composable
fun KnowledgeBaseScreen(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDark)
            .testTag("knowledge_base_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "FORENSIC LABORATORY GUIDE",
                    fontFamily = SpaceGrotesk,
                    fontSize = 20.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Technical taxonomy, physics of synthetic media, and methodology principles.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        item {
            GuideCard(
                icon = Icons.Default.Layers,
                iconTint = CyberCyan,
                title = "Error Level Analysis (ELA)",
                subtitle = "Compression Quantization Discrepancy",
                body = "JPEG compression encodes images using 8x8 discrete cosine transform (DCT) blocks. Every save cycle reduces high-frequency detail until error equilibrium is reached. When an element is pasted, face-swapped, or inpainted, its error rate differs sharply from untouched background pixels. Under calibrated recompression (90%–95%), tampered patches stand out with high amplified brightness."
            )
        }

        item {
            GuideCard(
                icon = Icons.Default.AutoAwesome,
                iconTint = MintTeal,
                title = "2D Fourier (FFT) Spectral Analysis",
                subtitle = "Diffusion Upsampling Grid Artifacts",
                body = "Generative diffusion models (Midjourney, Stable Diffusion, Flux, StyleGAN) synthesize images via transposed convolutions and multi-head attention grids. In the 2D frequency domain, these operations introduce unnatural periodic spikes, crosshatch energy, and circular harmonic rings. Optical camera photographs naturally exhibit smooth radial 1/f power falloff."
            )
        }

        item {
            GuideCard(
                icon = Icons.Default.Security,
                iconTint = CyberCyan,
                title = "PRNU Sensor Noise Fingerprinting",
                subtitle = "Photo-Response Non-Uniformity",
                body = "Physical camera sensors possess unique micro-imperfections in their silicon pixels that impart consistent photon noise across the entire image. AI generators produce mathematically smooth color gradients without physical sensor noise, causing facial skin to exhibit abnormal flat smoothness."
            )
        }

        item {
            GuideCard(
                icon = Icons.Default.Movie,
                iconTint = MintTeal,
                title = "Video Temporal Continuity & Morphing",
                subtitle = "Inter-Frame Delta & Seam Flickering",
                body = "Deepfake video generation struggles to maintain 3D geometric consistency across time. Successive frames reveal micro-jitter along facial perimeters, lighting vector mismatches between the neck and face, and synthetic background warping when objects occlude the head."
            )
        }

        item {
            GuideCard(
                icon = Icons.Default.GraphicEq,
                iconTint = CyberCyan,
                title = "Audio Spectrogram & Vocoder Analysis",
                subtitle = "Neural Speech Brickwalls & Zero Silence",
                body = "Neural TTS models (such as ElevenLabs, Tortoise, and Bark) typically generate audio at 16 kHz or 24 kHz, creating an artificial brickwall frequency cutoff (energy drops to zero above 11–14 kHz). Additionally, AI speech often inserts mathematical zero silence (< -90 dB) between words, whereas organic microphones always capture ambient room tone (-50 dB)."
            )
        }

        item {
            GuideCard(
                icon = Icons.Default.VerifiedUser,
                iconTint = MintTeal,
                title = "C2PA & Content Credentials",
                subtitle = "Cryptographic Provenance Standard",
                body = "The Coalition for Content Provenance and Authenticity (C2PA) standard embeds cryptographically signed JUMBF manifest boxes. In-camera signing (Leica, Sony, Nikon) proves hardware capture, while generative AI platforms embed tamper-evident AI generation claims."
            )
        }

        item {
            GuideCard(
                icon = Icons.Default.WarningAmber,
                iconTint = AmberAlert,
                title = "False Positives & Limits of Forensics",
                subtitle = "Contextual Interpretation Mandate",
                body = "Heuristic algorithms do not replace human editorial judgment. Heavy social media recompression (WhatsApp, Instagram downscaling), multiple JPEG saves, camera night mode noise reduction, and aggressive photography filters can produce false anomalies. Scores should be treated as probability indicators."
            )
        }
    }
}

@Composable
private fun GuideCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    body: String
) {
    Surface(
        color = CyberSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CyberBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = iconTint.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(38.dp)
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

                Column {
                    Text(
                        text = title,
                        fontFamily = SpaceGrotesk,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontFamily = SpaceGrotesk,
                        fontSize = 11.sp,
                        color = iconTint
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = body,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = TextSecondary
            )
        }
    }
}
