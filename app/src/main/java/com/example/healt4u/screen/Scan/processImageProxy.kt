package com.example.healt4u.screen.Scan

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

@ExperimentalGetImage
@androidx.annotation.OptIn(ExperimentalGetImage::class)
fun processImageProxy(
    imageProxy: ImageProxy,
    onBarcodeDetected: (Barcode) -> Unit
) {
    val mediaImage = imageProxy.image ?: return

    val inputImage = InputImage.fromMediaImage(
        mediaImage,
        imageProxy.imageInfo.rotationDegrees
    )

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