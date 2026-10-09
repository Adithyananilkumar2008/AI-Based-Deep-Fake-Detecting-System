package com.example.ui.components

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun ZoomableBox(
    modifier: Modifier = Modifier,
    minScale: Float = 1.0f,
    maxScale: Float = 6.0f,
    content: @Composable () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(minScale, maxScale)
                    scale = newScale

                    if (scale > 1f) {
                        val maxOffsetX = (size.width * (scale - 1)) / 2f
                        val maxOffsetY = (size.height * (scale - 1)) / 2f
                        offset = Offset(
                            x = (offset.x + pan.x).coerceIn(-maxOffsetX, maxOffsetX),
                            y = (offset.y + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                        )
                    } else {
                        offset = Offset.Zero
                    }
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        ) {
            content()
        }
    }
}
