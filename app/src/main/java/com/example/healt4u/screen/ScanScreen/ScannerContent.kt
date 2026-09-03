package com.example.healt4u.screen.Scan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.healt4u.screen.ScanScreen.ScannerOverlay
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import java.util.regex.Pattern
import kotlin.coroutines.resume

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

    val cameraExecutor = remember {
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
                        durationMillis = 2000,
                        easing = LinearEasing
                    ),
                repeatMode =
                    RepeatMode.Reverse
            ),
        label = "scanLineOffset"
    )

    // ============================================================
    // GALLERY
    // ============================================================

    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            if (uri == null || isGalleryAnalyzing) {
                return@rememberLauncherForActivityResult
            }

            scope.launch {

                isGalleryAnalyzing = true
                isScanning = false
                isScanningAnimation = false

                try {

                    Log.d(
                        "Scanner",
                        "Gallery image selected = $uri"
                    )

                    val result =
                        scanImageForMedicine(
                            context = context,
                            uri = uri
                        )

                    if (!result.isNullOrBlank()) {

                        Log.d(
                            "Scanner",
                            "Gallery result = $result"
                        )

                        scanResult = result

                        // IMPORTANT:
                        // Only call this ONCE.
                        onScanResult(result)

                    } else {

                        Log.d(
                            "Scanner",
                            "No barcode or MAL found"
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

    val requestGallery = {

        if (!isGalleryAnalyzing) {

            onGalleryPick()

            galleryLauncher.launch(
                "image/*"
            )
        }
    }

    // ============================================================
    // CLEANUP
    // ============================================================

    DisposableEffect(Unit) {

        onDispose {

            cameraExecutor.shutdown()
        }
    }

    // ============================================================
    // FLASH
    // ============================================================

    val toggleFlash = {

        val nextState =
            !isFlashOn

        cameraInstance
            ?.cameraControl
            ?.enableTorch(
                nextState
            )

        isFlashOn = nextState

        onFlashToggle(
            nextState
        )
    }

    // ============================================================
    // MAIN UI
    // ============================================================

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black)
    ) {

        // ========================================================
        // CAMERA
        // ========================================================

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
                                        previewUseCase ->

                                    previewUseCase.setSurfaceProvider(
                                        previewView.surfaceProvider
                                    )
                                }

                        val imageAnalysis =
                            ImageAnalysis.Builder()
                                .setTargetResolution(
                                    Size(
                                        1280,
                                        720
                                    )
                                )
                                .setBackpressureStrategy(
                                    ImageAnalysis
                                        .STRATEGY_KEEP_ONLY_LATEST
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

                            val currentTime =
                                System.currentTimeMillis()

                            if (
                                currentTime -
                                lastScanTime < 1500
                            ) {

                                imageProxy.close()
                                return@setAnalyzer
                            }

                            isProcessing = true

                            processCameraImage(
                                imageProxy = imageProxy
                            ) { result ->

                                isProcessing = false

                                if (
                                    !result.isNullOrBlank()
                                ) {

                                    lastScanTime =
                                        System.currentTimeMillis()

                                    scanResult = result

                                    isScanning = false
                                    isScanningAnimation = false

                                    Log.d(
                                        "Scanner",
                                        "Camera result = $result"
                                    )

                                    onScanResult(result)
                                }
                            }
                        }

                        cameraProvider.unbindAll()

                        cameraInstance =
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalysis
                            )

                        isCameraReady = true

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

        // ========================================================
        // OVERLAY
        // ========================================================

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

        // ========================================================
        // TOP BAR
        // ========================================================

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

        // ========================================================
        // BOTTOM CONTROLS
        // ========================================================

        BottomControls(
            onManualInput = onManualInput,

            onGalleryPick = {
                requestGallery()
            }
        )

        // ========================================================
        // STATUS
        // ========================================================

        ScanningStatus(
            isScanning = isScanning,
            scanResult = scanResult,
            modifier =
                Modifier.align(
                    Alignment.BottomCenter
                )
        )

        // ========================================================
        // CAMERA LOADING
        // ========================================================

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
                        color = Color.White
                    )
                }
            }
        }

        // ========================================================
        // GALLERY LOADING
        // ========================================================

        if (isGalleryAnalyzing) {

            GalleryAnalyzingScreen()
        }
    }
}

