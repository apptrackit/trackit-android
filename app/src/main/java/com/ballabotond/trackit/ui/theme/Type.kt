package com.ballabotond.trackit.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ballabotond.trackit.R

// Define a font family for the OUTLINED style
val materialSymbolsOutlined = FontFamily(
    Font(R.font.material_symbols_outlined, FontWeight.Normal)
)

// Define a font family for the FILLED style
val materialSymbolsFilled = FontFamily(
    Font(R.font.material_symbols_filled, FontWeight.Normal)
)

// Your default Typography (can be left as is)
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
    /* Other text styles */
)