package com.example

import com.example.forensics.AudioForensicsEngine
import com.example.forensics.EdgeResult
import com.example.forensics.ElaResult
import com.example.forensics.ExifMetadata
import com.example.forensics.ForensicsEngine
import com.example.forensics.NoiseResult
import com.example.forensics.RiskLevel
import com.example.forensics.TurboColorMap
import android.graphics.Bitmap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testTurboColorMapBounds() {
        val minColor = TurboColorMap.getColor(0)
        val midColor = TurboColorMap.getColor(128)
        val maxColor = TurboColorMap.getColor(255)

        assertTrue(minColor != 0)
        assertTrue(midColor != 0)
        assertTrue(maxColor != 0)
    }

    @Test
    fun testAudioAiSampleGeneration() {
        val (samples, sampleRate) = AudioForensicsEngine.generateSyntheticAiSample()
        assertEquals(44100, sampleRate)
        assertTrue(samples.isNotEmpty())

        // Verify dead silence exists in AI voice sample
        var hasDeadSilence = false
        for (s in samples) {
            if (s == 0.0f) {
                hasDeadSilence = true
                break
            }
        }
        assertTrue(hasDeadSilence)
    }

    @Test
    fun testAuthenticAudioSampleGeneration() {
        val (samples, sampleRate) = AudioForensicsEngine.generateAuthenticSample()
        assertEquals(44100, sampleRate)
        assertTrue(samples.isNotEmpty())
    }
}
