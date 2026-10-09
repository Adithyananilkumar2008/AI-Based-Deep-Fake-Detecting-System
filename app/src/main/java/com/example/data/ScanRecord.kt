package com.example.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp

data class ScanRecord(
    val id: String = "",
    val userId: String = "",
    val mediaType: String = "IMAGE",
    val imageLabel: String = "",
    val syntheticRiskScore: Long = 0L,
    val riskLevel: String = "LOW",
    val elaScore: Long = 0L,
    val summary: String = "",
    val isPreset: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to id,
            "userId" to userId,
            "mediaType" to mediaType,
            "imageLabel" to imageLabel,
            "syntheticRiskScore" to syntheticRiskScore,
            "riskLevel" to riskLevel,
            "elaScore" to elaScore,
            "summary" to summary,
            "isPreset" to isPreset,
            "createdAt" to (createdAt ?: FieldValue.serverTimestamp())
        )
        return map
    }

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): ScanRecord {
            return ScanRecord(
                id = doc.getString("id") ?: doc.id,
                userId = doc.getString("userId") ?: "",
                mediaType = doc.getString("mediaType") ?: "IMAGE",
                imageLabel = doc.getString("imageLabel") ?: "",
                syntheticRiskScore = doc.getLong("syntheticRiskScore") ?: 0L,
                riskLevel = doc.getString("riskLevel") ?: "LOW",
                elaScore = doc.getLong("elaScore") ?: 0L,
                summary = doc.getString("summary") ?: "",
                isPreset = doc.getBoolean("isPreset") ?: false,
                createdAt = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            )
        }
    }
}
