package com.duanstar.locationfaker.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
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

    @Composable
    fun cardElevation(isLight: Boolean = MaterialTheme.colors.isLight): Dp {
        return if (isLight) 1.dp else 4.dp
    }
}

