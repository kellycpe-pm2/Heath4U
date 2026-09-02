package com.example.healt4u.screen.Scan

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class ScannerAnalyzer(
    private val onMalDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val recognizer =
        TextRecognition.getClient(
            TextRecognizerOptions.DEFAULT_OPTIONS
        )

    private var lastMal: String? = null
    private var isProcessing = false

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {

        if (isProcessing) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image

        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        isProcessing = true

        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        recognizer.process(image)
            .addOnSuccessListener { result ->

                val malNumber =
                    MalNumberDetector.extractMalNumber(result.text)

                if (malNumber != null && malNumber != lastMal) {
                    lastMal = malNumber
                    onMalDetected(malNumber)
                }
            }
            .addOnFailureListener {
                // OCR failed
            }
            .addOnCompleteListener {
                isProcessing = false
                imageProxy.close()
            }
    }
}