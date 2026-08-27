package com.example.healt4u.screen.Scan

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ManualInputDialog(
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Manually Input MAL Number")
        },
        text = {
            Column {
                Text(
                    text = "Input MAL Number (EG. MAL123456789X）",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it.uppercase() },
                    label = { Text("MAL Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSearch(inputText)
                        onDismiss()
                    }
                },
                enabled = inputText.isNotBlank()
            ) {
                Text("Search", color = MaterialTheme.colorScheme.onBackground)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onBackground)
            }
        }
    )
}