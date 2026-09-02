package com.example.healt4u.screen.Scan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Vibrator
import android.util.Log
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
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
import com.example.healt4u.screen.ScanScreen.ScannerOverlay
import com.example.healt4u.screen.ScanScreen.TopBar
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import java.util.regex.Pattern

@OptIn(ExperimentalGetImage::class)
@Composable
fun ScannerContent(
    onScanResult: (String) -> Unit,
    onManualInput: () -> Unit,
    onFlashToggle: (Boolean) -> Unit,
    onGalleryPick: () -> Unit,
    onBackClick: () -> Unit
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    var isScanning by remember {
        mutableStateOf(true)
    }

    var isFlashOn by remember {
        mutableStateOf(false)
    }

    var isCameraReady by remember {
        mutableStateOf(false)
    }

    var isProcessing by remember {
        mutableStateOf(false)
    }

    var isGalleryAnalyzing by remember {
        mutableStateOf(false)
    }

    var scanResult by remember {
        mutableStateOf<String?>(null)
    }

    var isScanningAnimation by remember {
        mutableStateOf(true)
    }

    var cameraInstance by remember {
        mutableStateOf<Camera?>(null)
    }

    var lastScanTime by remember {
        mutableStateOf(0L)
    }

    val cameraExecutor =
        remember {
            Executors.newSingleThreadExecutor()
        }

    val infiniteTransition =
        rememberInfiniteTransition(
            label = "scanAnimation"
        )

    val scanLineOffset by
    infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        2000,
                        easing = LinearEasing
                    ),
                repeatMode =
                    RepeatMode.Reverse
            ),
        label = "scanLineOffset"
    )

    // ================================================================
    // GALLERY PICKER
    // ================================================================

    val galleryLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            if (uri == null) {
                return@rememberLauncherForActivityResult
            }

            if (isGalleryAnalyzing) {
                return@rememberLauncherForActivityResult
            }

            scope.launch {

                isGalleryAnalyzing = true
                isScanning = false
                isScanningAnimation = false

                try {

                    Log.d(
                        "Scanner",
                        "Gallery image selected: $uri"
                    )

                    val result =
                        scanImageForMedicine(
                            context = context,
                            uri = uri
                        )

                    if (result != null) {

                        Log.d(
                            "Scanner",
                            "Gallery result = $result"
                        )

                        scanResult = result

                        /*
                         * Only ONE callback.
                         */
                        onScanResult(result)

                    } else {

                        Log.d(
                            "Scanner",
                            "No MAL/barcode found in image"
                        )
                    }

                } catch (e: Exception) {

                    Log.e(
                        "Scanner",
                        "Gallery analysis failed",
                        e
                    )

                } finally {

                    isGalleryAnalyzing = false
                    isScanning = true
                    isScanningAnimation = true
                }
            }
        }

    // ================================================================
    // GALLERY BUTTON
    // ================================================================

    val requestGallery = {

        if (!isGalleryAnalyzing) {

            /*
             * GetContent opens the Android gallery/file picker.
             *
             * No READ_MEDIA_IMAGES permission is required here.
             */
            galleryLauncher.launch("image/*")

            onGalleryPick()
        }
    }

    // ================================================================
    // CAMERA CLEANUP
    // ================================================================

    DisposableEffect(Unit) {

        onDispose {

            cameraExecutor.shutdown()
        }
    }

    // ================================================================
    // FLASH
    // ================================================================

    val toggleFlash = {

        cameraInstance?.cameraControl?.let { control ->

            val nextState =
                !isFlashOn

            control.enableTorch(
                nextState
            )

            isFlashOn = nextState

            onFlashToggle(
                nextState
            )
        }
    }

    // ================================================================
    // MAIN UI
    // ================================================================

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black)
    ) {

        // ============================================================
        // CAMERA
        // ============================================================

        AndroidView(
            factory = { ctx ->

                PreviewView(ctx).apply {

                    scaleType =
                        PreviewView.ScaleType.FILL_CENTER

                    implementationMode =
                        PreviewView.ImplementationMode.COMPATIBLE
                }
            },

            modifier =
                Modifier.fillMaxSize(),

            update = { previewView ->

                /*
                 * Do not bind repeatedly while Compose updates.
                 */
                if (isCameraReady) {
                    return@AndroidView
                }

                val cameraProviderFuture =
                    ProcessCameraProvider.getInstance(
                        context
                    )

                cameraProviderFuture.addListener({

                    try {

                        val cameraProvider =
                            cameraProviderFuture.get()

                        val preview =
                            Preview.Builder()
                                .build()
                                .also {
                                    it.setSurfaceProvider(
                                        previewView.surfaceProvider
                                    )
                                }

                        val imageAnalysis =
                            ImageAnalysis.Builder()
                                .setTargetResolution(
                                    Size(1280, 720)
                                )
                                .setBackpressureStrategy(
                                    ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                                )
                                .build()

                        imageAnalysis.setAnalyzer(
                            cameraExecutor
                        ) { imageProxy ->

                            if (
                                !isScanning ||
                                isProcessing ||
                                isGalleryAnalyzing
                            ) {
                                imageProxy.close()
                                return@setAnalyzer
                            }

                            val currentTime = System.currentTimeMillis()

                            if (currentTime - lastScanTime < 1500) {
                                imageProxy.close()
                                return@setAnalyzer
                            }

                            isProcessing = true

                            processImageProxy(
                                imageProxy
                            ) { barcode, malNumber ->

                                isProcessing = false

                                // OCR found MAL number
                                if (!malNumber.isNullOrBlank()) {

                                    lastScanTime = System.currentTimeMillis()

                                    Log.d(
                                        "Scanner",
                                        "OCR MAL = $malNumber"
                                    )

                                    onScanResult(malNumber)

                                    return@processImageProxy
                                }

                                // Barcode found
                                if (barcode != null) {

                                    val rawData =
                                        barcode.rawValue?.trim()

                                    if (!rawData.isNullOrBlank()) {

                                        lastScanTime =
                                            System.currentTimeMillis()

                                        Log.d(
                                            "Scanner",
                                            "BARCODE = $rawData"
                                        )

                                        // Your barcode handling
                                        onScanResult(rawData)
                                    }
                                }
                            }
                        }

                        val cameraSelector =
                            CameraSelector
                                .DEFAULT_BACK_CAMERA

                        cameraProvider.unbindAll()

                        cameraInstance =
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )

                        isCameraReady = true

                        Log.d(
                            "Scanner",
                            "Camera ready"
                        )

                    } catch (e: Exception) {

                        Log.e(
                            "Scanner",
                            "Camera initialization failed",
                            e
                        )
                    }

                }, ContextCompat.getMainExecutor(context))
            }
        )

        // ============================================================
        // SCANNER OVERLAY
        // ============================================================

        if (isCameraReady) {

            ScannerOverlay(
                isScanning =
                    isScanningAnimation,
                scanLineOffset =
                    scanLineOffset,
                density =
                    density,
                onScanAreaChanged = {}
            )
        }

        // ============================================================
        // TOP BAR
        // ============================================================

        TopBar(
            isFlashOn = isFlashOn,

            onFlashToggle = {
                toggleFlash()
            },

            onGalleryPick = {
                requestGallery()
            },

            onBackClick = onBackClick
        )

        // ============================================================
        // BOTTOM CONTROLS
        // ============================================================

        BottomControls(
            onManualInput = onManualInput,

            onGalleryPick = {
                requestGallery()
            }
        )

        // ============================================================
        // SCAN STATUS
        // ============================================================

        ScanningStatus(
            isScanning = isScanning,
            scanResult = scanResult,
            modifier =
                Modifier.align(
                    Alignment.BottomCenter
                )
        )

        // ============================================================
        // CAMERA LOADING
        // ============================================================

        if (!isCameraReady) {

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Color.Black.copy(
                                alpha = 0.75f
                            )
                        ),
                contentAlignment =
                    Alignment.Center
            ) {

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    CircularProgressIndicator(
                        color = Color.White
                    )

                    Spacer(
                        modifier =
                            Modifier.height(16.dp)
                    )

                    Text(
                        text = "Starting camera...",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // ============================================================
        // GALLERY ANALYZING SCREEN
        // ============================================================

        if (isGalleryAnalyzing) {

            GalleryAnalyzingScreen()
        }
    }
}

