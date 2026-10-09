package com.example.forensics

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

data class SpectralResult(
    val spectralBitmap: Bitmap,
    val gridAnomalyScore: Float,
    val highFreqSpikeDetected: Boolean
)

object SpectralAnalysis {

    /**
     * Computes 2D Fourier magnitude spectrum and detects synthetic grid frequencies.
     */
    suspend fun compute2dFft(source: Bitmap): SpectralResult = withContext(Dispatchers.Default) {
        val size = 128 // 128x128 frequency domain for responsiveness and clarity
        val scaled = Bitmap.createScaledBitmap(source, size, size, true)

        val pixels = IntArray(size * size)
        scaled.getPixels(pixels, 0, size, 0, 0, size, size)

        val gray = FloatArray(size * size)
        for (i in 0 until (size * size)) {
            val p = pixels[i]
            gray[i] = (Color.red(p) * 0.299f + Color.green(p) * 0.587f + Color.blue(p) * 0.114f)
        }

        val magnitudes = FloatArray(size * size)
        var maxMag = 0f
        var highFreqEnergy = 0.0
        var midFreqEnergy = 0.0

        val half = size / 2

        // Compute 2D DFT with centered DC frequency
        for (u in 0 until size) {
            val shiftedU = u - half
            for (v in 0 until size) {
                val shiftedV = v - half

                var real = 0.0
                var imag = 0.0

                // Subsampled 2D Fourier kernel
                for (x in 0 until size step 2) {
                    val angleX = 2.0 * PI * shiftedU * x / size
                    for (y in 0 until size step 2) {
                        val angleY = 2.0 * PI * shiftedV * y / size
                        val totalAngle = angleX + angleY
                        val valPixel = gray[y * size + x]

                        real += valPixel * cos(totalAngle)
                        imag -= valPixel * sin(totalAngle)
                    }
                }

                val mag = sqrt(real * real + imag * imag).toFloat()
                magnitudes[u * size + v] = mag
                if (mag > maxMag) maxMag = mag

                val dist = sqrt((shiftedU * shiftedU + shiftedV * shiftedV).toDouble())
                if (dist > half * 0.6) {
                    highFreqEnergy += mag
                } else if (dist > half * 0.2) {
                    midFreqEnergy += mag
                }
            }
        }

        // Render centered 2D Spectrum Bitmap
        val specPixels = IntArray(size * size)
        for (i in 0 until (size * size)) {
            val m = magnitudes[i]
            val logScale = log10(1.0 + m) / log10(1.0 + maxMag)
            val intensity = (logScale * 255.0).toInt().coerceIn(0, 255)
            specPixels[i] = TurboColorMap.getColor(intensity)
        }

        val specBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        specBitmap.setPixels(specPixels, 0, size, 0, 0, size, size)

        val ratio = (highFreqEnergy / max(1.0, midFreqEnergy)).toFloat()
        val gridAnomalyScore = (ratio * 1.8f).coerceIn(0f, 1f)
        val highFreqSpikeDetected = gridAnomalyScore > 0.45f

        SpectralResult(
            spectralBitmap = specBitmap,
            gridAnomalyScore = gridAnomalyScore,
            highFreqSpikeDetected = highFreqSpikeDetected
        )
    }
}
