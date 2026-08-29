package com.example.healt4u

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.healt4u.nav.AppNavGraph
import com.example.healt4u.notification.NotificationHelper
import com.example.healt4u.screen.componentUI.Theme.colorTheme
import com.example.healt4u.ui.theme.Healt4UTheme

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* No extra handling needed — dose/stock alerts simply won't post if denied. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createChannels(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

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
@Preview(showBackground = true, name = "Medicine List Preview")
@Composable
fun PreviewMedicineListScreen() {
    AppNavGraph()
}