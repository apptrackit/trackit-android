package com.ballabotond.trackit.ui.common

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.ballabotond.trackit.ui.theme.materialSymbols

/**
 * A scalable and centralized Composable for displaying Material Symbols Outlined icons
 * from the integrated TTF font file.
 *
 * This function renders an icon by its string name, replacing the need for Icons.Filled.*.
 *
 * @param iconName The string identifier of the Material Symbol (e.g., "weight", "monitoring").
 * @param modifier The modifier to be applied to the icon.
 * @param tint The tint color for the icon. Defaults to the local content color.
 * @param contentDescription The accessibility description for the icon.
 */
@Composable
fun AppIcon(
    iconName: String,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    contentDescription: String? = null
) {
    Text(
        modifier = modifier,
        text = iconName,
        fontFamily = materialSymbols,
        color = tint,
        // Ensure the font settings are optimized for icon rendering.
        // Using a specific size or inheriting from the theme is fine.
        fontSize = MaterialTheme.typography.bodyLarge.fontSize.times(1.2f), // Slightly larger than text
        lineHeight = 1.sp // Collapse line height to prevent extra vertical space
    )
}