// =================================================================
// GALLERY ANALYZING SCREEN
// =================================================================

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

                strokeWidth =
                    5.dp
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
                    "Scanning barcode and MAL number",

                color =
                    Color.White.copy(
                        alpha = 0.7f
                    ),

                textAlign =
                    TextAlign.Center
            )
        }
    }
}

// =================================================================
// MAL DETECTOR
// =================================================================

object MalNumberDetector {

    /*
     * Examples:
     *
     * MAL19912345X
     * MAL 19912345 X
     * MAL12035013X
     */
    private val MAL_PATTERN =
        Pattern.compile(
            "MAL\\s*[0-9]{6,14}\\s*[A-Za-z]{0,3}",
            Pattern.CASE_INSENSITIVE
        )

    fun extractMalNumber(
        text: String
    ): String? {

        if (text.isBlank()) {
            return null
        }

        val matcher =
            MAL_PATTERN.matcher(text)

        if (!matcher.find()) {
            return null
        }

        return matcher
            .group()
            .uppercase()
            .replace(
                Regex("\\s+"),
                ""
            )
    }

    fun containsMalNumber(
        text: String
    ): Boolean {

        return extractMalNumber(text) != null
    }
}

// =================================================================
// GALLERY IMAGE
// =================================================================

private suspend fun scanImageForMedicine(
    context: Context,
    uri: Uri
): String? {

    return withContext(Dispatchers.IO) {

        try {

            // -----------------------------------------------------
            // First read image size
            // -----------------------------------------------------

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
                    options = boundsOptions,
                    reqWidth = 1600,
                    reqHeight = 1600
                )

            // -----------------------------------------------------
            // Decode smaller image
            // -----------------------------------------------------

            val decodeOptions =
                BitmapFactory.Options().apply {

                    inSampleSize = sampleSize

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
                    "GalleryScanner",
                    "Unable to decode image"
                )

                return@withContext null
            }

            Log.d(
                "GalleryScanner",
                "Image = ${bitmap.width} x ${bitmap.height}"
            )

            try {

                scanBitmap(
                    bitmap
                )

            } finally {

                bitmap.recycle()
            }

        } catch (e: Exception) {

            Log.e(
                "GalleryScanner",
                "Image analysis failed",
                e
            )

            null
        }
    }
}

// =================================================================
// GALLERY BARCODE + OCR
// =================================================================

private suspend fun scanBitmap(
    bitmap: Bitmap
): String? {

    /*
     * 1. Barcode scanning
     */
    val barcodeResult =
        scanBarcodeFromBitmap(
            bitmap
        )

    if (!barcodeResult.isNullOrBlank()) {

        Log.d(
            "GalleryScanner",
            "Barcode found = $barcodeResult"
        )

        /*
         * If barcode itself contains MAL,
         * return MAL instead.
         */
        val malFromBarcode =
            MalNumberDetector.extractMalNumber(
                barcodeResult
            )

        if (!malFromBarcode.isNullOrBlank()) {

            return malFromBarcode
        }

        /*
         * Normal barcode result.
         */
        return barcodeResult
    }

    /*
     * 2. If barcode not found,
     * try OCR for MAL.
     */
    val malResult =
        scanMalWithOcr(
            bitmap
        )

    if (!malResult.isNullOrBlank()) {

        Log.d(
            "GalleryScanner",
            "OCR MAL found = $malResult"
        )

        return malResult
    }

    /*
     * Nothing found.
     */
    return null
}

// =================================================================
// GALLERY BARCODE
// =================================================================

