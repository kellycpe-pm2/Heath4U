package com.example.healt4u.screen.componentUI

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.healt4u.screen.componentUI.Theme.colorTheme

@Composable
fun InputTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    onClick : () ->Unit ={},
    id : Int =0,
    contentDescription : String ="",
    imageEnable : Boolean = false,
    modifier: Modifier = Modifier
) {
    colorTheme(

        content = {
            OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        placeholder={Text(text = label)},
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        colors = TextFieldDefaults.colors(
            // Container colors - From theme surface (White)
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,

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
            focusedPlaceholderColor = MaterialTheme.colorScheme.onBackground,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.secondary,

            // Cursor color - From theme
            cursorColor = MaterialTheme.colorScheme.onBackground
        ),
                trailingIcon = {

                    IconButton (onClick = {onClick}, modifier = Modifier.padding(5.dp))

                {
                    if(imageEnable){
                        Image(painter = painterResource(id), contentDescription = contentDescription, modifier = Modifier.padding(10.dp))

                    }
                }
                },
        modifier = modifier.fillMaxWidth()
    )
        }

        )
}
