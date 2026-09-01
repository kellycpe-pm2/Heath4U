package com.example.healt4u.screen.ScanScreen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Vibrator
import android.util.Log
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.*
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
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
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    var isScanning by remember { mutableStateOf(true) }
    var isFlashOn by remember { mutableStateOf(false) }
    var isCameraReady by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var lastScanTime by remember { mutableStateOf(0L) }
    var showResultDialog by remember { mutableStateOf(false) }
    var scannedResult by remember { mutableStateOf<String?>(null) }
    var isEleaflet by remember { mutableStateOf(false) }

    var scanResult by remember { mutableStateOf<String?>(null) }
    var isScanningAnimation by remember { mutableStateOf(true) }

    var cameraInstance by remember { mutableStateOf<Camera?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }


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

private fun scanBarcodeFromImage(
    context: Context,
    imageUri: Uri,
    onResult: (String) -> Unit
) {
    try {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        bitmap?.let {
            val inputImage = InputImage.fromBitmap(it, 0)
            val scanner = BarcodeScanning.getClient()
            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let { value ->
                            if (value.isNotEmpty()) {
                                onResult(value)
                                return@addOnSuccessListener
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Scanner", "Image scan failed", e)
                }
        }
    } catch (e: Exception) {
        Log.e("Scanner", "Image processing failed", e)
    }
}

@ExperimentalGetImage
@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    imageProxy: ImageProxy,
    onBarcodeDetected: (Barcode) -> Unit
) {
    val mediaImage = imageProxy.image ?: return
    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    val scanner = BarcodeScanning.getClient()

    scanner.process(inputImage)
        .addOnSuccessListener { barcodes ->
            for (barcode in barcodes) {
                barcode.rawValue?.let {
                    if (it.isNotEmpty()) {
                        onBarcodeDetected(barcode)
                        break
                    }
                }
            }
        }
        .addOnFailureListener { e ->
            Log.e("Scanner", "Barcode scan failed", e)
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}
