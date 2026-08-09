package com.example.healt4u.screen.componentUI.Theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun colorTheme(content: @Composable () -> Unit) {
    val customColors = lightColorScheme(
        primary = Color(0xFFE9FCFF),
        onPrimary = Color(0xFFFFFFFF),
        secondary = Color(0xFF3674EE),
        onSecondary = Color(0xFFFFFFFF),
        background = Color(0xFFE9FCFF),
        onBackground = Color(0xFF011792),
        surface = Color(0xFFE9FCFF),
        onSurface = Color(0xFFFFFFFF),
        onSurfaceVariant =Color(0xFF293D9D),
        error = Color(0xFFD32F2F),
        onError = Color(0xFFF44336)
    )

    val customType = Typography(
        headlineMedium = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp
        ),
        bodyLarge = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
        )
    )

    MaterialTheme(
        colorScheme = customColors,
        typography = customType,
        content = content
    )
}
