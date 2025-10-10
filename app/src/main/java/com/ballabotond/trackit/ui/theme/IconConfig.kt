// You can add this right inside IconChoose.kt, before the object definition

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A data class to hold all visual configuration for an AppIcon.
 *
 * @param iconName The string name of the icon.
 * @param tint The color to tint the icon.
 * @param filled Whether the icon should be filled. Defaults to false.
 * @param size The default size for this icon. Defaults to 24.dp.
 */
data class IconConfig(
    val iconName: String,
    val tint: Color,
    val filled: Boolean = false, // Default to outlined
    val size: Dp = 24.dp         // Default size
)