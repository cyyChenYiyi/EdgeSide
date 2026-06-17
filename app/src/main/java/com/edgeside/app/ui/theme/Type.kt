package com.edgeside.app.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// iOS-leaning typography: slightly larger, bolder headings on the default font.
val IosTypography = Typography(
    h6 = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    body1 = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
    body2 = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
    caption = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal)
)