private suspend fun scanBarcodeFromBitmap(
    bitmap: Bitmap
): String? {

    return suspendCancellableCoroutine { continuation ->

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

            scanner.process(inputImage)

                .addOnSuccessListener { barcodes ->

                    val result =
                        barcodes
                            .asSequence()
                            .mapNotNull {
                                it.rawValue?.trim()
                            }
                            .firstOrNull()

                    scanner.close()

                    if (
                        continuation.isActive
                    ) {

                        continuation.resume(
                            result
                        )
                    }
                }

                .addOnFailureListener { exception ->

                    Log.e(
                        "GalleryBarcode",
                        "Barcode scan failed",
                        exception
                    )

                    scanner.close()

                    if (
                        continuation.isActive
                    ) {

                        continuation.resume(
                            null
                        )
                    }
                }

        } catch (e: Exception) {

            Log.e(
                "GalleryBarcode",
                "Barcode exception",
                e
            )

            if (
                continuation.isActive
            ) {

                continuation.resume(
                    null
                )
            }
        }
    }
}

// =================================================================
// GALLERY OCR
// =================================================================

private suspend fun scanMalWithOcr(
    bitmap: Bitmap
): String? {

    return suspendCancellableCoroutine { continuation ->

        try {

            val inputImage =
                InputImage.fromBitmap(
                    bitmap,
                    0
                )

            val recognizer =
                TextRecognition.getClient(
                    TextRecognizerOptions.DEFAULT_OPTIONS
                )

            recognizer.process(inputImage)

                .addOnSuccessListener { result ->

                    val text =
                        result.text

                    Log.d(
                        "GalleryOCR",
                        "OCR TEXT = $text"
                    )

                    /*
                     * Normal text.
                     */
                    var mal =
                        MalNumberDetector.extractMalNumber(
                            text
                        )

                    /*
                     * OCR sometimes adds spaces:
                     *
                     * MAL 12035013 X
                     */
                    if (mal == null) {

                        val normalized =
                            text.replace(
                                Regex("\\s+"),
                                ""
                            )

                        mal =
                            MalNumberDetector.extractMalNumber(
                                normalized
                            )
                    }

                    recognizer.close()

                    if (
                        continuation.isActive
                    ) {

                        continuation.resume(
                            mal
                        )
                    }
                }

                .addOnFailureListener { exception ->

                    Log.e(
                        "GalleryOCR",
                        "OCR failed",
                        exception
                    )

                    recognizer.close()

                    if (
                        continuation.isActive
                    ) {

                        continuation.resume(
                            null
                        )
                    }
                }

        } catch (e: Exception) {

            Log.e(
                "GalleryOCR",
                "OCR exception",
                e
            )

            if (
                continuation.isActive
            ) {

                continuation.resume(
                    null
                )
            }
        }
    }
}

// =================================================================
// IMAGE SIZE
// =================================================================

private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {

    val height =
        options.outHeight

    val width =
        options.outWidth

    var sampleSize = 1

    if (
        height > reqHeight ||
        width > reqWidth
    ) {

        var halfHeight =
            height / 2

        var halfWidth =
            width / 2

        while (
            halfHeight / sampleSize >= reqHeight &&
            halfWidth / sampleSize >= reqWidth
        ) {

            sampleSize *= 2
        }
    }

    return sampleSize
}

// =================================================================
// CAMERA BARCODE + OCR
// =================================================================

