package com.example.healt4u

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.healt4u.nav.AppNavGraph
import com.example.healt4u.screen.componentUI.Theme.colorTheme
import com.example.healt4u.ui.theme.Healt4UTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalGetImage::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Healt4UTheme {
                colorTheme(
                    {
                        Surface(
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface).fillMaxSize()
                        ) {
                            AppNavGraph()
                        }
                    })
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalGetImage::class)
@Preview(showBackground = true, name = "Medicine List Preview")
@Composable
fun PreviewMedicineListScreen() {
    AppNavGraph()
}