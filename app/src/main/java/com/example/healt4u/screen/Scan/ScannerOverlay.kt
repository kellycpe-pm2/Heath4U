package com.example.healt4u.screen.Scan

import android.graphics.Rect
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScannerOverlay(
    isScanning: Boolean,
    scanLineOffset: Float,
    density: Density,
    onScanAreaChanged: (Rect) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            // CRITICAL FIX: Forces an offscreen layer so BlendMode.Clear doesn't punch down to the black background
            .graphicsLayer { alpha = 0.99f }
            .drawWithContent {
                drawContent()

                val width = size.width
                val height = size.height
                val scanBoxSize = width * 0.7f
                val left = (width - scanBoxSize) / 2
                val top = (height - scanBoxSize) / 2
                val right = left + scanBoxSize
                val bottom = top + scanBoxSize

                // 1. Draw the semi-transparent black background
                drawRect(
                    color = Color.Black.copy(alpha = 0.5f),
                    topLeft = Offset(0f, 0f),
                    size = Size(width, height)
                )

                // 2. Punch out the clear frame (reveals camera preview)
                drawRect(
                    color = Color.Transparent,
                    topLeft = Offset(left, top),
                    size = Size(scanBoxSize, scanBoxSize),
                    blendMode = BlendMode.Clear
                )

                // 3. Draw Green Frame Corners
                val cornerLength = scanBoxSize * 0.15f
                val strokeWidth = 4.dp.toPx()
                val color = Color(0xFF4CAF50)

                // Top Left Corner
                drawLine(
                    color = color,
                    start = Offset(left, top + cornerLength),
                    end = Offset(left, top),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = color,
                    start = Offset(left, top),
                    end = Offset(left + cornerLength, top),
                    strokeWidth = strokeWidth
                )

                // Top Right Corner
                drawLine(
                    color = color,
                    start = Offset(right, top + cornerLength),
                    end = Offset(right, top),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = color,
                    start = Offset(right, top),
                    end = Offset(right - cornerLength, top),
                    strokeWidth = strokeWidth
                )

                // Bottom Left Corner
                drawLine(
                    color = color,
                    start = Offset(left, bottom - cornerLength),
                    end = Offset(left, bottom),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = color,
                    start = Offset(left, bottom),
                    end = Offset(left + cornerLength, bottom),
                    strokeWidth = strokeWidth
                )

                // Bottom Right Corner
                drawLine(
                    color = color,
                    start = Offset(right, bottom - cornerLength),
                    end = Offset(right, bottom),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = color,
                    start = Offset(right, bottom),
                    end = Offset(right - cornerLength, bottom),
                    strokeWidth = strokeWidth
                )

                // 4. Scanning Line Animation
                if (isScanning) {
                    val lineY = top + (bottom - top) * scanLineOffset
                    drawLine(
                        color = Color(0xFF4CAF50).copy(alpha = 0.7f),
                        start = Offset(left + 20.dp.toPx(), lineY),
                        end = Offset(right - 20.dp.toPx(), lineY),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
            .onGloballyPositioned { coordinates ->
                val width = coordinates.size.width
                val height = coordinates.size.height
                val scanBoxSize = (width * 0.7f).toInt()
                val left = ((width - scanBoxSize) / 2)
                val top = ((height - scanBoxSize) / 2)

                val rect = Rect(
                    left,
                    top,
                    left + scanBoxSize,
                    top + scanBoxSize
                )
                onScanAreaChanged(rect)
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 50.dp, vertical = 100.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📷",
                    fontSize = 32.sp,
                    modifier = Modifier.alpha(0.7f)
                )
                Text(
                    text = "Please Put The Product Barcode In The Frame",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Text(
                    text = "Scan Automatically",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
        }
    }
}