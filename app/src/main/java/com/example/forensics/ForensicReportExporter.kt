package com.example.forensics

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ForensicReportExporter {

    fun generateFormalReport(report: ForensicAnalysisReport): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
        val dateStr = dateFormat.format(Date(report.timestamp))

        return buildString {
            appendLine("=================================================================")
            appendLine("      DIGITAL FORENSIC EVIDENCE & INTEGRITY REPORT")
            appendLine("                 LABORATORY AUDIT RECORD")
            appendLine("=================================================================")
            appendLine("Report ID:        ${report.id}")
            appendLine("Examination Date: $dateStr")
            appendLine("Target Media:     ${report.mediaType.label.uppercase()}")
            appendLine("Input Origin:     ${if (report.presetId != null) "Calibrated Benchmark Sample (${report.presetId})" else "User Provided Media File"}")
            appendLine()
            appendLine("-----------------------------------------------------------------")
            appendLine("1. CRYPTOGRAPHIC DATA INTEGRITY (CHAIN OF CUSTODY)")
            appendLine("-----------------------------------------------------------------")
            appendLine("SHA-256 Digest:   ${report.sha256Hash.ifEmpty { "CALCULATED_ON_DEVICE_LOCAL" }}")
            appendLine("MD5 Digest:       ${report.md5Hash.ifEmpty { "CALCULATED_ON_DEVICE_LOCAL" }}")
            appendLine("Provenance Proof: C2PA Content Credentials ${if (report.c2paResult?.hasC2paManifest == true) "DETECTED (${report.c2paResult.signerDetails})" else "NOT PRESENT (Standard file)"}")
            appendLine()
            appendLine("-----------------------------------------------------------------")
            appendLine("2. EXECUTIVE FORENSIC DETERMINATION")
            appendLine("-----------------------------------------------------------------")
            appendLine("Verdict:          ${report.assessment.riskLevel.label.uppercase()}")
            appendLine("Suspicion Score:  ${report.assessment.syntheticRiskScore} / 100")
            appendLine("Analytical Summary:")
            appendLine("  ${report.assessment.summary}")
            appendLine()
            appendLine("-----------------------------------------------------------------")
            appendLine("3. MULTI-LAYER TECHNICAL SIGNALS BREAKDOWN")
            appendLine("-----------------------------------------------------------------")
            when (report.mediaType) {
                MediaType.IMAGE -> {
                    appendLine("• Error Level Analysis (ELA): ${report.assessment.elaScore}% divergence (Q=${report.qualityUsed}%, Scale=${report.scaleUsed}x)")
                    appendLine("• Sensor Noise Uniformity:    ${report.assessment.noiseScore}% (PRNU Laplacian filter)")
                    appendLine("• 2D Fourier (FFT) Spectrum:  ${report.assessment.spectralScore}% (High-frequency grid spikes: ${report.spectralResult?.highFreqSpikeDetected == true})")
                    appendLine("• Boundary Gradient Seams:    ${report.assessment.edgeScore}% (Sobel discontinuity index)")
                    appendLine("• EXIF & Parameter Metadata:  ${report.assessment.metadataScore}% (Camera: ${report.exifMetadata.cameraMake ?: "None"}, Software: ${report.exifMetadata.software ?: "None"})")
                }
                MediaType.VIDEO -> {
                    appendLine("• Temporal Motion Variance:   ${report.assessment.noiseScore}% delta between keyframes")
                    appendLine("• Background Morphing Score:  ${report.assessment.elaScore}% warp factor")
                    appendLine("• Facial Boundary Seam Jitter:${report.assessment.edgeScore}%")
                    appendLine("• Timeline Resolution:        ${report.videoResult?.width}x${report.videoResult?.height} @ ${report.videoResult?.frameRate} fps")
                }
                MediaType.AUDIO -> {
                    appendLine("• High-Frequency Cutoff:      ${String.format("%.1f", report.audioResult?.cutoffKhz ?: 0f)} kHz (AI Brickwall: ${((report.audioResult?.cutoffKhz ?: 20f) < 14f)})")
                    appendLine("• Digital Zero Silence Ratio: ${(report.audioResult?.deadSilenceRatio ?: 0f) * 100}%")
                    appendLine("• Vocoder Comb Artifacts:     ${if (report.audioResult?.vocoderArtifactDetected == true) "DETECTED" else "NOT OBSERVED"}")
                    appendLine("• Audio Noise Floor:          ${report.audioResult?.noiseFloorDb?.toInt()} dB")
                }
            }
            appendLine()
            appendLine("-----------------------------------------------------------------")
            appendLine("4. DETAILED FORENSIC FINDINGS")
            appendLine("-----------------------------------------------------------------")
            report.assessment.findings.forEachIndexed { i, f ->
                appendLine("[${i + 1}] ${f.category.uppercase()} - ${f.title}")
                appendLine("    Severity: ${f.severity.name}")
                appendLine("    Details:  ${f.detail}")
                appendLine()
            }
            appendLine("-----------------------------------------------------------------")
            appendLine("5. METHODOLOGY NOTICE & LIMITATIONS")
            appendLine("-----------------------------------------------------------------")
            appendLine("Heuristic digital forensics evaluates mathematical and physical anomalies")
            appendLine("in pixel quantization, temporal vectors, and acoustic spectrograms.")
            appendLine("Social media compression, repeated re-encoding, and artistic filters can")
            appendLine("induce false alarms. Results must be interpreted in legal/editorial context.")
            appendLine("=================================================================")
            appendLine("               [ END OF FORMAL FORENSIC AUDIT ]")
            appendLine("=================================================================")
        }
    }

    fun copyToClipboard(context: Context, report: ForensicAnalysisReport) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Forensic Evidence Report", generateFormalReport(report))
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Forensic audit report copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun shareReport(context: Context, report: ForensicAnalysisReport) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "Forensic Report - ${report.id.take(8)}")
            putExtra(Intent.EXTRA_TEXT, generateFormalReport(report))
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Export Formal Forensic Report"))
    }
}
