package com.example.healt4u.screen.componentUI

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun TimingButton(
    text: String,
    isSelected: Boolean,
    id : Int,
    onClick: () -> Unit
) {
    val selectedColor = Color(0xFF70BCFD)
    val unselectedColor = Color.LightGray

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) selectedColor.copy(alpha = 0.15f) else Color.White
        ),
        border = if (isSelected) BorderStroke(2.dp, selectedColor) else BorderStroke(1.dp, unselectedColor),
        modifier = Modifier
            .width(140.dp)
            .height(90.dp) // Height to fit the icon + text
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = id),
                contentDescription = "Medication timing icon",
                modifier = Modifier
                    .size(width = 56.dp, height = 17.dp)
                    .padding(bottom = 8.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = text,
                fontSize = 14.sp,
                color = if (isSelected) selectedColor else Color.Gray
            )
        }
    }
}