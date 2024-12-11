package com.sakethh.linkora.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

val poppinsFontFamily = FontFamily(
    Font(
        resource = "composeResources/linkora.composeapp.generated.resources/font/semibold.otf",
        weight = FontWeight.SemiBold
    ),
    Font(
        resource = "composeResources/linkora.composeapp.generated.resources/font/medium.otf",
        weight = FontWeight.Medium
    ),
    Font(
        resource = "composeResources/linkora.composeapp.generated.resources/font/regular.otf",
        weight = FontWeight.Normal
    )
)

val playWriteITTradFontFamily = FontFamily(
    Font(
        "composeResources/linkora.composeapp.generated.resources/font/playwriteittrad.otf",
        weight = FontWeight.Normal
    )
)

val Typography = Typography(
    titleLarge = TextStyle(fontFamily = poppinsFontFamily, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontFamily = poppinsFontFamily, fontWeight = FontWeight.Medium),
    titleSmall = TextStyle(fontFamily = poppinsFontFamily, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontFamily = playWriteITTradFontFamily, fontWeight = FontWeight.Normal)
)