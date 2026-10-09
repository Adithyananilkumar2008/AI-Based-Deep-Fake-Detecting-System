package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forensics.FindingSeverity
import com.example.forensics.ForensicAnalysisReport
import com.example.forensics.ForensicFinding
import com.example.forensics.ForensicMode
import com.example.forensics.MediaType
import com.example.ui.ForensicsUiState
import com.example.ui.components.CompareSlider
import com.example.ui.components.DisclaimerBanner
import com.example.ui.components.PixelInspectorCard
import com.example.ui.components.RiskMeterCard
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.DangerRed
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.MintTeal
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    report: ForensicAnalysisReport,
    uiState: ForensicsUiState,
    onBack: () -> Unit,
    onSelectMode: (ForensicMode) -> Unit,
    onSplitFractionChange: (Float) -> Unit,
    onToggleSplit: () -> Unit,
    onTapPoint: (Float, Float) -> Unit,
    onClearInspector: () -> Unit,
    onUpdateParameters: (quality: Int, scale: Int) -> Unit,
    onOpenElaHelp: () -> Unit,
    onOpenExif: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { onBack() }

    var showTuneControls by remember { mutableStateOf(false) }
    var selectedVideoFrameIndex by remember { mutableIntStateOf(0) }
    var tempQuality by remember(report.qualityUsed) { mutableIntStateOf(report.qualityUsed) }
    var tempScale by remember(report.scaleUsed) { mutableIntStateOf(report.scaleUsed) }

    val activeForensicBitmap: Bitmap = when (uiState.selectedMode) {
        ForensicMode.ORIGINAL -> report.originalBitmap
        ForensicMode.ELA -> report.elaResult.elaBitmap
        ForensicMode.HEATMAP -> report.elaResult.heatBitmap
        ForensicMode.SPECTRAL -> report.spectralResult?.spectralBitmap ?: report.elaResult.heatBitmap
        ForensicMode.NOISE -> report.noiseResult.noiseBitmap
        ForensicMode.EDGE -> report.edgeResult.edgeBitmap
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDark)
            .testTag("analysis_screen")
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "${report.mediaType.label} Forensic Workbench",
                        fontFamily = SpaceGrotesk,
                        fontSize = 17.sp,
                        color = Color.White
                    )
                    Text(
                        text = "100% On-Device Analysis",
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        color = CyberCyan
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("back_to_home_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Return to Home",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(onClick = onOpenElaHelp) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Forensics documentation",
                        tint = MintTeal
                    )
                }
                if (report.mediaType == MediaType.IMAGE) {
                    IconButton(onClick = onOpenExif) {
                        Icon(
                            imageVector = Icons.Default.DataObject,
                            contentDescription = "View EXIF Metadata",
                            tint = CyberCyan
                        )
                    }
                }
                IconButton(
                    onClick = {
                        val shareText = buildString {
                            appendLine("DEEPFAKE DETECTOR FORENSIC REPORT")
                            appendLine("Media Type: ${report.mediaType.label}")
                            appendLine("Assessment: ${report.assessment.riskLevel.label} (${report.assessment.syntheticRiskScore}% Suspicion)")
                            appendLine("Summary: ${report.assessment.summary}")
                            appendLine("Heuristic Signals: ELA=${report.assessment.elaScore}%, Noise=${report.assessment.noiseScore}%, Edge=${report.assessment.edgeScore}%")
                            appendLine("\nNote: Heuristic forensics, not a trained model. Results can be wrong: screenshots, filters, social-media compression and stripped metadata all cause false alarms.")
                        }
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Forensic Report"))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share report",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = CyberSurface
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Media-specific display
            when (report.mediaType) {
                MediaType.IMAGE -> {
                    // Mode Selector Tabs for Image
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ForensicMode.values().forEach { mode ->
                                val isSelected = uiState.selectedMode == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectMode(mode) },
                                    label = {
                                        Text(text = mode.label, fontFamily = SpaceGrotesk, fontSize = 12.sp)
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyberCyan,
                                        selectedLabelColor = CyberDark,
                                        containerColor = CyberSurfaceVariant,
                                        labelColor = TextSecondary
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) CyberCyan else CyberBorder),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.testTag("mode_chip_${mode.name}")
                                )
                            }
                        }
                    }

                    // Mode Description Callout
                    item {
                        Surface(
                            color = CyberSurface,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CyberBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    color = CyberCyan.copy(alpha = 0.2f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = "i", fontFamily = JetBrainsMono, fontSize = 11.sp, color = CyberCyan)
                                    }
                                }
                                Text(
                                    text = uiState.selectedMode.description,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }

                    // Interactive Image Canvas & A/B Compare Slider with Pinch-to-Zoom
                    item {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                            ) {
                                com.example.ui.components.ZoomableBox(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    CompareSlider(
                                        originalBitmap = report.originalBitmap,
                                        forensicBitmap = activeForensicBitmap,
                                        splitFraction = uiState.compareSliderFraction,
                                        isSplitActive = uiState.isCompareSliderEnabled && uiState.selectedMode != ForensicMode.ORIGINAL,
                                        onFractionChange = onSplitFractionChange,
                                        onTapPoint = onTapPoint,
                                        forensicLabel = uiState.selectedMode.label,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                if (uiState.inspectorInfo != null) {
                                    PixelInspectorCard(
                                        info = uiState.inspectorInfo,
                                        onDismiss = onClearInspector,
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = onToggleSplit,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (uiState.isCompareSliderEnabled) CyberCyan.copy(alpha = 0.2f) else CyberSurfaceVariant,
                                            contentColor = if (uiState.isCompareSliderEnabled) CyberCyan else TextSecondary
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Compare, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = if (uiState.isCompareSliderEnabled) "Split: ON" else "Split: OFF", fontFamily = SpaceGrotesk, fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = { showTuneControls = !showTuneControls },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (showTuneControls) MintTeal.copy(alpha = 0.2f) else CyberSurfaceVariant,
                                            contentColor = if (showTuneControls) MintTeal else TextSecondary
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Tune ELA", fontFamily = SpaceGrotesk, fontSize = 11.sp)
                                    }
                                }

                                Text(text = "Tap to inspect pixels", fontFamily = JetBrainsMono, fontSize = 10.sp, color = TextMuted)
                            }
                        }
                    }

                    // Tuning panel
                    if (showTuneControls) {
                        item {
                            Surface(
                                color = CyberSurface,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CyberBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "ELA SENSITIVITY CALIBRATION", fontFamily = SpaceGrotesk, fontSize = 12.sp, color = MintTeal)
                                        IconButton(onClick = { showTuneControls = false }, modifier = Modifier.size(24.dp)) {
                                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = "Recompression Quality", fontSize = 12.sp, color = TextSecondary)
                                        Text(text = "$tempQuality%", fontFamily = JetBrainsMono, fontSize = 12.sp, color = CyberCyan)
                                    }
                                    Slider(
                                        value = tempQuality.toFloat(),
                                        onValueChange = { tempQuality = it.roundToInt() },
                                        valueRange = 75f..98f,
                                        steps = 22,
                                        colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan, inactiveTrackColor = Color.White.copy(alpha = 0.1f))
                                    )

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = "Error Amplification Gain", fontSize = 12.sp, color = TextSecondary)
                                        Text(text = "${tempScale}x", fontFamily = JetBrainsMono, fontSize = 12.sp, color = MintTeal)
                                    }
                                    Slider(
                                        value = tempScale.toFloat(),
                                        onValueChange = { tempScale = it.roundToInt() },
                                        valueRange = 10f..40f,
                                        steps = 29,
                                        colors = SliderDefaults.colors(thumbColor = MintTeal, activeTrackColor = MintTeal, inactiveTrackColor = Color.White.copy(alpha = 0.1f))
                                    )

                                    Button(
                                        onClick = { onUpdateParameters(tempQuality, tempScale) },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = CyberDark),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().height(40.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Recalculate ELA Heatmap", fontFamily = SpaceGrotesk, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                MediaType.VIDEO -> {
                    // Video Specific Filmstrip & Timeline Analysis
                    val vResult = report.videoResult
                    if (vResult != null) {
                        item {
                            Surface(
                                color = CyberSurface,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, CyberBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "SAMPLED TIMELINE KEYFRAMES",
                                            fontFamily = SpaceGrotesk,
                                            fontSize = 12.sp,
                                            color = MintTeal
                                        )
                                        Text(
                                            text = "${vResult.width}x${vResult.height} • ${String.format("%.1f", vResult.durationSeconds)}s",
                                            fontFamily = JetBrainsMono,
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Filmstrip of frames
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        vResult.sampledFrames.forEachIndexed { index, frame ->
                                            val isSelected = selectedVideoFrameIndex == index
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.clickable { selectedVideoFrameIndex = index }
                                            ) {
                                                Image(
                                                    bitmap = frame.asImageBitmap(),
                                                    contentDescription = "Keyframe $index",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(76.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .then(
                                                            if (isSelected) Modifier.background(CyberCyan).padding(2.dp)
                                                            else Modifier
                                                        )
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "T+${index * 25}%",
                                                    fontFamily = JetBrainsMono,
                                                    fontSize = 10.sp,
                                                    color = if (isSelected) CyberCyan else TextMuted
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Active selected frame preview with ELA overlay
                                    if (selectedVideoFrameIndex < vResult.sampledFrames.size) {
                                        val curFrame = vResult.sampledFrames[selectedVideoFrameIndex]
                                        val curEla = vResult.frameElaResults.getOrNull(selectedVideoFrameIndex) ?: curFrame

                                        Text(
                                            text = "Frame ${selectedVideoFrameIndex + 1} Temporal ELA Inspection",
                                            fontFamily = SpaceGrotesk,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Image(
                                                    bitmap = curFrame.asImageBitmap(),
                                                    contentDescription = "Raw frame",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(140.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                )
                                                Text(
                                                    text = "RAW CAPTURE",
                                                    fontFamily = JetBrainsMono,
                                                    fontSize = 10.sp,
                                                    color = TextMuted,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Image(
                                                    bitmap = curEla.asImageBitmap(),
                                                    contentDescription = "ELA frame",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(140.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                )
                                                Text(
                                                    text = "ERROR LEVEL MAP",
                                                    fontFamily = JetBrainsMono,
                                                    fontSize = 10.sp,
                                                    color = CyberCyan,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                MediaType.AUDIO -> {
                    // Audio Spectrogram Visualization Card
                    val aResult = report.audioResult
                    if (aResult != null) {
                        item {
                            Surface(
                                color = CyberSurface,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, CyberBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "HIGH-RESOLUTION SPECTROGRAM (FFT)",
                                            fontFamily = SpaceGrotesk,
                                            fontSize = 12.sp,
                                            color = CyberCyan
                                        )
                                        Text(
                                            text = "${aResult.sampleRate} Hz • Mono",
                                            fontFamily = JetBrainsMono,
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Spectrogram Canvas
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(170.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black)
                                    ) {
                                        Image(
                                            bitmap = aResult.spectrogramBitmap.asImageBitmap(),
                                            contentDescription = "Audio Spectrogram",
                                            contentScale = ContentScale.FillBounds,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        // Frequency Axis Legend Overlays
                                        Column(
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(6.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("22 kHz", fontFamily = JetBrainsMono, fontSize = 9.sp, color = Color.White.copy(alpha = 0.7f))
                                        }
                                        Column(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(6.dp)
                                        ) {
                                            Text("0 kHz", fontFamily = JetBrainsMono, fontSize = 9.sp, color = Color.White.copy(alpha = 0.7f))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Key Audio Metrics
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(text = "High-Freq Cutoff", fontSize = 11.sp, color = TextSecondary)
                                            Text(
                                                text = "${String.format("%.1f", aResult.cutoffKhz)} kHz",
                                                fontFamily = JetBrainsMono,
                                                fontSize = 13.sp,
                                                color = if (aResult.cutoffKhz < 14f) DangerRed else SafeGreen
                                            )
                                        }
                                        Column {
                                            Text(text = "Noise Floor", fontSize = 11.sp, color = TextSecondary)
                                            Text(
                                                text = "${aResult.noiseFloorDb.toInt()} dB",
                                                fontFamily = JetBrainsMono,
                                                fontSize = 13.sp,
                                                color = Color.White
                                            )
                                        }
                                        Column {
                                            Text(text = "Zero Silence Ratio", fontSize = 11.sp, color = TextSecondary)
                                            Text(
                                                text = "${(aResult.deadSilenceRatio * 100).toInt()}%",
                                                fontFamily = JetBrainsMono,
                                                fontSize = 13.sp,
                                                color = if (aResult.deadSilenceRatio > 0.08f) DangerRed else SafeGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Universal Risk Assessment Card
            item {
                RiskMeterCard(assessment = report.assessment)
            }

            // Key Findings List
            item {
                Text(
                    text = "FORENSIC FINDINGS",
                    fontFamily = SpaceGrotesk,
                    fontSize = 13.sp,
                    color = CyberCyan,
                    letterSpacing = 0.5.sp
                )
            }

            items(report.assessment.findings) { finding ->
                FindingCard(finding = finding)
            }

            // Cryptographic Data Integrity Card
            item {
                Surface(
                    color = CyberSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CyberBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CRYPTOGRAPHIC PROVENANCE & INTEGRITY",
                                fontFamily = SpaceGrotesk,
                                fontSize = 12.sp,
                                color = CyberCyan,
                                letterSpacing = 0.5.sp
                            )
                            Surface(
                                color = if (report.c2paResult?.hasC2paManifest == true) MintTeal.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (report.c2paResult?.hasC2paManifest == true) "C2PA VERIFIED" else "STANDARD FILE",
                                    fontFamily = JetBrainsMono,
                                    fontSize = 10.sp,
                                    color = if (report.c2paResult?.hasC2paManifest == true) MintTeal else TextMuted,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(text = "SHA-256 Digest:", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = report.sha256Hash.ifEmpty { "CALCULATED_ON_DEVICE" },
                            fontFamily = JetBrainsMono,
                            fontSize = 11.sp,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(text = "MD5 Digest:", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = report.md5Hash.ifEmpty { "CALCULATED_ON_DEVICE" },
                            fontFamily = JetBrainsMono,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // Report Export Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { com.example.forensics.ForensicReportExporter.copyToClipboard(context, report) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan.copy(alpha = 0.2f),
                            contentColor = CyberCyan
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(text = "Copy Audit Report", fontFamily = SpaceGrotesk, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { com.example.forensics.ForensicReportExporter.shareReport(context, report) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = CyberDark
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(text = "Export Evidence", fontFamily = SpaceGrotesk, fontSize = 12.sp)
                    }
                }
            }

            // Mandatory Educational Disclaimer
            item {
                DisclaimerBanner()
            }

            // Bottom Actions
            item {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberSurfaceVariant,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CyberBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Scan Another Media File",
                        fontFamily = SpaceGrotesk,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FindingCard(finding: ForensicFinding) {
    val (statusColor, badgeLabel) = when (finding.severity) {
        FindingSeverity.ALERT -> DangerRed to "ALERT"
        FindingSeverity.WARNING -> AmberAlert to "SUSPICIOUS"
        FindingSeverity.INFO -> SafeGreen to "ORGANIC"
    }

    Surface(
        color = CyberSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CyberBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = finding.category.uppercase(),
                    fontFamily = SpaceGrotesk,
                    fontSize = 11.sp,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = badgeLabel,
                        color = statusColor,
                        fontFamily = JetBrainsMono,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = finding.title,
                fontFamily = SpaceGrotesk,
                fontSize = 14.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = finding.detail,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = TextSecondary
            )
        }
    }
}
