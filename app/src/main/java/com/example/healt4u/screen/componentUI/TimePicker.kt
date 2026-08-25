package com.example.healt4u.screen.componentUI

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.example.healt4u.R
import com.example.healt4u.screen.componentUI.Theme.colorTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerPopupOnClick(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onTimeChange: (String) -> Unit = {}
) {
    colorTheme({
        var showPopup by remember { mutableStateOf(false) }

        val parts = value.split(":")
        val initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val popupState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true
        )

        OutlinedTextField(
            value = value,
            onValueChange = { /* Read-only */ },
            label = { Text(label) },
            placeholder = { Text("HH:mm") },
            readOnly = true,
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.onPrimary,
                focusedIndicatorColor = MaterialTheme.colorScheme.onBackground,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.secondary,
                disabledIndicatorColor = MaterialTheme.colorScheme.secondary,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.secondary,
                focusedLabelColor = MaterialTheme.colorScheme.onBackground,
                unfocusedLabelColor = MaterialTheme.colorScheme.secondary,
                cursorColor = MaterialTheme.colorScheme.onBackground
            ),
            trailingIcon = {
                Icon(
                    painter = painterResource(
                        if (showPopup) R.drawable.calendar_focus else R.drawable.calendar_unfocus
                    ),
                    contentDescription = "Select time"
                )
            },
            modifier = modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(pass = PointerEventPass.Initial)
                        val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                        if (upEvent != null) {
                            showPopup = true
                        }
                    }
                }
        )

        if (showPopup) {
            Popup(
                onDismissRequest = { showPopup = false },
                alignment = Alignment.TopStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .shadow(elevation = 4.dp)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TimePicker(
                            state = popupState,
                            colors = TimePickerDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                clockDialColor = MaterialTheme.colorScheme.primary,
                                selectorColor = MaterialTheme.colorScheme.secondary,
                                periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.secondary,
                                timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.secondary,
                                timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                        TextButton(onClick = {
                            val formatted = "%02d:%02d".format(popupState.hour, popupState.minute)
                            onTimeChange(formatted)
                            showPopup = false
                        }) {
                            Text("OK", color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    })
}
