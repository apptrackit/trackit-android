package com.ballabotond.trackit.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ballabotond.trackit.R

// Define the FontFamily for the Material Symbols font
val materialSymbols = FontFamily(
    Font(R.font.material_symbols_outlined, FontWeight.Normal)
)

// Default Typography setup (can remain as it was)
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other text styles */
)