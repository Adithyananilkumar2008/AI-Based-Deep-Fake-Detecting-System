package com.example.forensics

import android.graphics.Bitmap

enum class MediaType(val label: String) {
    IMAGE("Image"),
    VIDEO("Video"),
    AUDIO("Audio")
}

enum class ForensicMode(val label: String, val description: String) {
    ORIGINAL("Original", "Raw uncompressed input image"),
    ELA("Error Level", "Differences after JPEG recompression; brighter areas indicate edited or spliced patches"),
    HEATMAP("Thermal Heatmap", "Colorized ELA intensity highlighting localized anomalies and synthetic regions"),
    SPECTRAL("Spectral FFT", "2D Fourier frequency domain; reveals synthetic diffusion grid spikes and transposed convolution artifacts"),
    NOISE("Noise Residual", "High-pass PRNU noise pattern; AI faces often exhibit unnatural smoothness"),
    EDGE("Edge Gradient", "Sobel gradient filter revealing composite seams and unnatural boundary transitions")
}

enum class RiskLevel(val label: String, val hexColor: Long) {
    LOW("Likely Authentic", 0xFF06D6A0),
    MODERATE("Inconclusive / Mixed", 0xFFFFB703),
    HIGH("Likely AI / Manipulated", 0xFFFF3366)
}

enum class FindingSeverity {
    INFO,
    WARNING,
    ALERT
}

data class ForensicFinding(
    val category: String,
    val title: String,
    val detail: String,
    val severity: FindingSeverity
)

data class ElaResult(
    val elaBitmap: Bitmap,
    val heatBitmap: Bitmap,
    val averageError: Float,
    val maxLocalError: Float,
    val variance: Float,
    val hotspotFraction: Float
)

data class NoiseResult(
    val noiseBitmap: Bitmap,
    val noiseVariance: Float,
    val uniformityIndex: Float,
    val isAbnormallySmooth: Boolean
)

data class EdgeResult(
    val edgeBitmap: Bitmap,
    val edgeVariance: Float,
    val boundaryAnomalyScore: Float
)

data class ExifMetadata(
    val cameraMake: String? = null,
    val cameraModel: String? = null,
    val software: String? = null,
    val exposureTime: String? = null,
    val fNumber: String? = null,
    val iso: String? = null,
    val dateTime: String? = null,
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val hasAiPromptTags: Boolean = false,
    val aiTagsFound: List<String> = emptyList(),
    val hasCameraExif: Boolean = false,
    val compressionType: String = "JPEG/Standard"
)

data class ForensicAssessment(
    val syntheticRiskScore: Int,
    val riskLevel: RiskLevel,
    val summary: String,
    val findings: List<ForensicFinding>,
    val elaScore: Int,
    val noiseScore: Int,
    val edgeScore: Int,
    val metadataScore: Int,
    val spectralScore: Int = 0
)

data class ForensicAnalysisReport(
    val id: String,
    val timestamp: Long,
    val mediaType: MediaType = MediaType.IMAGE,
    val imageUriString: String?,
    val presetId: String?,
    val originalBitmap: Bitmap,
    val elaResult: ElaResult,
    val noiseResult: NoiseResult,
    val edgeResult: EdgeResult,
    val exifMetadata: ExifMetadata,
    val assessment: ForensicAssessment,
    val qualityUsed: Int,
    val scaleUsed: Int,
    val sha256Hash: String = "",
    val md5Hash: String = "",
    val c2paResult: C2paInspectionResult? = null,
    val spectralResult: SpectralResult? = null,
    val videoResult: VideoForensicResult? = null,
    val audioResult: AudioForensicResult? = null
)

data class SamplePreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val drawableRes: Int,
    val typeDescription: String,
    val mediaType: MediaType = MediaType.IMAGE
)
