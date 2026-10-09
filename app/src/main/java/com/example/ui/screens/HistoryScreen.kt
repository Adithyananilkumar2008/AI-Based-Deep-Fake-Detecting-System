package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScanHistoryEntity
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
fun HistoryScreen(
    scans: List<ScanHistoryEntity>,
    onDeleteScan: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredList = scans.filter { item ->
        val matchesSearch = searchQuery.isBlank() || item.imageLabel.contains(searchQuery, ignoreCase = true)
        val matchesCategory = when (selectedFilter) {
            "HIGH" -> item.riskLevel == "HIGH"
            "MODERATE" -> item.riskLevel == "MODERATE"
            "LOW" -> item.riskLevel == "LOW"
            else -> true
        }
        matchesSearch && matchesCategory
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDark)
            .padding(16.dp)
            .testTag("history_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "FORENSIC AUDIT ARCHIVE",
                    fontFamily = SpaceGrotesk,
                    fontSize = 20.sp,
                    color = Color.White
                )
                Text(
                    text = "${scans.size} Scans Logged in Database",
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    color = CyberCyan
                )
            }

            if (scans.isNotEmpty()) {
                IconButton(onClick = onClearAll) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear all scans",
                        tint = TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search logs by label or filename...", fontSize = 13.sp, color = TextMuted) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = CyberBorder,
                focusedContainerColor = CyberSurface,
                unfocusedContainerColor = CyberSurface,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_scans_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL" to "All Logs", "HIGH" to "High Suspicion", "MODERATE" to "Inconclusive", "LOW" to "Authentic").forEach { (key, label) ->
                val isSelected = selectedFilter == key
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = key },
                    label = { Text(text = label, fontFamily = SpaceGrotesk, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberCyan,
                        selectedLabelColor = CyberDark,
                        containerColor = CyberSurfaceVariant,
                        labelColor = TextSecondary
                    ),
                    border = BorderStroke(1.dp, if (isSelected) CyberCyan else CyberBorder),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredList.isEmpty()) {
            Surface(
                color = CyberSurface,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CyberBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Forensic Records Found",
                        fontFamily = SpaceGrotesk,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No entries match your search query." else "Scans you perform will appear here in the audit log.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    AuditLogItemCard(
                        scan = item,
                        onDelete = { onDeleteScan(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AuditLogItemCard(
    scan: ScanHistoryEntity,
    onDelete: () -> Unit
) {
    val dateString = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(scan.timestamp))
    val (badgeColor, verdictLabel) = when (scan.riskLevel) {
        "HIGH" -> DangerRed to "LIKELY SYNTHETIC"
        "MODERATE" -> Color(0xFFFFB703) to "INCONCLUSIVE"
        else -> SafeGreen to "AUTHENTIC"
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = badgeColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = verdictLabel,
                            color = badgeColor,
                            fontFamily = JetBrainsMono,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "${scan.syntheticRiskScore}% RISK",
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        color = badgeColor
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = scan.imageLabel,
                fontFamily = SpaceGrotesk,
                fontSize = 14.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = scan.summary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dateString,
                    fontFamily = JetBrainsMono,
                    fontSize = 10.sp,
                    color = TextMuted
                )
                Text(
                    text = "ELA Score: ${scan.elaScore}%",
                    fontFamily = JetBrainsMono,
                    fontSize = 10.sp,
                    color = CyberCyan
                )
            }
        }
    }
}
