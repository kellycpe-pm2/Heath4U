package com.example.healt4u.screen.componentUI

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun InputtextField(value :String, onValueChange : (String) -> Unit, label : String, modifier: Modifier = Modifier){
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {Text(text =label)},
        singleLine = true,
        shape = RoundedCornerShape(20.dp),

        colors = TextFieldDefaults.colors(

            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,

            // Indicator colors (underline/border)
            focusedIndicatorColor = Color(0xFF011792),
            unfocusedIndicatorColor = Color(0xFF3674EE),
            disabledIndicatorColor = Color(0xFF3674EE),


            // Text colors
            focusedTextColor = Color(0xFF011792),
            unfocusedTextColor = Color(0xFF3674EE),

            // Placeholder colors
            focusedPlaceholderColor = Color(0xFF011792),
            unfocusedPlaceholderColor = Color(0xFF3674EE)
        ),

        modifier = modifier.fillMaxWidth()
    )

}