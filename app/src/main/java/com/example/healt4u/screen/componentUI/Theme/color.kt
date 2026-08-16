package com.example.healt4u.screen.componentUI.Theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.example.healt4u.R

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
        displayLarge = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = Color(0xFF011792)
        ),
        displayMedium = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = Color(0xFF011792)
        ),
        displaySmall = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color(0xFF011792)
        ),
        headlineLarge = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = Color(0xFF011792)
        ),
        headlineMedium = TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            color = Color(0xFF011792)
        ),
        headlineSmall = TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = Color(0xFF011792)
        ),
        titleLarge = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            color = Color(0xFF011792),
            textAlign = TextAlign.Center,
            letterSpacing = 1.5.sp,
            textDecoration = TextDecoration.Underline,
            fontFamily = FontFamily(Font(R.font.bungee))
        ),
        titleMedium = TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color(0xFF011792)
        ),
        titleSmall = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color(0xFF293D9D)
        ),
        bodyLarge = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = Color(0xFF011792)
        ),
        bodyMedium = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = Color(0xFF293D9D)
        ),
        bodySmall = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = Color(0xFF293D9D)
        ),
        labelLarge = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color(0xFFFFFFFF)
        ),
        labelMedium = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = Color(0xFF293D9D)
        ),
        labelSmall = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            color = Color(0xFF293D9D)
        )
    )

    MaterialTheme(
        colorScheme = customColors,
        typography = customType,
        content = content
    )
}
