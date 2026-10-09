package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.SpaceGrotesk

@Composable
fun DisclaimerBanner(
    modifier: Modifier = Modifier
) {
    Surface(
        color = AmberAlert.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AmberAlert.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("forensic_disclaimer_banner")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = "Disclaimer notice",
                tint = AmberAlert,
                modifier = Modifier
                    .size(22.dp)
                    .padding(top = 2.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "HEURISTIC FORENSICS NOTICE",
                    fontFamily = SpaceGrotesk,
                    fontSize = 12.sp,
                    color = AmberAlert,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Heuristic forensics, not a trained model. Results can be wrong: screenshots, filters, social-media compression and stripped metadata all cause false alarms, and good fakes can pass. Do not treat the score as proof.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}
