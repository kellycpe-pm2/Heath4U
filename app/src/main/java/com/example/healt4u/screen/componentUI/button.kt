package com.example.healt4u.screen.componentUI

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.healt4u.screen.componentUI.Theme.colorTheme

@Composable
fun button(text:String, onClick : ()-> Unit){
    colorTheme {
        Button(
            onClick = {onClick},
            colors = ButtonColors (
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContentColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = text, fontSize = 20.sp)
        }
    }

}

@Preview (showBackground = true)
@Composable
fun Previewbutton(){
    button("xx",{})
}