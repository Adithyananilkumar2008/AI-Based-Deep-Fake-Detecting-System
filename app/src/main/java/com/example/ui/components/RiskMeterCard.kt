package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forensics.ForensicAssessment
import com.example.forensics.RiskLevel
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.MintTeal
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun RiskMeterCard(
    assessment: ForensicAssessment,
    modifier: Modifier = Modifier
) {
    val riskColor = Color(assessment.riskLevel.hexColor)
    val animatedScore by animateFloatAsState(
        targetValue = assessment.syntheticRiskScore / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "risk_score_anim"
    )

    Surface(
        color = CyberSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CyberBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("risk_meter_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SYNTHETIC RISK ANALYSIS",
                    fontFamily = SpaceGrotesk,
                    fontSize = 13.sp,
                    color = CyberCyan,
                    letterSpacing = 0.5.sp
                )

                Surface(
                    color = riskColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, riskColor.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = assessment.riskLevel.label.uppercase(),
                        color = riskColor,
                        fontFamily = SpaceGrotesk,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main score gauge row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Gauge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(92.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(92.dp),
                        color = Color.White.copy(alpha = 0.08f),
                        strokeWidth = 9.dp
                    )
                    CircularProgressIndicator(
                        progress = { animatedScore },
                        modifier = Modifier.size(92.dp),
                        color = riskColor,
                        strokeWidth = 9.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${assessment.syntheticRiskScore}%",
                            fontFamily = JetBrainsMono,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Text(
                            text = "RISK",
                            fontFamily = SpaceGrotesk,
                            fontSize = 9.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = assessment.summary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sub-scores breakdown
            Text(
                text = "HEURISTIC METRIC BREAKDOWN",
                fontFamily = SpaceGrotesk,
                fontSize = 11.sp,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            MetricRow(
                label = "Error Level Divergence (ELA)",
                value = assessment.elaScore,
                activeColor = if (assessment.elaScore > 60) DangerRed else CyberCyan
            )
            Spacer(modifier = Modifier.height(8.dp))

            MetricRow(
                label = "Sensor Noise / Flatness Index",
                value = assessment.noiseScore,
                activeColor = if (assessment.noiseScore > 60) DangerRed else MintTeal
            )
            Spacer(modifier = Modifier.height(8.dp))

            MetricRow(
                label = "Boundary & Seam Anomaly",
                value = assessment.edgeScore,
                activeColor = if (assessment.edgeScore > 60) DangerRed else CyberCyan
            )
            Spacer(modifier = Modifier.height(8.dp))

            MetricRow(
                label = "EXIF / Software Parameters",
                value = assessment.metadataScore,
                activeColor = if (assessment.metadataScore > 60) DangerRed else SafeGreen
            )

            if (assessment.spectralScore > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                MetricRow(
                    label = "2D Fourier (FFT) Grid Anomaly",
                    value = assessment.spectralScore,
                    activeColor = if (assessment.spectralScore > 60) DangerRed else MintTeal
                )
            }
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: Int,
    activeColor: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextSecondary
            )
            Text(
                text = "$value%",
                fontFamily = JetBrainsMono,
                fontSize = 12.sp,
                color = activeColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = activeColor,
            trackColor = Color.White.copy(alpha = 0.08f),
            strokeCap = StrokeCap.Round
        )
    }
}