// ====================================================================
// GALLERY ANALYZING SCREEN
// ====================================================================

@Composable
fun GalleryAnalyzingScreen() {

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(
                        alpha = 0.92f
                    )
                ),
        contentAlignment =
            Alignment.Center
    ) {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            CircularProgressIndicator(
                modifier =
                    Modifier.size(64.dp),
                color =
                    MaterialTheme
                        .colorScheme
                        .primary,
                strokeWidth = 5.dp
            )

            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )

            Text(
                text = "Analyzing image...",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text(
                text =
                    "Looking for MAL number or barcode",
                color =
                    Color.White.copy(
                        alpha = 0.7f
                    ),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ====================================================================
// MAL DETECTOR
// ====================================================================

object MalNumberDetector {

    private val MAL_PATTERN =
        Pattern.compile(
            "MAL\\s*[0-9]{6,12}\\s*[A-Za-z]{0,3}",
            Pattern.CASE_INSENSITIVE
        )

    private val MAL_EXTRACT =
        Pattern.compile(
            "MAL[0-9]{6,14}[A-Za-z]{0,3}",
            Pattern.CASE_INSENSITIVE
        )

    fun extractMalNumber(
        text: String
    ): String? {

        if (text.isBlank()) {
            return null
        }

        var matcher =
            MAL_PATTERN.matcher(text)

        if (matcher.find()) {

            return matcher
                .group()
                .uppercase()
                .replace(
                    Regex("\\s+"),
                    ""
                )
        }

        matcher =
            MAL_EXTRACT.matcher(text)

        if (matcher.find()) {

            return matcher
                .group()
                .uppercase()
        }

        return null
    }

    fun containsMalNumber(
        text: String
    ): Boolean {

        return extractMalNumber(text) != null
    }
}

// ====================================================================
// SCAN RESULT TYPE
// ====================================================================

sealed class ScanResultType {

    data class MalNumber(
        val number: String
    ) : ScanResultType()

    data class Barcode(
        val code: String
    ) : ScanResultType()

    data class Eleaflet(
        val url: String
    ) : ScanResultType()

    data class Unknown(
        val data: String
    ) : ScanResultType()
}

// ====================================================================
// DETECT SCAN TYPE
// ====================================================================

fun detectScanResultType(
    data: String
): ScanResultType {

    val cleanData =
        data.trim()

    return when {

        cleanData.contains(
            "quest3plus.npra.gov.my",
            ignoreCase = true
        ) ||
                cleanData.contains(
                    "npra.gov.my",
                    ignoreCase = true
                ) -> {

            ScanResultType.Eleaflet(
                cleanData
            )
        }

        MalNumberDetector
            .containsMalNumber(cleanData) -> {

            ScanResultType.MalNumber(
                MalNumberDetector
                    .extractMalNumber(
                        cleanData
                    )
                    ?: cleanData
            )
        }

        cleanData.all {
            it.isDigit()
        } &&
                cleanData.length in 8..13 -> {

            ScanResultType.Barcode(
                cleanData
            )
        }

        else -> {

            ScanResultType.Unknown(
                cleanData
            )
        }
    }
}

// ====================================================================
// TOP BAR
// ====================================================================

@Composable
fun TopBar(
    isFlashOn: Boolean,
    onFlashToggle: () -> Unit,
    onGalleryPick: () -> Unit,
    onBackClick: () -> Unit
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding(),

        horizontalArrangement =
            Arrangement.SpaceBetween,

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        IconButton(
            onClick = onBackClick
        ) {

            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Text(
            text = "Scan Medicine",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            IconButton(
                onClick = onFlashToggle,
                modifier =
                    Modifier
                        .size(44.dp)
                        .background(
                            Color.White.copy(
                                alpha = 0.2f
                            ),
                            CircleShape
                        )
            ) {

                Icon(
                    if (isFlashOn)
                        Icons.Default.FlashOn
                    else
                        Icons.Default.FlashOff,

                    contentDescription =
                        "Flashlight",

                    tint = Color.White
                )
            }

            IconButton(
                onClick = onGalleryPick,
                modifier =
                    Modifier
                        .size(44.dp)
                        .background(
                            Color.White.copy(
                                alpha = 0.2f
                            ),
                            CircleShape
                        )
            ) {

                Icon(
                    Icons.Default.PhotoLibrary,
                    contentDescription = "Gallery",
                    tint = Color.White
                )
            }
        }
    }
}

// ====================================================================
// BOTTOM CONTROLS
// ====================================================================

@Composable
fun BottomControls(
    onManualInput: () -> Unit,
    onGalleryPick: () -> Unit
) {

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(32.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Bottom
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Button(
                onClick = onManualInput,

                modifier =
                    Modifier.weight(1f),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color.White.copy(
                                alpha = 0.2f
                            ),
                        contentColor =
                            Color.White
                    ),

                shape =
                    RoundedCornerShape(12.dp)
            ) {

                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    modifier =
                        Modifier.size(20.dp)
                )

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Text("Manual Entry")
            }

            Button(
                onClick = onGalleryPick,

                modifier =
                    Modifier.weight(1f),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color.White.copy(
                                alpha = 0.2f
                            ),
                        contentColor =
                            Color.White
                    ),

                shape =
                    RoundedCornerShape(12.dp)
            ) {

                Icon(
                    Icons.Default.Photo,
                    contentDescription = null,
                    modifier =
                        Modifier.size(20.dp)
                )

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Text("Gallery")
            }
        }

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        Text(
            text =
                "Supports MAL numbers and barcodes",

            color =
                Color.White.copy(
                    alpha = 0.5f
                ),

            fontSize = 11.sp,

            textAlign =
                TextAlign.Center
        )
    }
}

