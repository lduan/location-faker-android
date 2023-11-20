package com.duanstar.locationfaker.ui.theme

import androidx.compose.material.Colors
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

object AppColors {

    private val Red10 = Color(0xFFFF8A80)
    private val Red20 = Color(0xFFFF5252)
    private val Red40 = Color(0xFFFF1744)

    private val BlueGrey90 = Color(0xff263238)
    private val BlueGrey80 = Color(0xff37474F)
    private val BlueGrey60 = Color(0xff546E7A)

    private val DarkBackground = Color(0xff243036)

    val DarkColors = darkColors(
        primary = BlueGrey80,
        primaryVariant = BlueGrey90,
        secondary = BlueGrey80,
        secondaryVariant = BlueGrey60,
        background = DarkBackground,
        surface = DarkBackground,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color.White,
        onSurface = Color.White
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

val Colors.primaryOnSurface
    get() = if (isLight) primary else onSurface