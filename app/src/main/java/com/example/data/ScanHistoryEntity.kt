package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val imageLabel: String,
    val syntheticRiskScore: Int,
    val riskLevel: String,
    val elaScore: Int,
    val summary: String,
    val isPreset: Boolean
)