// ====================================================================
// SCANNING STATUS
// ====================================================================

@Composable
fun ScanningStatus(
    isScanning: Boolean,
    scanResult: String?,
    modifier: Modifier = Modifier
) {

    AnimatedVisibility(
        visible =
            !isScanning &&
                    scanResult != null,

        enter =
            fadeIn() +
                    slideInVertically(),

        exit =
            fadeOut() +
                    slideOutVertically()
    ) {

        androidx.compose.material3.Card(

            modifier =
                modifier
                    .padding(
                        bottom = 120.dp
                    )
                    .fillMaxWidth(0.8f),

            colors =
                androidx.compose.material3
                    .CardDefaults
                    .cardColors(
                        containerColor =
                            Color(0xFF4CAF50)
                    ),

            shape =
                RoundedCornerShape(12.dp)
        ) {

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),

                horizontalArrangement =
                    Arrangement.Center,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White
                )

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Text(
                    "Scan Successful!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ====================================================================
// GALLERY IMAGE ANALYSIS
// ====================================================================

private suspend fun scanImageForMedicine(
    context: Context,
    uri: Uri
): String? {

    return withContext(Dispatchers.IO) {

        try {

            Log.d(
                "Scanner",
                "Loading gallery image..."
            )

            val boundsOptions =
                BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }

            context.contentResolver
                .openInputStream(uri)
                ?.use { stream ->

                    BitmapFactory.decodeStream(
                        stream,
                        null,
                        boundsOptions
                    )
                }

            val sampleSize =
                calculateInSampleSize(
                    boundsOptions,
                    1600,
                    1600
                )

            val decodeOptions =
                BitmapFactory.Options().apply {

                    inSampleSize =
                        sampleSize

                    inPreferredConfig =
                        Bitmap.Config.ARGB_8888
                }

            val bitmap =
                context.contentResolver
                    .openInputStream(uri)
                    ?.use { stream ->

                        BitmapFactory.decodeStream(
                            stream,
                            null,
                            decodeOptions
                        )
                    }

            if (bitmap == null) {

                Log.e(
                    "Scanner",
                    "Unable to decode image"
                )

                return@withContext null
            }

            Log.d(
                "Scanner",
                "Image = ${bitmap.width}x${bitmap.height}"
            )

            scanBitmap(bitmap)

        } catch (e: Exception) {

            Log.e(
                "Scanner",
                "Gallery image analysis failed",
                e
            )

            null
        }
    }
}

// ====================================================================
// BITMAP SCANNING
// ====================================================================

private suspend fun scanBitmap(
    bitmap: Bitmap
): String? {

    return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->

        try {

            val inputImage =
                InputImage.fromBitmap(
                    bitmap,
                    0
                )

            val options =
                BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                        Barcode.FORMAT_ALL_FORMATS
                    )
                    .build()

            val scanner =
                BarcodeScanning.getClient(
                    options
                )

            scanner.process(
                inputImage
            )
                .addOnSuccessListener { barcodes ->

                    var result: String? = null

                    for (barcode in barcodes) {

                        val raw =
                            barcode.rawValue
                                ?.trim()

                        if (raw.isNullOrBlank()) {
                            continue
                        }

                        val mal =
                            MalNumberDetector
                                .extractMalNumber(
                                    raw
                                )

                        result =
                            mal ?: raw

                        break
                    }

                    if (
                        result == null
                    ) {

                        Log.d(
                            "Scanner",
                            "Barcode scanner found nothing"
                        )
                    }

                    scanner.close()

                    if (
                        continuation.isActive
                    ) {

                        continuation.resume(
                            result,
                            onCancellation = null
                        )
                    }
                }

                .addOnFailureListener { error ->

                    Log.e(
                        "Scanner",
                        "ML Kit gallery scan failed",
                        error
                    )

                    scanner.close()

                    if (
                        continuation.isActive
                    ) {

                        continuation.resume(
                            null,
                            onCancellation = null
                        )
                    }
                }

        } catch (e: Exception) {

            Log.e(
                "Scanner",
                "Bitmap scanner error",
                e
            )

            if (
                continuation.isActive
            ) {

                continuation.resume(
                    null,
                    onCancellation = null
                )
            }
        }
    }
}

