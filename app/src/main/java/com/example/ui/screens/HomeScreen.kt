package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.ScanHistoryEntity
import com.example.forensics.MediaType
import com.example.forensics.SamplePreset
import com.example.ui.ForensicsUiState
import com.example.ui.components.CameraCaptureDialog
import com.example.ui.components.DisclaimerBanner
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    uiState: ForensicsUiState,
    samplePresets: List<SamplePreset>,
    onSelectTab: (MediaType) -> Unit,
    onSelectImage: (Uri) -> Unit,
    onCapturePhoto: (Bitmap) -> Unit,
    onSelectVideo: (Uri) -> Unit,
    onSelectAudio: (Uri) -> Unit,
    onSelectPreset: (SamplePreset) -> Unit,
    onDeleteScan: (String) -> Unit,
    onOpenElaHelp: () -> Unit,
    userEmail: String? = null,
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showCameraDialog by remember { mutableStateOf(false) }

    // Camera permission request launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showCameraDialog = true
        } else {
            Toast.makeText(
                context,
                "Camera permission is required to capture photos directly for forensic analysis.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Launchers for Photo, Video, and Audio pickers
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) onSelectImage(uri)
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) onSelectVideo(uri)
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) onSelectAudio(uri)
    }

    val filteredPresets = samplePresets.filter { it.mediaType == uiState.activeTab }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(CyberDark)
                .testTag("home_screen_lazy_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // User Account & Cloud Sync Status Bar
        if (!userEmail.isNullOrBlank()) {
            item {
                Surface(
                    color = CyberSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CyberBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = CyberCyan.copy(alpha = 0.2f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = userEmail.take(1).uppercase(),
                                        fontFamily = SpaceGrotesk,
                                        fontSize = 13.sp,
                                        color = CyberCyan
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = userEmail,
                                    fontFamily = SpaceGrotesk,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "☁️ Synced to Cloud Firestore",
                                    fontFamily = JetBrainsMono,
                                    fontSize = 10.sp,
                                    color = MintTeal
                                )
                            }
                        }

                        Button(
                            onClick = onSignOut,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.08f),
                                contentColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "Sign Out",
                                fontFamily = SpaceGrotesk,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Hero Banner Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CyberSurface,
                border = BorderStroke(1.dp, CyberBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.forensics_banner_1791447063909),
                            contentDescription = "Forensic scanner illustration",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            CyberSurface.copy(alpha = 0.95f)
                                        )
                                    )
                                )
                        )
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = CyberCyan.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = CyberCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "100% ON-DEVICE FORENSICS",
                                        fontFamily = SpaceGrotesk,
                                        fontSize = 10.sp,
                                        color = CyberCyan
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Deepfake Detector",
                            fontFamily = SpaceGrotesk,
                            fontSize = 24.sp,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Analyzes images, video frames, and audio tracks for signs of AI generation, cloning, or tampering. Runs locally on your device.",
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Media Type Selector Tabs (Image / Video / Audio)
        item {
            Surface(
                color = CyberSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CyberBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                TabRow(
                    selectedTabIndex = uiState.activeTab.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = CyberCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[uiState.activeTab.ordinal]),
                            color = CyberCyan,
                            height = 3.dp
                        )
                    }
                ) {
                    MediaType.values().forEach { tab ->
                        val isSelected = uiState.activeTab == tab
                        Tab(
                            selected = isSelected,
                            onClick = { onSelectTab(tab) },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = when (tab) {
                                            MediaType.IMAGE -> Icons.Default.Image
                                            MediaType.VIDEO -> Icons.Default.Movie
                                            MediaType.AUDIO -> Icons.Default.Mic
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) CyberCyan else TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = tab.label.uppercase(),
                                        fontFamily = SpaceGrotesk,
                                        fontSize = 12.sp,
                                        color = if (isSelected) CyberCyan else TextMuted
                                    )
                                }
                            },
                            modifier = Modifier.testTag("tab_${tab.name}")
                        )
                    }
                }
            }
        }

        // Upload / Capture Button specific to active tab
        item {
            when (uiState.activeTab) {
                MediaType.IMAGE -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Option 1: Live CameraX Capture
                        Button(
                            onClick = {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    showCameraDialog = true
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyan,
                                contentColor = CyberDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("camera_capture_button")
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Capture with CameraX (Live Sensor)", fontFamily = SpaceGrotesk, fontSize = 14.sp)
                        }

                        // Option 2: Photo Picker from Gallery
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = CyberCyan
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("select_image_button")
                        ) {
                            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Choose Photo from Gallery", fontFamily = SpaceGrotesk, fontSize = 13.sp)
                        }
                    }
                }

                MediaType.VIDEO -> {
                    Button(
                        onClick = {
                            videoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintTeal,
                            contentColor = CyberDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("select_video_button")
                    ) {
                        Icon(imageVector = Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Select Video to Analyze (Timeline & Frames)", fontFamily = SpaceGrotesk, fontSize = 14.sp)
                    }
                }

                MediaType.AUDIO -> {
                    Button(
                        onClick = {
                            audioPickerLauncher.launch("audio/*")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = CyberDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("select_audio_button")
                    ) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Select Audio File (Spectrogram & Cutoff)", fontFamily = SpaceGrotesk, fontSize = 14.sp)
                    }
                }
            }
        }

        // Disclaimer Notice
        item {
            DisclaimerBanner()
        }

        // ELA / Forensic Education Banner
        item {
            Surface(
                color = Color.White.copy(alpha = 0.03f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CyberBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenElaHelp() }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MintTeal,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "How Digital Forensics Works",
                                fontFamily = SpaceGrotesk,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Learn about ELA, sensor noise, and audio spectrograms",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    Text(
                        text = "GUIDE",
                        fontFamily = SpaceGrotesk,
                        fontSize = 11.sp,
                        color = MintTeal
                    )
                }
            }
        }

        // Sample Presets for current tab
        item {
            Column {
                Text(
                    text = "TRY ${uiState.activeTab.label.uppercase()} DEMO SAMPLES",
                    fontFamily = SpaceGrotesk,
                    fontSize = 13.sp,
                    color = CyberCyan,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                filteredPresets.forEach { preset ->
                    PresetCard(
                        preset = preset,
                        onClick = { onSelectPreset(preset) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        // Recent Scans History
        if (uiState.historyList.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "RECENT INSPECTION LOGS",
                            fontFamily = SpaceGrotesk,
                            fontSize = 12.sp,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "${uiState.historyList.size} SCANS",
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            items(uiState.historyList) { item ->
                HistoryItemCard(
                    scan = item,
                    onDelete = { onDeleteScan(item.id) }
                )
            }
        }

        // Live CameraX Capture Dialog
        if (showCameraDialog) {
            CameraCaptureDialog(
                onCapture = { bitmap ->
                    showCameraDialog = false
                    onCapturePhoto(bitmap)
                },
                onDismiss = {
                    showCameraDialog = false
                }
            )
        }
    }
}

@Composable
private fun PresetCard(
    preset: SamplePreset,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CyberSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CyberBorder),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("preset_card_${preset.id}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Image(
                painter = painterResource(id = preset.drawableRes),
                contentDescription = preset.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = preset.title,
                    fontFamily = SpaceGrotesk,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Text(
                    text = preset.subtitle,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${preset.mediaType.label.uppercase()} • ${preset.typeDescription.uppercase()}",
                    fontFamily = JetBrainsMono,
                    fontSize = 10.sp,
                    color = CyberCyan
                )
            }

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyan.copy(alpha = 0.15f),
                    contentColor = CyberCyan
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Analyze",
                    fontFamily = SpaceGrotesk,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    scan: ScanHistoryEntity,
    onDelete: () -> Unit
) {
    val dateString = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(scan.timestamp))
    val badgeColor = when (scan.riskLevel) {
        "HIGH" -> DangerRed
        "MODERATE" -> Color(0xFFFFB703)
        else -> SafeGreen
    }

    Surface(
        color = CyberSurface.copy(alpha = 0.6f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, CyberBorder.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = scan.imageLabel,
                        fontFamily = SpaceGrotesk,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Surface(
                        color = badgeColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${scan.syntheticRiskScore}% RISK",
                            fontFamily = JetBrainsMono,
                            fontSize = 10.sp,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$dateString • Forensic Score: ${scan.elaScore}%",
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete scan",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
