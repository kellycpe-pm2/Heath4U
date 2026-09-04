package com.example.healt4u.screen.componentUI

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun AppLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(80.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF3779EE).copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp, 80.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Color(0xFF3779EE))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.White)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF3779EE))
                )
            }
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                
                // Top vertical line (blue)
                drawLine(
                    color = Color(0xFF3779EE),
                    start = center,
                    end = Offset(size.width / 2, size.height * 0.25f),
                    strokeWidth = 10f,
                    cap = StrokeCap.Round
                )
                
                // Diagonal line (white)
                drawLine(
                    color = Color.White,
                    start = center,
                    end = Offset(size.width * 0.75f, size.height * 0.7f),
                    strokeWidth = 10f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
