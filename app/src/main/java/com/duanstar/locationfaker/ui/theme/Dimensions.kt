package com.duanstar.locationfaker.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Dimensions {

    val spacing: Dp
        get() = 16.dp

    val marginHorizontal: Dp
        get() = spacing
    val marginVertical: Dp
        get() = spacing
    val padding: PaddingValues
        get() = PaddingValues(horizontal = marginHorizontal, vertical = marginVertical)

}