package com.example.healt4u.screen.componentUI

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.healt4u.screen.componentUI.Theme.colorTheme

@Composable
fun slider(){
    var sliderValue by remember { mutableFloatStateOf(0f) }
    colorTheme(
        {
            Slider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp, vertical = 5.dp),
                value = sliderValue,
                onValueChange = { sliderValue = it },
                valueRange = 0f..5f,
                steps = 4,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.onBackground,
                    activeTrackColor = MaterialTheme.colorScheme.onBackground,
                    activeTickColor = MaterialTheme.colorScheme.secondary,
                    inactiveTrackColor = MaterialTheme.colorScheme.secondary,
                    inactiveTickColor = MaterialTheme.colorScheme.onBackground,

                    disabledThumbColor = MaterialTheme.colorScheme.onBackground,
                    disabledActiveTrackColor = MaterialTheme.colorScheme.onBackground,
                    disabledActiveTickColor = MaterialTheme.colorScheme.onBackground,
                    disabledInactiveTrackColor = MaterialTheme.colorScheme.onBackground,
                    disabledInactiveTickColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    )

}
@Preview(showBackground =  true)
@Composable
fun s (){
    slider()
}