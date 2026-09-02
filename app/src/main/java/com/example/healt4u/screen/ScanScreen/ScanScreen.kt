package com.example.healt4u.screen.ScanScreen

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.healt4u.screen.Scan.ScannerContent
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(
    onBarcodeScanned: (String) -> Unit,
    onManualInput: () -> Unit = {},
    onFlashToggle: (Boolean) -> Unit = {},
    onGalleryPick: () -> Unit = {}
) {

    val cameraPermissionState =
        rememberPermissionState(
            Manifest.permission.CAMERA
        )

    LaunchedEffect(Unit) {
        cameraPermissionState
            .launchPermissionRequest()
    }

    when {

        cameraPermissionState
            .status
            .isGranted -> {

            ScannerContent(

                onScanResult =
                    onBarcodeScanned,

                onManualInput =
                    onManualInput,

                onFlashToggle =
                    onFlashToggle,

                onGalleryPick =
                    onGalleryPick,

                onBackClick = {}
            )
        }

        cameraPermissionState
            .status
            .shouldShowRationale -> {

            PermissionRationaleScreen {
                cameraPermissionState
                    .launchPermissionRequest()
            }
        }

        else -> {

            PermissionDeniedScreen {
                cameraPermissionState
                    .launchPermissionRequest()
            }
        }
    }
}