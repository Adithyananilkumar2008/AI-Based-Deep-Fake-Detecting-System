package com.example.forensics

import android.graphics.Color
import kotlin.math.min

object TurboColorMap {
    private val colorLut = IntArray(256)

    init {
        // Pre-compute 256 gradient colors for high-performance forensic thermal map
        for (i in 0..255) {
            val t = i / 255.0f
            val r: Int
            val g: Int
            val b: Int

            when {
                t < 0.25f -> {
                    // Deep navy to electric cyan
                    val factor = t / 0.25f
                    r = (10 + (0 - 10) * factor).toInt()
                    g = (20 + (220 - 20) * factor).toInt()
                    b = (45 + (255 - 45) * factor).toInt()
                }
                t < 0.5f -> {
                    // Cyan to electric green
                    val factor = (t - 0.25f) / 0.25f
                    r = (0 + (40 - 0) * factor).toInt()
                    g = (220 + (255 - 220) * factor).toInt()
                    b = (255 + (70 - 255) * factor).toInt()
                }
                t < 0.75f -> {
                    // Green to bright warning amber
                    val factor = (t - 0.5f) / 0.25f
                    r = (40 + (255 - 40) * factor).toInt()
                    g = (255 + (180 - 255) * factor).toInt()
                    b = (70 + (0 - 70) * factor).toInt()
                }
                else -> {
                    // Amber to vivid neon crimson/white
                    val factor = (t - 0.75f) / 0.25f
                    r = 255
                    g = (180 + (40 - 180) * factor).toInt()
                    b = (0 + (100 - 0) * factor).toInt()
                }
            }
            colorLut[i] = Color.rgb(
                min(255, r.coerceAtLeast(0)),
                min(255, g.coerceAtLeast(0)),
                min(255, b.coerceAtLeast(0))
            )
        }
    }

    fun getColor(intensity: Int): Int {
        val clamped = intensity.coerceIn(0, 255)
        return colorLut[clamped]
    }
}
