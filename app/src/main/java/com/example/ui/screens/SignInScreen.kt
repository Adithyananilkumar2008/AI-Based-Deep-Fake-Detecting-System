package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.AuthUiState
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.MintTeal
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun SignInScreen(
    authUiState: AuthUiState,
    onSignIn: (Activity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // App Icon with glowing border
            Surface(
                shape = CircleShape,
                color = CyberSurface,
                border = BorderStroke(2.dp, CyberCyan),
                shadowElevation = 12.dp,
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.deepfake_detector_icon_1791447037844),
                        contentDescription = "App logo",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Deepfake Detector",
                fontFamily = SpaceGrotesk,
                fontSize = 28.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Multi-Modal Forensics & Cloud Persistence",
                fontFamily = SpaceGrotesk,
                fontSize = 14.sp,
                color = CyberCyan
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Feature Highlights
            Surface(
                color = CyberSurface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CyberBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FeatureRow(
                        icon = Icons.Default.Shield,
                        tint = CyberCyan,
                        title = "Multi-Layer Forensics",
                        desc = "Error Level Analysis, 2D FFT spectrum, PRNU noise, and audio STFT."
                    )
                    FeatureRow(
                        icon = Icons.Default.CloudDone,
                        tint = MintTeal,
                        title = "Cloud Firestore Persistence",
                        desc = "Securely sync inspection logs, findings, and evidence across devices."
                    )
                    FeatureRow(
                        icon = Icons.Default.Lock,
                        tint = CyberCyan,
                        title = "Zero-Trust Security",
                        desc = "Authenticated personal database rules ensuring private audit logs."
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sign in with Google Button
            Button(
                onClick = { activity?.let { onSignIn(it) } },
                enabled = !authUiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyan,
                    contentColor = CyberDark
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("sign_in_google_button")
            ) {
                if (authUiState.isLoading) {
                    CircularProgressIndicator(
                        color = CyberDark,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Connecting to Google...",
                        fontFamily = SpaceGrotesk,
                        fontSize = 15.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sign in with Google",
                        fontFamily = SpaceGrotesk,
                        fontSize = 16.sp
                    )
                }
            }

            if (authUiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = authUiState.errorMessage,
                    color = Color(0xFFFF5252),
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    desc: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            color = tint.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(34.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = SpaceGrotesk,
                fontSize = 13.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = TextSecondary
            )
        }
    }
}
