package com.duanstar.locationfaker.ui.theme

import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

object AppColors {

    val Red10 = Color(0xFFFF8A80)
    val Red20 = Color(0xFFFF5252)
    val Red40 = Color(0xFFFF1744)

    val DarkColors = darkColors(
//        primary = Primary,
//        primaryVariant = PrimaryVariant,
//        secondary = Color.White,
//        onPrimary = Color.White
    )

    val LightColors = lightColors(
        primary = Red20,
        primaryVariant = Red10,
        secondary = Red20,
        secondaryVariant = Red40,
        background = Color.White,
        surface = Color.White,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color.Black,
        onSurface = Color.Black
    )
}
