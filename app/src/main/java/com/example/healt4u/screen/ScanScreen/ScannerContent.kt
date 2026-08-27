package com.example.healt4u.screen.ScanScreen

import android.content.Context
import android.net.Uri
import android.os.Vibrator
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalGetImage::class, ExperimentalPermissionsApi::class)
@Composable
fun ScannerContent(
    onBarcodeScanned: (String) -> Unit,
    onManualInput: () -> Unit,
    onFlashToggle: (Boolean) -> Unit,
    onGalleryPick: () -> Unit,
    storagePermissionState: PermissionState,
    context: Context,
    onBackClick: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    var isScanning by remember { mutableStateOf(true) }
    var isFlashOn by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<String?>(null) }
    var isScanningAnimation by remember { mutableStateOf(true) }
    var isCameraReady by remember { mutableStateOf(false) }

    var cameraInstance by remember { mutableStateOf<Camera?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var lastScanTime by remember { mutableStateOf(0L) }

    val infiniteTransition = rememberInfiniteTransition(label = "scanAnimation")
    val scanLineOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLineOffset"
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            scanBarcodeFromImage(context, it) { barcode ->
                onBarcodeScanned(barcode)
            }
        }
    }

    val requestGalleryAccess = {
        if (storagePermissionState.status.isGranted) {
            galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
            onGalleryPick()
        } else {
            storagePermissionState.launchPermissionRequest()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    val toggleFlash = {
        cameraInstance?.cameraControl?.let { control ->
            val nextState = !isFlashOn
            control.enableTorch(nextState)
            isFlashOn = nextState
            onFlashToggle(nextState)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setTargetResolution(android.util.Size(1280, 720))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            if (isScanning) {
                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastScanTime > 2000) {
                                    processImageProxy(imageProxy) { barcode ->
                                        lastScanTime = currentTime
                                        isScanning = false
                                        scanResult = barcode.rawValue
                                        isScanningAnimation = false

                                        try {
                                            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                                            if (vibrator.hasVibrator()) {
                                                vibrator.vibrate(100)
                                            }
                                        } catch (e: Exception) {
                                            Log.e("Scanner", "Vibration failed", e)
                                        }

                                        onBarcodeScanned(barcode.rawValue ?: "")

                                        scope.launch {
                                            delay(2000)
                                            isScanning = true
                                            isScanningAnimation = true
                                        }
                                    }
                                } else {
                                    imageProxy.close()
                                }
                            } else {
                                imageProxy.close()
                            }
                        }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraInstance = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                            isCameraReady = true
                        } catch (e: Exception) {
                            Log.e("Scanner", "Camera binding failed", e)
                        }
                    } catch (e: Exception) {
                        Log.e("Scanner", "Camera provider failed", e)
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        )

        if (isCameraReady) {
            ScannerOverlay(
                isScanning = isScanningAnimation,
                scanLineOffset = scanLineOffset,
                density = density,
                onScanAreaChanged = { rect -> }
            )
        }

        TopBar(
            isFlashOn = isFlashOn,
            onFlashToggle = { toggleFlash() },
            onGalleryPick = { requestGalleryAccess() },
            onBackClick = onBackClick
        )

        BottomControls(
            onManualInput = onManualInput,
            onGalleryPick = { requestGalleryAccess() }
        )

        ScanningStatus(
            isScanning = isScanning,
            scanResult = scanResult,
        )

        if (!isCameraReady) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Text(
                        text = "Opening Camera ......",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}