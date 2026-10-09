package com.example.forensics

import java.io.InputStream

data class C2paInspectionResult(
    val hasC2paManifest: Boolean,
    val isSignedByCamera: Boolean,
    val isSignedAsAiGenerated: Boolean,
    val signerDetails: String?
)

object C2paInspector {

    /**
     * Inspects byte stream for C2PA / JUMBF boxes and Content Authenticity markers.
     */
    fun inspectStream(inputStream: InputStream): C2paInspectionResult {
        return try {
            val buffer = ByteArray(65536)
            val bytesRead = inputStream.read(buffer)
            if (bytesRead <= 0) return C2paInspectionResult(false, false, false, null)

            val text = String(buffer, 0, bytesRead, Charsets.ISO_8859_1)

            val hasC2pa = text.contains("c2pa") || text.contains("jumb") || text.contains("contentauthenticity")
            val hasAiClaim = text.contains("generator") || text.contains("trainedAlgorithmicMedia") || text.contains("ai_generated")
            val hasCameraClaim = text.contains("c2pa.created") || text.contains("c2pa.camera") || text.contains("HardwareKey")

            val signer = when {
                text.contains("Adobe Photoshop") -> "Adobe Content Credentials"
                text.contains("Leica Camera") -> "Leica Hardware Signature"
                text.contains("Sony Electronics") -> "Sony In-Camera Signing"
                text.contains("OpenAI") || text.contains("DALL-E") -> "OpenAI Content Authenticity Manifest"
                hasC2pa -> "Standard C2PA Manifest Store"
                else -> null
            }

            C2paInspectionResult(
                hasC2paManifest = hasC2pa,
                isSignedByCamera = hasCameraClaim,
                isSignedAsAiGenerated = hasAiClaim,
                signerDetails = signer
            )
        } catch (e: Exception) {
            C2paInspectionResult(false, false, false, null)
        }
    }
}
