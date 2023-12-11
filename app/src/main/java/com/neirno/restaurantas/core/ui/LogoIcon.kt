package com.neirno.restaurantas.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.neirno.restaurantas.R

@Composable
fun LogoIcon(
    modifier: Modifier = Modifier
) {

    val isDarkTheme = isSystemInDarkTheme()

    val imageResource = if (isDarkTheme) {
        R.drawable.ic_restaurant_dark
    } else R.drawable.ic_restaurant_light

    Image(
        modifier = modifier,
        colorFilter = if (isDarkTheme) ColorFilter.tint(Color.White) else null,
        painter = painterResource(id = imageResource),
        contentDescription = null
    )
}