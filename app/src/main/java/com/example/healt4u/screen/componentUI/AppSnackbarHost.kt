package com.example.healt4u.screen.componentUI

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// The app's custom color theme doesn't define inverseSurface/inverseOnSurface,
// so the default Material3 SnackbarHost falls back to low-contrast colors
// (blue-ish text on a dark background — hard to read). This forces an
// explicit, always-legible dark-navy/white style everywhere a snackbar is
// shown, instead of relying on the ambient theme.
private val SnackbarBackground = Color(0xFF1A1A2E)
private val SnackbarText = Color.White

@Composable
fun AppSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { data ->
        Snackbar(
            modifier = Modifier.padding(12.dp),
            shape = RoundedCornerShape(12.dp),
            containerColor = SnackbarBackground,
            contentColor = SnackbarText,
            actionContentColor = Color(0xFF7FB6FF),
            content = {
                Text(data.visuals.message, color = SnackbarText, fontWeight = FontWeight.Medium)
            }
        )
    }
}
