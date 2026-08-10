package com.example.healt4u

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.healt4u.screen.componentUI.Theme.colorTheme
import com.example.healt4u.ui.theme.Healt4UTheme

class MainActivity : ComponentActivity() {
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

                        }
                    })
            }
        }
    }
}

