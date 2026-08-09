package com.example.healt4u.screen.componentUI

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.healt4u.R
import kotlin.String
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.healt4u.screen.componentUI.Theme.colorTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun dropDownMenu(
    modifier: Modifier = Modifier,
    categories : List<String>,
    label: String ="",
    onValueChange: (String) -> Unit = {}
){
    var expanded by remember { mutableStateOf <Boolean>(false) }
    var selectedItem by remember { mutableStateOf(categories.first()) }
    colorTheme (content= {
        Column(
            modifier = modifier
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                InputTextField(
                    value = selectedItem,
                    onValueChange = {},
                    label = label,
                    modifier = Modifier.border(3.dp, color = Color(0))
                        .fillMaxWidth()
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        ),
                    readOnly = true,
                    trailingIcon = {

                        if (expanded) {
                            Icon(
                                painter = painterResource(R.drawable.up_arrow),
                                contentDescription = "up arrow",
                                tint = Color.Unspecified
                            )

                        } else {
                            Icon(
                                painter = painterResource(R.drawable.down_arrow),
                                contentDescription = "down arrow",
                                tint = Color.Unspecified
                            )

                        }
                    }
                )
                ExposedDropdownMenu(
                    expanded = expanded, onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(20.dp)
                        )
                        .shadow(4.dp, RoundedCornerShape(12.dp))

                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedItem = category
                                expanded = false
                                onValueChange(selectedItem)

                            }
                        )

                    }
                }
            }
        }
    }
    )
        }








@Preview(
    name = "Default Dropdown",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun PreviewDropDownMenu() {
    // Sample data for preview
    val sampleCategories = listOf("Category A", "Category B", "Category C", "Category D")
    val sampleSelectedItem = "Category A"
    var c by remember { mutableStateOf("") }

    dropDownMenu(
        modifier=Modifier.fillMaxSize(),
        categories = sampleCategories,
    label="dd",
        onValueChange = {newValue ->c = newValue }
    )

}