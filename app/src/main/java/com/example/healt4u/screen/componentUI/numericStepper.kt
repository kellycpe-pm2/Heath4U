package com.example.healt4u.screen.componentUI

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.overscroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.healt4u.R
import com.example.healt4u.screen.componentUI.Theme.colorTheme

@Composable
fun NumericStepper(
    minNum: Int ,
    maxNum: Int ,
    currentValue: Int = 0,
    step : Int =1,
    onValueChange: (Int) -> Unit = {}
) {
    var value by remember { mutableStateOf(currentValue.coerceIn(minNum, maxNum)) }

    if (value != currentValue && currentValue in minNum..maxNum) {
        value = currentValue
    }

    val isEnableDecrease = value > minNum
    val isEnableIncrease = value < maxNum
    colorTheme {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {


                IconButton(
                    onClick = {
                        if (value > minNum) {
                            value-=step
                            onValueChange(value)
                        }
                    },
                    enabled = isEnableDecrease
                    , modifier = Modifier.padding(1.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            if (isEnableDecrease) R.drawable.leftarrow_focus else R.drawable.leftarrow_unfocus
                        ),
                        contentDescription = "Decrease value",
                        tint = Color.Unspecified
                    )
                }

                TextField(
                    value = value.toString(),
                    onValueChange ={newValue -> value = newValue.toIntOrNull() ?: value},

                    textStyle = MaterialTheme.typography.labelLarge.copy(
                        color = when (value) {
                            minNum -> MaterialTheme.colorScheme.onError
                            maxNum -> MaterialTheme.colorScheme.onError
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    ),
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
                    modifier = Modifier.padding(  15.dp).size(width = 75.dp, height = 50.dp)
                )

                IconButton(
                    onClick = {
                        if (value < maxNum) {
                            value+=step
                            onValueChange(value)
                        }
                    },
                    enabled = isEnableIncrease
                ) {
                    Icon(
                        painter = painterResource(
                            if (isEnableIncrease) R.drawable.rightarrow_focus else R.drawable.rightarrow_unfocus
                        ),
                        contentDescription = "Increase value",
                        tint = Color.Unspecified
                    )
                }
                Spacer(Modifier.padding(1.dp))

            }
             /*Text(
                text = when (value) {
                    minNum -> " Minimum reached"
                    maxNum -> " Maximum reached"
                    else -> " "
                },
                style = MaterialTheme.typography.labelMedium,
                color = when (value) {
                    minNum -> MaterialTheme.colorScheme.error
                    maxNum -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )*/

        }
    }


@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    name = "Interactive Stepper"
)
@Composable
fun PreviewNumericStepper() {
    var quantity by remember { mutableStateOf(5) }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            NumericStepper(
                minNum = 1,
                maxNum = 10,
                currentValue = quantity,
                onValueChange = { newValue ->
                    quantity = newValue
                }
            )
        }
    }
