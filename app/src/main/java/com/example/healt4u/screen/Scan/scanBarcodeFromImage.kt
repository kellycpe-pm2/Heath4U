package com.example.healt4u.screen.scan

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

 fun scanBarcodeFromImage(
    context: Context,
    imageUri: Uri,
    onBarcodeDetected: (String) -> Unit
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
                                onBarcodeDetected(value)
                                break
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