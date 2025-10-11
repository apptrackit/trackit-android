package com.ballabotond.trackit.ui.common

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.ballabotond.trackit.ui.theme.materialSymbolsFilled
import com.ballabotond.trackit.ui.theme.materialSymbolsOutlined

/**
 * A stable icon composable that renders either the outlined or filled icon
 * by choosing the appropriate font family.
 */
@Composable
fun AppIcon(
    iconName: String,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    filled: Boolean = true, // This parameter now chooses the font
    contentDescription: String? = null
) {
    // Choose which font to use based on the 'filled' parameter
    val selectedFontFamily = if (filled) {
        materialSymbolsFilled
    } else {
        materialSymbolsOutlined
    }

    Text(
        text = iconName,
        modifier = modifier,
        color = tint,
        style = TextStyle(
            fontFamily = selectedFontFamily, // Use the selected font
            fontSize = 25.sp, // A good default base size
            lineHeight = 1.sp // Prevents extra vertical spacing
        )
    )
}