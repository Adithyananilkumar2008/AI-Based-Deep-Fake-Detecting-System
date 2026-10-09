package com.example.forensics

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object ForensicsEngine {

    private const val MAX_PROCESS_DIMENSION = 960

    /**
     * Scale bitmap for memory-efficient and responsive on-device processing.
     */
    fun getNormalizedBitmap(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        if (width <= MAX_PROCESS_DIMENSION && height <= MAX_PROCESS_DIMENSION) {
            return source.copy(Bitmap.Config.ARGB_8888, true)
        }

        val scale = min(
            MAX_PROCESS_DIMENSION.toFloat() / width,
            MAX_PROCESS_DIMENSION.toFloat() / height
        )
        val targetWidth = (width * scale).toInt().coerceAtLeast(1)
        val targetHeight = (height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
    }

    /**
     * Executes Error Level Analysis (ELA) by recompressing at a target JPEG quality
     * and calculating pixel-level absolute divergence scaled by amplification factor.
     */
    suspend fun runErrorLevelAnalysis(
        sourceBitmap: Bitmap,
        quality: Int = 90,
        scaleFactor: Int = 20
    ): ElaResult = withContext(Dispatchers.Default) {
        val width = sourceBitmap.width
        val height = sourceBitmap.height

        // 1. Recompress in memory using specified JPEG quality
        val outputStream = ByteArrayOutputStream()
        sourceBitmap.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(50, 99), outputStream)
        val compressedBytes = outputStream.toByteArray()
        val recompressedBitmap = BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)
            ?: throw IllegalStateException("Failed to decode recompressed JPEG for ELA")

        val pixelCount = width * height
        val srcPixels = IntArray(pixelCount)
        val compPixels = IntArray(pixelCount)
        sourceBitmap.getPixels(srcPixels, 0, width, 0, 0, width, height)
        recompressedBitmap.getPixels(compPixels, 0, width, 0, 0, width, height)

        val elaPixels = IntArray(pixelCount)
        val heatPixels = IntArray(pixelCount)

        var totalError = 0.0
        val blockDim = 16
        val blocksX = width / blockDim
        val blocksY = height / blockDim
        val blockErrors = if (blocksX > 0 && blocksY > 0) FloatArray(blocksX * blocksY) else FloatArray(1)
        val blockPixelCounts = if (blocksX > 0 && blocksY > 0) IntArray(blocksX * blocksY) else IntArray(1)

        for (y in 0 until height) {
            val by = if (blocksY > 0) (y / blockDim).coerceAtMost(blocksY - 1) else 0
            for (x in 0 until width) {
                val index = y * width + x
                val bx = if (blocksX > 0) (x / blockDim).coerceAtMost(blocksX - 1) else 0
                val blockIndex = by * max(1, blocksX) + bx

                val p1 = srcPixels[index]
                val p2 = compPixels[index]

                val r1 = Color.red(p1)
                val g1 = Color.green(p1)
                val b1 = Color.blue(p1)

                val r2 = Color.red(p2)
                val g2 = Color.green(p2)
                val b2 = Color.blue(p2)

                val diffR = abs(r1 - r2)
                val diffG = abs(g1 - g2)
                val diffB = abs(b1 - b2)
                val rawAvg = (diffR + diffG + diffB) / 3.0f

                totalError += rawAvg
                blockErrors[blockIndex] += rawAvg
                blockPixelCounts[blockIndex] += 1

                // Amplified ELA channels
                val ampR = min(255, (diffR * scaleFactor))
                val ampG = min(255, (diffG * scaleFactor))
                val ampB = min(255, (diffB * scaleFactor))
                elaPixels[index] = Color.rgb(ampR, ampG, ampB)

                // Thermal Heatmap
                val ampAvg = min(255, (rawAvg * scaleFactor).toInt())
                heatPixels[index] = TurboColorMap.getColor(ampAvg)
            }
        }

        // Calculate block averages & variance
        var sumBlockAvg = 0.0
        var maxLocal = 0.0f
        val numBlocks = max(1, blockErrors.size)
        val averages = FloatArray(numBlocks)

        for (i in 0 until numBlocks) {
            val count = max(1, blockPixelCounts[i])
            val avg = blockErrors[i] / count
            averages[i] = avg
            sumBlockAvg += avg
            if (avg > maxLocal) maxLocal = avg
        }

        val meanBlock = (sumBlockAvg / numBlocks).toFloat()
        var varianceSum = 0.0
        for (i in 0 until numBlocks) {
            val diff = averages[i] - meanBlock
            varianceSum += diff * diff
        }
        val variance = (varianceSum / numBlocks).toFloat()
        val stdDev = sqrt(variance)

        // Threshold for anomalous hotspot blocks (> mean + 1.8 * stdDev)
        var hotspotBlocks = 0
        val threshold = meanBlock + 1.8f * stdDev
        for (i in 0 until numBlocks) {
            if (averages[i] > threshold && averages[i] > 3.0f) {
                hotspotBlocks++
            }
        }
        val hotspotFraction = hotspotBlocks.toFloat() / numBlocks

        val elaBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        elaBitmap.setPixels(elaPixels, 0, width, 0, 0, width, height)

        val heatBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        heatBitmap.setPixels(heatPixels, 0, width, 0, 0, width, height)

        val avgError = (totalError / pixelCount).toFloat()

        ElaResult(
            elaBitmap = elaBitmap,
            heatBitmap = heatBitmap,
            averageError = avgError,
            maxLocalError = maxLocal,
            variance = variance,
            hotspotFraction = hotspotFraction
        )
    }

    /**
     * Laplacian high-pass residual filter to analyze PRNU sensor noise.
     * Synthetic generation typically exhibits abnormally flat noise or periodic artifacts.
     */
    suspend fun runNoiseAnalysis(sourceBitmap: Bitmap): NoiseResult = withContext(Dispatchers.Default) {
        val width = sourceBitmap.width
        val height = sourceBitmap.height
        val pixelCount = width * height

        val srcPixels = IntArray(pixelCount)
        sourceBitmap.getPixels(srcPixels, 0, width, 0, 0, width, height)

        val luma = FloatArray(pixelCount)
        for (i in 0 until pixelCount) {
            val p = srcPixels[i]
            luma[i] = 0.299f * Color.red(p) + 0.587f * Color.green(p) + 0.114f * Color.blue(p)
        }

        val noisePixels = IntArray(pixelCount)
        var sumResidual = 0.0
        var sumSquared = 0.0
        var smoothPixels = 0

        // 3x3 Laplacian high-pass: 4 * center - top - bottom - left - right
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val idx = y * width + x
                val center = luma[idx]
                val top = luma[(y - 1) * width + x]
                val bottom = luma[(y + 1) * width + x]
                val left = luma[y * width + (x - 1)]
                val right = luma[y * width + (x + 1)]

                val residual = 4.0f * center - (top + bottom + left + right)
                val absRes = abs(residual)

                sumResidual += absRes
                sumSquared += absRes * absRes

                if (absRes < 1.2f) {
                    smoothPixels++
                }

                // Center visual noise on mid-gray (128) with amplification
                val amp = (128 + residual * 3.5f).toInt().coerceIn(0, 255)
                noisePixels[idx] = Color.rgb(amp, amp, amp)
            }
        }

        val interiorCount = max(1, (width - 2) * (height - 2))
        val meanResidual = (sumResidual / interiorCount).toFloat()
        val variance = ((sumSquared / interiorCount) - (meanResidual * meanResidual)).toFloat().coerceAtLeast(0f)
        val uniformityIndex = (smoothPixels.toFloat() / interiorCount)
        val isAbnormallySmooth = uniformityIndex > 0.45f && variance < 15.0f

        val noiseBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        noiseBitmap.setPixels(noisePixels, 0, width, 0, 0, width, height)

        NoiseResult(
            noiseBitmap = noiseBitmap,
            noiseVariance = variance,
            uniformityIndex = uniformityIndex,
            isAbnormallySmooth = isAbnormallySmooth
        )
    }

    /**
     * Sobel edge operator to detect seam line gradients and pasting boundaries.
     */
    suspend fun runEdgeGradientAnalysis(sourceBitmap: Bitmap): EdgeResult = withContext(Dispatchers.Default) {
        val width = sourceBitmap.width
        val height = sourceBitmap.height
        val pixelCount = width * height

        val srcPixels = IntArray(pixelCount)
        sourceBitmap.getPixels(srcPixels, 0, width, 0, 0, width, height)

        val gray = IntArray(pixelCount)
        for (i in 0 until pixelCount) {
            val p = srcPixels[i]
            gray[i] = (Color.red(p) * 299 + Color.green(p) * 587 + Color.blue(p) * 114) / 1000
        }

        val edgePixels = IntArray(pixelCount)
        var edgeSum = 0.0
        var maxMag = 0.0

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val idx = y * width + x

                // Sobel kernels
                val gx = (
                    -1 * gray[(y - 1) * width + (x - 1)] + 1 * gray[(y - 1) * width + (x + 1)] +
                    -2 * gray[y * width + (x - 1)]       + 2 * gray[y * width + (x + 1)] +
                    -1 * gray[(y + 1) * width + (x - 1)] + 1 * gray[(y + 1) * width + (x + 1)]
                )

                val gy = (
                    -1 * gray[(y - 1) * width + (x - 1)] - 2 * gray[(y - 1) * width + x] - 1 * gray[(y - 1) * width + (x + 1)] +
                     1 * gray[(y + 1) * width + (x - 1)] + 2 * gray[(y + 1) * width + x] + 1 * gray[(y + 1) * width + (x + 1)]
                )

                val mag = sqrt((gx * gx + gy * gy).toDouble())
                if (mag > maxMag) maxMag = mag
                edgeSum += mag

                val c = min(255, (mag / 3.0).toInt())
                // Cyan edge glow
                edgePixels[idx] = Color.rgb((c * 0.1).toInt(), (c * 0.9).toInt(), c)
            }
        }

        val edgeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        edgeBitmap.setPixels(edgePixels, 0, width, 0, 0, width, height)

        val interiorCount = max(1, (width - 2) * (height - 2))
        val avgEdge = (edgeSum / interiorCount).toFloat()
        val boundaryAnomalyScore = min(1.0f, (maxMag / 1200.0).toFloat())

        EdgeResult(
            edgeBitmap = edgeBitmap,
            edgeVariance = avgEdge,
            boundaryAnomalyScore = boundaryAnomalyScore
        )
    }

    /**
     * Reads EXIF metadata and inspects for AI generator signatures, prompt tags,
     * or camera hardware provenance.
     */
    suspend fun extractExifData(context: Context, uri: Uri?): ExifMetadata = withContext(Dispatchers.IO) {
        if (uri == null) {
            return@withContext ExifMetadata(
                software = "Sample Preset Image",
                hasCameraExif = false,
                aiTagsFound = emptyList()
            )
        }

        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) return@withContext ExifMetadata()

            val exif = ExifInterface(inputStream)
            val make = exif.getAttribute(ExifInterface.TAG_MAKE)
            val model = exif.getAttribute(ExifInterface.TAG_MODEL)
            val software = exif.getAttribute(ExifInterface.TAG_SOFTWARE)
            val exposure = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)
            val fNumber = exif.getAttribute(ExifInterface.TAG_F_NUMBER)
            val iso = exif.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS)
            val date = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL) ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
            val width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0)
            val height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0)
            val userComment = exif.getAttribute(ExifInterface.TAG_USER_COMMENT) ?: ""
            val imgDesc = exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION) ?: ""

            val combinedText = "$software $userComment $imgDesc".lowercase()
            val aiKeywords = listOf(
                "stable diffusion", "midjourney", "dall-e", "novelai", "automatic1111",
                "comfyui", "flux.1", "invokeai", "prompt:", "negative prompt:", "steps:",
                "sampler:", "cfg scale:", "seed:", "clip skip"
            )

            val detectedAiTags = aiKeywords.filter { combinedText.contains(it) }
            val hasCamera = !make.isNullOrBlank() || !model.isNullOrBlank() || !iso.isNullOrBlank()

            ExifMetadata(
                cameraMake = make,
                cameraModel = model,
                software = software,
                exposureTime = exposure,
                fNumber = fNumber,
                iso = iso,
                dateTime = date,
                imageWidth = width,
                imageHeight = height,
                hasAiPromptTags = detectedAiTags.isNotEmpty(),
                aiTagsFound = detectedAiTags,
                hasCameraExif = hasCamera
            )
        } catch (e: Exception) {
            ExifMetadata()
        } finally {
            try {
                inputStream?.close()
            } catch (_: Exception) {}
        }
    }

    /**
     * Synthesizes forensic heuristics into an overall Synthetic Risk Score (0-100).
     */
    fun calculateAssessment(
        elaResult: ElaResult,
        noiseResult: NoiseResult,
        edgeResult: EdgeResult,
        exif: ExifMetadata,
        spectralResult: SpectralResult? = null
    ): ForensicAssessment {
        val findings = mutableListOf<ForensicFinding>()

        // 1. ELA Scoring (0-100)
        // High hotspot fraction or extreme local variance indicates spliced elements or mixed compression.
        val elaHotspotFactor = (elaResult.hotspotFraction * 200).coerceIn(0f, 60f)
        val elaVarianceFactor = (elaResult.variance * 1.5f).coerceIn(0f, 40f)
        val elaScore = (elaHotspotFactor + elaVarianceFactor).toInt().coerceIn(0, 100)

        if (elaResult.hotspotFraction > 0.08f) {
            findings.add(
                ForensicFinding(
                    category = "Error Level Analysis",
                    title = "Localized Recompression Discrepancy",
                    detail = "Isolated patches exhibit significantly higher compression error than surrounding regions. Brighter areas indicate editing, inpainting, or spliced elements.",
                    severity = FindingSeverity.ALERT
                )
            )
        } else if (elaResult.variance < 2.0f && elaResult.averageError < 1.0f) {
            findings.add(
                ForensicFinding(
                    category = "Error Level Analysis",
                    title = "Uniform Single-Pass Compression",
                    detail = "Error distribution is uniform across the entire frame. Typical of authentic unedited photos or uniform single-generation AI exports.",
                    severity = FindingSeverity.INFO
                )
            )
        } else {
            findings.add(
                ForensicFinding(
                    category = "Error Level Analysis",
                    title = "Consistent Error Gradient",
                    detail = "Normal compression variations across texture and edge boundaries.",
                    severity = FindingSeverity.INFO
                )
            )
        }

        // 2. Noise & PRNU Residual Scoring (0-100)
        var noiseScore = 0
        if (noiseResult.isAbnormallySmooth) {
            noiseScore = 75
            findings.add(
                ForensicFinding(
                    category = "Sensor Noise Residual",
                    title = "Abnormal Smoothness / Missing Sensor Noise",
                    detail = "The image lacks expected physical camera sensor noise (PRNU). AI diffusion generation often renders unnaturally smooth skin and flat frequency bands.",
                    severity = FindingSeverity.WARNING
                )
            )
        } else if (noiseResult.noiseVariance > 45f) {
            noiseScore = 30
            findings.add(
                ForensicFinding(
                    category = "Sensor Noise Residual",
                    title = "Natural Sensor Noise Detected",
                    detail = "High-frequency residuals show organic photon noise patterns consistent with physical digital camera sensors.",
                    severity = FindingSeverity.INFO
                )
            )
        } else {
            noiseScore = 40
            findings.add(
                ForensicFinding(
                    category = "Sensor Noise Residual",
                    title = "Moderate High-Pass Noise",
                    detail = "Residual high-frequency energy within standard photographic bounds.",
                    severity = FindingSeverity.INFO
                )
            )
        }

        // 3. Edge Integrity Scoring (0-100)
        val edgeScore = (edgeResult.boundaryAnomalyScore * 70).toInt().coerceIn(0, 100)
        if (edgeResult.boundaryAnomalyScore > 0.7f) {
            findings.add(
                ForensicFinding(
                    category = "Edge Gradient",
                    title = "High Contrast Seam Boundaries",
                    detail = "Sharp gradient transitions detected along localized contours, potentially indicating composite blending or cut-and-paste boundaries.",
                    severity = FindingSeverity.WARNING
                )
            )
        }

        // 4. Metadata Scoring (0-100)
        var metadataScore = 0
        if (exif.hasAiPromptTags) {
            metadataScore = 100
            findings.add(
                ForensicFinding(
                    category = "EXIF Metadata",
                    title = "Direct AI Generation Parameters Found",
                    detail = "Image metadata contains known generative AI software tags or prompt parameters: ${exif.aiTagsFound.joinToString(", ")}.",
                    severity = FindingSeverity.ALERT
                )
            )
        } else if (!exif.hasCameraExif) {
            metadataScore = 45
            findings.add(
                ForensicFinding(
                    category = "EXIF Metadata",
                    title = "Stripped / Absent Camera Provenance",
                    detail = "No camera make, model, or exposure settings found. Common in AI outputs, screenshots, or social media re-encodings.",
                    severity = FindingSeverity.WARNING
                )
            )
        } else {
            metadataScore = 10
            findings.add(
                ForensicFinding(
                    category = "EXIF Metadata",
                    title = "Hardware Camera Provenance Present",
                    detail = "Detected camera hardware (${exif.cameraMake ?: ""} ${exif.cameraModel ?: ""}) with shutter/ISO parameters.",
                    severity = FindingSeverity.INFO
                )
            )
        }

        // 5. 2D Spectral Fourier Analysis
        var spectralScore = 15
        if (spectralResult != null) {
            if (spectralResult.highFreqSpikeDetected) {
                spectralScore = 85
                findings.add(
                    ForensicFinding(
                        category = "Fourier Spectrum",
                        title = "Synthetic High-Frequency Grid Spikes Detected",
                        detail = "2D FFT frequency magnitude exhibits unnatural periodic grid peaks and circular harmonics characteristic of transposed convolution layers in generative AI models.",
                        severity = FindingSeverity.ALERT
                    )
                )
            } else {
                spectralScore = 20
                findings.add(
                    ForensicFinding(
                        category = "Fourier Spectrum",
                        title = "Natural 1/f Power Decay",
                        detail = "2D frequency magnitude displays smooth organic radial power attenuation without artificial grid peaks.",
                        severity = FindingSeverity.INFO
                    )
                )
            }
        }

        // Weighted Overall Synthetic Risk Score
        val compositeScore = if (exif.hasAiPromptTags) {
            95
        } else {
            val weighted = if (spectralResult != null) {
                (elaScore * 0.35f) + (noiseScore * 0.25f) + (spectralScore * 0.20f) + (edgeScore * 0.10f) + (metadataScore * 0.10f)
            } else {
                (elaScore * 0.40f) + (noiseScore * 0.30f) + (edgeScore * 0.15f) + (metadataScore * 0.15f)
            }
            weighted.toInt().coerceIn(0, 100)
        }

        val riskLevel = when {
            compositeScore >= 65 -> RiskLevel.HIGH
            compositeScore >= 35 -> RiskLevel.MODERATE
            else -> RiskLevel.LOW
        }

        val summary = when (riskLevel) {
            RiskLevel.HIGH -> "High probability of AI generation or localized digital editing detected through multi-layer forensic convergence."
            RiskLevel.MODERATE -> "Inconclusive results. Moderate compression anomalies detected; possible filter, re-save, or subtle composite."
            RiskLevel.LOW -> "Consistent error levels, organic sensor noise, natural Fourier decay, and hardware metadata indicate authentic capture."
        }

        return ForensicAssessment(
            syntheticRiskScore = compositeScore,
            riskLevel = riskLevel,
            summary = summary,
            findings = findings,
            elaScore = elaScore,
            noiseScore = noiseScore,
            edgeScore = edgeScore,
            metadataScore = metadataScore,
            spectralScore = spectralScore
        )
    }
}