// ====================================================================
// IMAGE SIZE
// ====================================================================

private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {

    val height =
        options.outHeight

    val width =
        options.outWidth

    var inSampleSize = 1

    if (
        height > reqHeight ||
        width > reqWidth
    ) {

        val halfHeight =
            height / 2

        val halfWidth =
            width / 2

        while (
            halfHeight / inSampleSize >= reqHeight &&
            halfWidth / inSampleSize >= reqWidth
        ) {

            inSampleSize *= 2
        }
    }

    return inSampleSize
}

// ====================================================================
// CAMERA IMAGE PROCESSING
// ====================================================================

@ExperimentalGetImage
@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    imageProxy: ImageProxy,
    onResult: (
        Barcode?,
        String?
    ) -> Unit
) {

    val mediaImage = imageProxy.image

    if (mediaImage == null) {
        imageProxy.close()
        onResult(null, null)
        return
    }

    val image = InputImage.fromMediaImage(
        mediaImage,
        imageProxy.imageInfo.rotationDegrees
    )

    // BARCODE
    val barcodeScanner =
        BarcodeScanning.getClient()

    // OCR
    val textRecognizer =
        TextRecognition.getClient(
            TextRecognizerOptions.DEFAULT_OPTIONS
        )

    var barcodeResult: Barcode? = null
    var malResult: String? = null

    var barcodeFinished = false
    var ocrFinished = false

    fun finishIfDone() {

        if (barcodeFinished && ocrFinished) {

            onResult(
                barcodeResult,
                malResult
            )

            barcodeScanner.close()
            textRecognizer.close()
            imageProxy.close()
        }
    }

    // -------------------------
    // BARCODE SCANNING
    // -------------------------

    barcodeScanner.process(image)
        .addOnSuccessListener { barcodes ->

            barcodeResult =
                barcodes.firstOrNull()
        }
        .addOnFailureListener { exception ->

            Log.e(
                "Scanner",
                "Barcode error",
                exception
            )
        }
        .addOnCompleteListener {

            barcodeFinished = true

            finishIfDone()
        }

    // -------------------------
    // OCR
    // -------------------------

    textRecognizer.process(image)
        .addOnSuccessListener { result ->

            Log.d(
                "OCR",
                "TEXT = ${result.text}"
            )

            malResult =
                MalNumberDetector
                    .extractMalNumber(result.text)
        }
        .addOnFailureListener { exception ->

            Log.e(
                "OCR",
                "OCR error",
                exception
            )
        }
        .addOnCompleteListener {

            ocrFinished = true

            finishIfDone()
        }
}

