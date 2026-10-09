package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.TextMuted
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCancellableCoroutine

@Composable
fun CameraCaptureDialog(
    onCapture: (Bitmap) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableStateOf(ImageCapture.FLASH_MODE_OFF) }
    var isCapturing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setFlashMode(flashMode)
            .build()
    }

    LaunchedEffect(flashMode) {
        imageCapture.flashMode = flashMode
    }

    Dialog(
        onDismissRequest = {
            if (!isCapturing) onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !isCapturing,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("camera_capture_dialog")
        ) {
            // Camera Preview View
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val mainExecutor: Executor = ContextCompat.getMainExecutor(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(lensFacing)
                                .build()

                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                        } catch (e: Exception) {
                            errorMessage = "Camera init failed: ${e.message}"
                        }
                    }, mainExecutor)

                    previewView
                },
                update = { previewView ->
                    val mainExecutor = ContextCompat.getMainExecutor(context)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(lensFacing)
                                .build()

                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                        } catch (e: Exception) {
                            errorMessage = "Switch camera failed: ${e.message}"
                        }
                    }, mainExecutor)
                }
            )

            // Top Camera Bar: Close, Mode Indicator, Flash Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .testTag("camera_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Camera",
                        tint = Color.White
                    )
                }

                Surface(
                    color = CyberDark.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(CyberCyan, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CAMERAX LIVE SENSOR",
                            fontFamily = JetBrainsMono,
                            fontSize = 11.sp,
                            color = CyberCyan
                        )
                    }
                }

                IconButton(
                    onClick = {
                        flashMode = if (flashMode == ImageCapture.FLASH_MODE_OFF) {
                            ImageCapture.FLASH_MODE_ON
                        } else {
                            ImageCapture.FLASH_MODE_OFF
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .testTag("camera_flash_toggle")
                ) {
                    Icon(
                        imageVector = if (flashMode == ImageCapture.FLASH_MODE_ON) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Toggle Flash",
                        tint = if (flashMode == ImageCapture.FLASH_MODE_ON) CyberCyan else Color.White
                    )
                }
            }

            // Forensic Crosshair reticle overlay in center
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(240.dp)
                    .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ALIGN SUBJECT IN FRAME",
                    fontFamily = SpaceGrotesk,
                    fontSize = 10.sp,
                    color = CyberCyan.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
            }

            // Error Message Banner if any
            if (errorMessage != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 100.dp, start = 20.dp, end = 20.dp),
                    color = Color.Red.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Bottom Shutter Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Photo will be sent directly to ELA & Frequency Engine",
                    fontFamily = SpaceGrotesk,
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Switch Camera (Front / Back)
                    IconButton(
                        onClick = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                            .testTag("switch_camera_button"),
                        enabled = !isCapturing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Switch Camera Front/Back",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Shutter Button
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(if (isCapturing) Color.Gray else CyberCyan)
                            .border(4.dp, Color.White, CircleShape)
                            .testTag("capture_shutter_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                if (!isCapturing) {
                                    isCapturing = true
                                    errorMessage = null
                                    val executor = ContextCompat.getMainExecutor(context)
                                    imageCapture.takePicture(
                                        executor,
                                        object : ImageCapture.OnImageCapturedCallback() {
                                            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                                try {
                                                    val bitmap = imageProxyToBitmap(imageProxy)
                                                    imageProxy.close()
                                                    isCapturing = false
                                                    onCapture(bitmap)
                                                } catch (e: Exception) {
                                                    imageProxy.close()
                                                    isCapturing = false
                                                    errorMessage = "Error processing capture: ${e.message}"
                                                }
                                            }

                                            override fun onError(exception: ImageCaptureException) {
                                                isCapturing = false
                                                errorMessage = "Capture failed: ${exception.message}"
                                            }
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                            enabled = !isCapturing
                        ) {
                            if (isCapturing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = CyberDark,
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Take Photo",
                                    tint = CyberDark,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }

                    // Spacer to balance layout
                    Spacer(modifier = Modifier.size(52.dp))
                }
            }
        }
    }
}

/**
 * Converts ImageProxy from CameraX to a correctly rotated Bitmap.
 */
private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
    val buffer = imageProxy.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        ?: throw IllegalStateException("Failed to decode ImageProxy bytes to Bitmap")

    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
    return if (rotationDegrees != 0) {
        val matrix = Matrix().apply {
            postRotate(rotationDegrees.toFloat())
        }
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } else {
        bitmap
    }
}
