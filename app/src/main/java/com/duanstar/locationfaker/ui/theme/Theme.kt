package com.duanstar.locationfaker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.runtime.Composable
import com.duanstar.locationfaker.ui.theme.AppColors.DarkColors
import com.duanstar.locationfaker.ui.theme.AppColors.LightColors

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = Shapes(),
        content = content
    )
}