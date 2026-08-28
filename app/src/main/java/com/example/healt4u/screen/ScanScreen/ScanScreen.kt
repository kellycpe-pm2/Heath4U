package com.example.healt4u.screen.ScanScreen

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@ExperimentalGetImage
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(
    onBarcodeScanned: (String) -> Unit,
    onManualInput: () -> Unit = {},
    onFlashToggle: (Boolean) -> Unit = {},
    onGalleryPick: () -> Unit = {},
    onBackClick: () -> Unit,
    context: Context
) {
    val cameraPermissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )

    val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val storagePermissionState = rememberPermissionState(permission = storagePermission)

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    when {
        cameraPermissionState.status.isGranted -> {
            ScannerContent(
                onBarcodeScanned = onBarcodeScanned,
                onManualInput = onManualInput,
                onFlashToggle = onFlashToggle,
                onGalleryPick = onGalleryPick,
                storagePermissionState = storagePermissionState,
                context = context,
                onBackClick = onBackClick
            )
        }
        cameraPermissionState.status.shouldShowRationale -> {
            PermissionRationaleScreen(onGrant = {
                cameraPermissionState.launchPermissionRequest()
            })
        }
        else -> {
            PermissionDeniedScreen(onRequest = {
                cameraPermissionState.launchPermissionRequest()
            })
        }
    }
}


