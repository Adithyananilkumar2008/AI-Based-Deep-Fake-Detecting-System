package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDark
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.SpaceGrotesk
import kotlin.math.roundToInt

@Composable
fun CompareSlider(
    originalBitmap: Bitmap,
    forensicBitmap: Bitmap,
    splitFraction: Float,
    isSplitActive: Boolean,
    onFractionChange: (Float) -> Unit,
    onTapPoint: (normalizedX: Float, normalizedY: Float) -> Unit,
    forensicLabel: String,
    modifier: Modifier = Modifier
) {
    var componentSize by remember { mutableStateOf(IntSize.Zero) }

    val origImageBitmap = remember(originalBitmap) { originalBitmap.asImageBitmap() }
    val forensicImageBitmap = remember(forensicBitmap) { forensicBitmap.asImageBitmap() }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CyberDark)
            .onSizeChanged { componentSize = it }
            .pointerInput(componentSize) {
                detectTapGestures { tapOffset ->
                    if (componentSize.width > 0 && componentSize.height > 0) {
                        val normX = (tapOffset.x / componentSize.width).coerceIn(0f, 1f)
                        val normY = (tapOffset.y / componentSize.height).coerceIn(0f, 1f)
                        onTapPoint(normX, normY)
                    }
                }
            }
            .then(
                if (isSplitActive) {
                    Modifier.pointerInput(componentSize) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            if (componentSize.width > 0) {
                                val newFraction = (change.position.x / componentSize.width).coerceIn(0f, 1f)
                                onFractionChange(newFraction)
                            }
                        }
                    }
                } else Modifier
            )
            .testTag("compare_slider_canvas")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // 1. Draw forensic image on full canvas
            drawImage(
                image = forensicImageBitmap,
                dstSize = IntSize(canvasWidth.toInt(), canvasHeight.toInt())
            )

            // 2. If split active, draw original image clipped to left partition
            if (isSplitActive) {
                val splitX = canvasWidth * splitFraction
                clipRect(left = 0f, top = 0f, right = splitX, bottom = canvasHeight) {
                    drawImage(
                        image = origImageBitmap,
                        dstSize = IntSize(canvasWidth.toInt(), canvasHeight.toInt())
                    )
                }

                // Vertical divider line
                drawLine(
                    color = CyberCyan,
                    start = Offset(splitX, 0f),
                    end = Offset(splitX, canvasHeight),
                    strokeWidth = 3.dp.toPx()
                )
            }
        }

        // Pill badges in corners
        if (isSplitActive) {
            Surface(
                color = Color.Black.copy(alpha = 0.75f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = "ORIGINAL",
                    color = Color.White,
                    fontFamily = SpaceGrotesk,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Surface(
                color = CyberCyan.copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Text(
                    text = forensicLabel.uppercase(),
                    color = CyberDark,
                    fontFamily = SpaceGrotesk,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Draggable center handle
            if (componentSize.width > 0) {
                val handleX = (componentSize.width * splitFraction) - 20.dp.value
                val handleY = (componentSize.height / 2f) - 20.dp.value

                Surface(
                    shape = CircleShape,
                    color = CyberCyan,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .offset { IntOffset(handleX.roundToInt(), handleY.roundToInt()) }
                        .size(40.dp)
                        .testTag("compare_slider_handle")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Drag to compare original and forensic map",
                            tint = CyberDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
