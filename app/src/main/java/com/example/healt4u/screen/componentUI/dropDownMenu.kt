package com.example.healt4u.screen.componentUI

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import java.lang.reflect.Type
import kotlin.reflect.KType

@Composable
fun dropDownMenu(type : Type, itemList: List<KType>,){
    var expended by remember { mutableStateOf <Boolean>(false) }
    var selectedItem by remember { mutableStateOf(itemList.first()) }
    var mTextFieldSize by remember {mutableStateOf(Size.Zero)}


}