@OptIn(ExperimentalGetImage::class)
private fun processCameraImage(
    imageProxy: ImageProxy,
    onResult: (String?) -> Unit
) {

    val mediaImage =
        imageProxy.image

    if (mediaImage == null) {

        imageProxy.close()

        onResult(null)

        return
    }

    val inputImage =
        InputImage.fromMediaImage(
            mediaImage,
            imageProxy
                .imageInfo
                .rotationDegrees
        )

    val barcodeScanner =
        BarcodeScanning.getClient()

    val textRecognizer =
        TextRecognition.getClient(
            TextRecognizerOptions.DEFAULT_OPTIONS
        )

    var barcodeFinished = false
    var ocrFinished = false

    var barcodeValue: String? = null
    var malValue: String? = null

    fun finish() {

        if (
            !barcodeFinished ||
            !ocrFinished
        ) {
            return
        }

        /*
         * MAL has priority.
         */
        val finalResult =
            malValue ?: barcodeValue

        barcodeScanner.close()
        textRecognizer.close()
        imageProxy.close()

        onResult(finalResult)
    }

    // -------------------------------------------------------------
    // BARCODE
    // -------------------------------------------------------------

    barcodeScanner
        .process(inputImage)

        .addOnSuccessListener { barcodes ->

            barcodeValue =
                barcodes
                    .asSequence()
                    .mapNotNull {
                        it.rawValue?.trim()
                    }
                    .firstOrNull()
        }

        .addOnFailureListener { error ->

            Log.e(
                "CameraBarcode",
                "Barcode error",
                error
            )
        }

        .addOnCompleteListener {

            barcodeFinished = true

            finish()
        }

    // -------------------------------------------------------------
    // OCR
    // -------------------------------------------------------------

    textRecognizer
        .process(inputImage)

        .addOnSuccessListener { result ->

            val text =
                result.text

            Log.d(
                "CameraOCR",
                "OCR TEXT = $text"
            )

            malValue =
                MalNumberDetector.extractMalNumber(
                    text
                )

            if (malValue == null) {

                malValue =
                    MalNumberDetector.extractMalNumber(
                        text.replace(
                            Regex("\\s+"),
                            ""
                        )
                    )
            }
        }

        .addOnFailureListener { error ->

            Log.e(
                "CameraOCR",
                "OCR error",
                error
            )
        }

        .addOnCompleteListener {

            ocrFinished = true

            finish()
        }
}

// =================================================================
// TOP BAR
// =================================================================

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
                imageVector =
                    Icons.Default.ArrowBack,

                contentDescription =
                    "Back",

                tint =
                    Color.White
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
                    imageVector =
                        if (isFlashOn) {
                            Icons.Default.FlashOn
                        } else {
                            Icons.Default.FlashOff
                        },

                    contentDescription =
                        "Flashlight",

                    tint =
                        Color.White
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
                    imageVector =
                        Icons.Default.PhotoLibrary,

                    contentDescription =
                        "Gallery",

                    tint =
                        Color.White
                )
            }
        }
    }
}

// =================================================================
// BOTTOM CONTROLS
// =================================================================

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
                    imageVector =
                        Icons.Default.Edit,

                    contentDescription =
                        null,

                    modifier =
                        Modifier.size(20.dp)
                )

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Text(
                    "Manual Entry"
                )
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
                    imageVector =
                        Icons.Default.Photo,

                    contentDescription =
                        null,

                    modifier =
                        Modifier.size(20.dp)
                )

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Text(
                    "Gallery"
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        Text(
            text =
                "Scan barcode or MAL number",

            color =
                Color.White.copy(
                    alpha = 0.5f
                ),

            fontSize =
                11.sp,

            textAlign =
                TextAlign.Center
        )
    }
}

// =================================================================
// SCANNING STATUS
// =================================================================

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
                    imageVector =
                        Icons.Default.CheckCircle,

                    contentDescription =
                        null
                )

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Text(
                    text =
                        "Scan Successful!",

                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}

// =================================================================
// PERMISSION RATIONALE
// =================================================================

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
            imageVector =
                Icons.Default.PhotoCamera,

            contentDescription =
                null,

            tint =
                Color.White,

            modifier =
                Modifier.size(64.dp)
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        Text(
            text =
                "Camera Permission Required",

            color =
                Color.White,

            fontSize =
                24.sp,

            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        Text(
            text =
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

            Text(
                "Grant Permission"
            )
        }
    }
}

// =================================================================
// PERMISSION DENIED
// =================================================================

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
            imageVector =
                Icons.Default.Warning,

            contentDescription =
                null,

            tint =
                Color(0xFFFF9800),

            modifier =
                Modifier.size(64.dp)
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        Text(
            text =
                "Camera Permission Denied",

            color =
                Color.White,

            fontSize =
                24.sp,

            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        Text(
            text =
                "Please grant camera permission and try again.",

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

            Text(
                "Request Again"
            )
        }
    }
}