// ====================================================================
// PERMISSION SCREENS
// ====================================================================

@Composable
fun PermissionRationaleScreen(
    onGrant: () -> Unit
) {

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(32.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        Icon(
            Icons.Default.PhotoCamera,
            contentDescription = null,
            tint = Color.White,
            modifier =
                Modifier.size(64.dp)
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        Text(
            "Camera Permission Required",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        Text(
            "Camera access is needed to scan medicine information.",
            color =
                Color.White.copy(
                    alpha = 0.7f
                ),
            textAlign =
                TextAlign.Center
        )

        Spacer(
            modifier =
                Modifier.height(32.dp)
        )

        Button(
            onClick = onGrant
        ) {

            Text("Grant Permission")
        }
    }
}

@Composable
fun PermissionDeniedScreen(
    onRequest: () -> Unit
) {

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(32.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            tint = Color(0xFFFF9800),
            modifier =
                Modifier.size(64.dp)
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        Text(
            "Camera Permission Denied",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        Text(
            "Please grant camera permission in system settings.",
            color =
                Color.White.copy(
                    alpha = 0.7f
                ),
            textAlign =
                TextAlign.Center
        )

        Spacer(
            modifier =
                Modifier.height(32.dp)
        )

        Button(
            onClick = onRequest
        ) {

            Text("Request Again")
        }
    }
}