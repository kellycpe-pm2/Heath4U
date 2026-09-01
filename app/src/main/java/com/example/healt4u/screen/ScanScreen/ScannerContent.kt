// 📁 screen/scan/ScannerScreen.kt

package com.example.healt4u.screen.scan  // ✅ 改为小写

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
import androidx.compose.ui.geometry.Rect
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
import androidx.lifecycle.Lifecycle
import com.example.healt4u.screen.ScanScreen.ScannerOverlay
import com.example.healt4u.screen.ScanScreen.TopBar
import com.google.accompanist.permissions.*
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

// ==================== 主 Composable ====================

@androidx.camera.core.ExperimentalGetImage
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
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
    var lastScanTime by remember { mutableStateOf(0L) }
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

    // ✅ 相册选择器
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            Log.d("Scanner", "选择的图片 URI: $it")
            // ✅ 使用 Application Context 并检查生命周期
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                scanBarcodeFromImage(context.applicationContext, it) { barcode ->
                    Log.d("Scanner", "扫描结果: $barcode")
                    onBarcodeScanned(barcode)
                }
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
        // ✅ 相机预览
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
                            .setTargetResolution(Size(1280, 720))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            if (isScanning) {
                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastScanTime > 2000) {
                                    processImageProxy(imageProxy = imageProxy) { barcode ->
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

                                        handleScannedData(context, barcode.rawValue ?: "", onBarcodeScanned)

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
            modifier = Modifier.align(Alignment.BottomCenter)
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


@Composable
fun BottomControls(
    onManualInput: () -> Unit,
    onGalleryPick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onManualInput,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("手动输入")
            }

            Button(
                onClick = onGalleryPick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Photo, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("相册")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "支持 MAL 号码 / 条形码 / Farmatag / E-labelling",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ScanningStatus(
    isScanning: Boolean,
    scanResult: String?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !isScanning && scanResult != null,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        Card(
            modifier = modifier
                .padding(bottom = 120.dp)
                .fillMaxWidth(0.8f),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4CAF50)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("扫描成功!", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==================== 扫描结果处理 ====================

fun handleScannedData(
    context: Context,
    scannedData: String,
    onBarcodeScanned: (String) -> Unit
) {
    when {
        // E-labelling 电子说明书
        scannedData.contains("quest3plus.npra.gov.my") ||
                scannedData.contains("npra.gov.my") -> {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(scannedData))
                context.startActivity(intent)
            } catch (e: Exception) {
                onBarcodeScanned(scannedData)
            }
        }
        else -> {
            onBarcodeScanned(scannedData)
        }
    }
}

// ==================== 图片扫描 ====================

private fun scanBarcodeFromImage(
    context: Context,
    imageUri: Uri,
    onResult: (String) -> Unit
) {
    // ✅ 使用协程在后台线程处理
    CoroutineScope(Dispatchers.IO).launch {
        try {
            Log.d("Scanner", "开始扫描图片: $imageUri")

            // ✅ 使用 ContentResolver 获取图片
            val inputStream = context.contentResolver.openInputStream(imageUri)

            // ✅ 使用更小的采样率避免 OOM
            val options = android.graphics.BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            android.graphics.BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // ✅ 计算合适的采样率
            val sampleSize = calculateInSampleSize(options, 800, 600)

            val decodeOptions = android.graphics.BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = android.graphics.Bitmap.Config.RGB_565
            }

            val newInputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = android.graphics.BitmapFactory.decodeStream(newInputStream, null, decodeOptions)
            newInputStream?.close()

            if (bitmap == null) {
                Log.e("Scanner", "无法解码图片")
                return@launch
            }

            Log.d("Scanner", "图片尺寸: ${bitmap.width}x${bitmap.height}")

            if (bitmap.width < 100 || bitmap.height < 100) {
                Log.e("Scanner", "图片太小，无法识别")
                return@launch
            }

            // ✅ 使用 withContext 切换到主线程处理结果
            withContext(Dispatchers.Main) {
                processBitmapForBarcode(bitmap, onResult)
            }

        } catch (e: Exception) {
            Log.e("Scanner", "图片处理失败: ${e.message}", e)
        }
    }
}

// ✅ 计算采样率
private fun calculateInSampleSize(options: android.graphics.BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2

        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

// ✅ 处理 Bitmap 扫描
private fun processBitmapForBarcode(
    bitmap: android.graphics.Bitmap,
    onResult: (String) -> Unit
) {
    try {
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE or
                        Barcode.FORMAT_EAN_13 or
                        Barcode.FORMAT_EAN_8 or
                        Barcode.FORMAT_UPC_A or
                        Barcode.FORMAT_CODE_128 or
                        Barcode.FORMAT_CODE_39
            )
            .build()

        val scanner = BarcodeScanning.getClient(options)

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    for (barcode in barcodes) {
                        barcode.rawValue?.let { value ->
                            if (value.isNotEmpty()) {
                                Log.d("Scanner", "✅ 扫描成功: $value")
                                onResult(value)
                                return@addOnSuccessListener
                            }
                        }
                    }
                } else {
                    Log.d("Scanner", "未检测到条形码")
                    tryDefaultScanner(bitmap, onResult)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Scanner", "扫描失败: ${e.message}")
                tryDefaultScanner(bitmap, onResult)
            }

    } catch (e: Exception) {
        Log.e("Scanner", "处理失败: ${e.message}", e)
    }
}

// ✅ 默认扫描器
private fun tryDefaultScanner(
    bitmap: android.graphics.Bitmap,
    onResult: (String) -> Unit
) {
    try {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val scanner = BarcodeScanning.getClient()

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { value ->
                        if (value.isNotEmpty()) {
                            Log.d("Scanner", "✅ 默认扫描器成功: $value")
                            onResult(value)
                            return@addOnSuccessListener
                        }
                    }
                }
                Log.d("Scanner", "默认扫描器未检测到条形码")
            }
            .addOnFailureListener { e ->
                Log.e("Scanner", "默认扫描器失败: ${e.message}")
            }
    } catch (e: Exception) {
        Log.e("Scanner", "默认扫描器异常: ${e.message}")
    }
}

// ==================== 相机图像处理 ====================

@ExperimentalGetImage
@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    imageProxy: ImageProxy,
    onBarcodeDetected: (Barcode) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }

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