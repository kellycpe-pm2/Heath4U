package com.example.healt4u.screen.componentUI

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.healt4u.R
import com.example.healt4u.screen.componentUI.Theme.colorTheme

@Composable
fun NumericStepper(
    minNum: Int ,
    maxNum: Int ,
    currentValue: Int = 0,
    onValueChange: (Int) -> Unit = {}
) {
    var value by remember { mutableStateOf(currentValue.coerceIn(minNum, maxNum)) }

    if (value != currentValue && currentValue in minNum..maxNum) {
        value = currentValue
    }

    val isEnableDecrease = value > minNum
    val isEnableIncrease = value < maxNum
    colorTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {


                IconButton(
                    onClick = {
                        if (value > minNum) {
                            value--
                            onValueChange(value)
                        }
                    },
                    enabled = isEnableDecrease
                ) {
                    Icon(
                        painter = painterResource(
                            if (isEnableDecrease) R.drawable.leftarrow_focus else R.drawable.leftarrow_unfocus
                        ),
                        contentDescription = "Decrease value",
                        tint = Color.Unspecified
                    )
                }

                Text(
                    text = value.toString(),
                    color = when (value) {
                        minNum -> MaterialTheme.colorScheme.onError
                        maxNum -> MaterialTheme.colorScheme.onError
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },

                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                IconButton(
                    onClick = {
                        if (value < maxNum) {
                            value++
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
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = when (value) {
                    minNum -> " Minimum reached"
                    maxNum -> " Maximum reached"
                    else -> " "
                },
                style = MaterialTheme.typography.labelMedium,
                color = when (value) {
                    1 -> MaterialTheme.colorScheme.error
                    10 -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
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
