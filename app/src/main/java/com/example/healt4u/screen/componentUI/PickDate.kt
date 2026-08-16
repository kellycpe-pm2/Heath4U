package com.example.healt4u.screen.componentUI

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.example.healt4u.R
import com.example.healt4u.screen.componentUI.Theme.colorTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

@Composable
fun DatePickerPopupOnClick(
    modifier: Modifier = Modifier,label: String,
    onDateSelected: (String) -> Unit = {}
) {
    colorTheme({
        var selectedDate by remember { mutableStateOf<Long?>(null) }
        var showPopup by remember { mutableStateOf(false) }


        val popupState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)

        // Auto-close when date is selected
        LaunchedEffect(popupState.selectedDateMillis) {
            popupState.selectedDateMillis?.let {
                selectedDate = it
                onDateSelected(convertMillisToDate(it))
                showPopup = false
            }
        }

        OutlinedTextField(
            value = selectedDate?.let { convertMillisToDate(it) } ?: "",
            onValueChange = { /* Read-only */ },
            label =  { Text("$label") },
            placeholder = { Text("MM/DD/YYYY") },
            readOnly = true,
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.colors(
                // Container colors - From theme onSurface
                focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.onPrimary,

                // Indicator colors - From theme
                focusedIndicatorColor = MaterialTheme.colorScheme.onBackground,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.secondary,
                disabledIndicatorColor = MaterialTheme.colorScheme.secondary,

                // Text colors - From theme
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.secondary,

                // Label colors - From theme
                focusedLabelColor = MaterialTheme.colorScheme.onBackground,
                unfocusedLabelColor = MaterialTheme.colorScheme.secondary,

                // Placeholder colors - From theme
                focusedPlaceholderColor = MaterialTheme.colorScheme.secondary,
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.secondary,

                // Cursor color - From theme
                cursorColor = MaterialTheme.colorScheme.onBackground
            ),
            trailingIcon = {
                if (!showPopup) {
                    Icon(
                        painter = painterResource(R.drawable.calendar_unfocus),
                        contentDescription = "Select date"
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.calendar_focus),
                        contentDescription = "Select date"
                    )
                }

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
                    DatePicker(
                        state = popupState,
                        colors = DatePickerDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surface,

                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                            headlineContentColor = MaterialTheme.colorScheme.onBackground,
                            subheadContentColor = MaterialTheme.colorScheme.onBackground,

                            navigationContentColor = MaterialTheme.colorScheme.onBackground,

                            weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,

                            yearContentColor = MaterialTheme.colorScheme.onBackground,
                            disabledYearContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            currentYearContentColor = MaterialTheme.colorScheme.secondary,
                            selectedYearContentColor = MaterialTheme.colorScheme.onSecondary,
                            disabledSelectedYearContentColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.5f),
                            selectedYearContainerColor = MaterialTheme.colorScheme.secondary,
                            disabledSelectedYearContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),

                            dayContentColor = MaterialTheme.colorScheme.onBackground,
                            disabledDayContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.38f),
                            selectedDayContentColor = MaterialTheme.colorScheme.onSecondary,
                            disabledSelectedDayContentColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.5f),
                            selectedDayContainerColor = MaterialTheme.colorScheme.secondary,
                            disabledSelectedDayContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),

                            todayContentColor = MaterialTheme.colorScheme.secondary,
                            todayDateBorderColor = MaterialTheme.colorScheme.secondary,

                            dayInSelectionRangeContentColor = MaterialTheme.colorScheme.onSecondary,
                            dayInSelectionRangeContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),

                            dividerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)),
                        showModeToggle = false
                    )
                }
            }
        }
    }
    )

}

@Preview(showBackground = true, name = "Docked Popup")
@Composable
fun sds() {

    DatePickerPopupOnClick(
            modifier = Modifier.fillMaxSize(),
        "",
            onDateSelected = { println("Selected: $it") }
        )

}

