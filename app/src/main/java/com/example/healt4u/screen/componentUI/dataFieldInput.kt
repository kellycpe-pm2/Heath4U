package com.example.healt4u.screen.componentUI

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.healt4u.screen.componentUI.Theme.colorTheme

@Composable
fun TextFieldInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    readOnly: Boolean,
    keyboardType : KeyboardType = KeyboardType.Unspecified,
    singleLine : Boolean,
    trailingIcon: @Composable (() -> Unit)? = null
)
{
    colorTheme(

        content = {
            OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
                readOnly = readOnly,
        label = { Text(text = label) },
        placeholder={Text(text = label)},
        singleLine = singleLine,
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
                trailingIcon = trailingIcon,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier
    )
        }

        )
}
