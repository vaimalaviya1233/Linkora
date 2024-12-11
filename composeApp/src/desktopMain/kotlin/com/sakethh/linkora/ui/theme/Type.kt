package com.sakethh.linkora.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

val poppinsFontFamily = FontFamily(
    Font(
        resource = "font/semibold.ttf",
        weight = FontWeight.SemiBold
    ),
    Font(
        resource = "font/medium.ttf",
        weight = FontWeight.Medium
    ),
    Font(
        resource = "font/regular.ttf",
        weight = FontWeight.Normal
    )
)

val playWriteITTradFontFamily = FontFamily(
    Font(
        resource = "font/playwriteittrad.ttf",
        weight = FontWeight.Normal
    )
)

val DesktopTypography = Typography(
    titleLarge = TextStyle(fontFamily = poppinsFontFamily, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontFamily = poppinsFontFamily, fontWeight = FontWeight.Medium),
    titleSmall = TextStyle(fontFamily = poppinsFontFamily, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontFamily = playWriteITTradFontFamily, fontWeight = FontWeight.Normal)
)