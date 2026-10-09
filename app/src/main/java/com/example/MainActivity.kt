package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AuthViewModel
import com.example.ui.ForensicsViewModel
import com.example.ui.components.ElaExplanationSheet
import com.example.ui.components.ExifInspectorSheet
import com.example.ui.screens.AnalysisScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.KnowledgeBaseScreen
import com.example.ui.screens.SignInScreen
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

enum class MainAppTab(val title: String, val icon: ImageVector) {
    WORKBENCH("Workbench", Icons.Default.Biotech),
    AUDIT("Audit Log", Icons.Default.ReceiptLong),
    GUIDE("Lab Guide", Icons.Default.MenuBook)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val authViewModel: AuthViewModel = viewModel()
                val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
                val user = authUiState.currentUser

                if (user == null) {
                    SignInScreen(
                        authUiState = authUiState,
                        onSignIn = { activity -> authViewModel.signInWithGoogle(activity) }
                    )
                } else {
                    val forensicsViewModel: ForensicsViewModel = viewModel()
                    LaunchedEffect(user.uid) {
                        forensicsViewModel.setAuthenticatedUser(user.uid)
                    }

                    DeepfakeDetectorApp(
                        viewModel = forensicsViewModel,
                        userEmail = user.email ?: user.displayName,
                        onSignOut = { authViewModel.signOut() }
                    )
                }
            }
        }
    }
}

@Composable
fun DeepfakeDetectorApp(
    viewModel: ForensicsViewModel,
    userEmail: String? = null,
    onSignOut: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var currentNavTab by remember { mutableStateOf(MainAppTab.WORKBENCH) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CyberDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState.currentReport == null) {
                NavigationBar(
                    containerColor = CyberSurface,
                    modifier = Modifier.testTag("main_navigation_bar")
                ) {
                    MainAppTab.values().forEach { tab ->
                        val isSelected = currentNavTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentNavTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontFamily = SpaceGrotesk,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyberDark,
                                selectedTextColor = CyberCyan,
                                indicatorColor = CyberCyan,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = Pair(currentNavTab, uiState.currentReport),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "main_screen_transition"
            ) { (tab, report) ->
                when {
                    report != null -> {
                        AnalysisScreen(
                            report = report,
                            uiState = uiState,
                            onBack = { viewModel.resetToHome() },
                            onSelectMode = { mode -> viewModel.setForensicMode(mode) },
                            onSplitFractionChange = { fraction -> viewModel.setCompareFraction(fraction) },
                            onToggleSplit = { viewModel.toggleCompareSlider() },
                            onTapPoint = { x, y -> viewModel.inspectPoint(x, y) },
                            onClearInspector = { viewModel.clearInspector() },
                            onUpdateParameters = { q, s -> viewModel.updateParameters(q, s) },
                            onOpenElaHelp = { viewModel.setElaHelpVisible(true) },
                            onOpenExif = { viewModel.setExifSheetVisible(true) }
                        )
                    }

                    tab == MainAppTab.AUDIT -> {
                        HistoryScreen(
                            scans = uiState.historyList,
                            onDeleteScan = { id -> viewModel.deleteScan(id) },
                            onClearAll = { viewModel.clearHistory() }
                        )
                    }

                    tab == MainAppTab.GUIDE -> {
                        KnowledgeBaseScreen()
                    }

                    else -> {
                        HomeScreen(
                            uiState = uiState,
                            samplePresets = viewModel.samplePresets,
                            onSelectTab = { t -> viewModel.setActiveTab(t) },
                            onSelectImage = { uri -> viewModel.analyzeImageUri(context, uri) },
                            onCapturePhoto = { bitmap -> viewModel.analyzeCapturedBitmap(context, bitmap) },
                            onSelectVideo = { uri -> viewModel.analyzeVideoUri(context, uri) },
                            onSelectAudio = { uri -> viewModel.analyzeAudioUri(context, uri) },
                            onSelectPreset = { preset -> viewModel.analyzePreset(context, preset) },
                            onDeleteScan = { id -> viewModel.deleteScan(id) },
                            onOpenElaHelp = { viewModel.setElaHelpVisible(true) },
                            userEmail = userEmail,
                            onSignOut = onSignOut
                        )
                    }
                }
            }

            // Futuristic loading dialog
            if (uiState.isLoading) {
                Dialog(onDismissRequest = {}) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CyberSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = CyberCyan,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "RUNNING FORENSICS",
                                fontFamily = SpaceGrotesk,
                                fontSize = 14.sp,
                                color = CyberCyan,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = uiState.loadingMessage.ifEmpty { "Executing on-device heuristic forensics..." },
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Bottom Sheets
            if (uiState.showElaHelp) {
                ElaExplanationSheet(
                    onDismiss = { viewModel.setElaHelpVisible(false) }
                )
            }

            if (uiState.showExifSheet && uiState.currentReport != null) {
                ExifInspectorSheet(
                    metadata = uiState.currentReport!!.exifMetadata,
                    onDismiss = { viewModel.setExifSheetVisible(false) }
                )
            }
        }
    }
}
