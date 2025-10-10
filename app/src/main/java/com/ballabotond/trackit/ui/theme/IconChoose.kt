package com.ballabotond.trackit.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * A centralized utility to provide visual properties (icon and color) for different metrics.
 * This object maps a metric's string name to its corresponding Material Symbols icon name
 * and a designated theme color.
 */
object IconChoose {
    /**
     * Returns a Pair containing the Material Symbols icon name (String) and a Color
     * for a given metric or UI element name.
     *
     * @param name The name of the metric (e.g., "Weight", "BMI") or UI element ("User", "Lock").
     * @return A Pair where the first element is the icon name string and the second is the Color.
     */
    fun getVisuals(name: String): Pair<String, Color> {
        return when (name) {
            // Metrics
            "Weight" -> Pair("weight", Color(0xFF2196F3))
            "Body Fat" -> Pair("percent", Color(0xFF4CAF50))
            "Height" -> Pair("straighten", Color(0xFF9C27B0)) // "straighten" is the M3 ruler icon
            "Chest" -> Pair("man", Color(0xFFFF9800)) // "man" or "person" are good general body icons
            "Waist" -> Pair("square_foot", Color(0xFF00BCD4)) // "square_foot" looks like a tape measure
            "Bicep", "Biceps" -> Pair("fitness_center", Color(0xFFFF5722)) // M3 dumbbell icon
            "Thigh" -> Pair("accessibility_new", Color(0xFF00BCD4)) // A good generic body part icon
            "Shoulder" -> Pair("man", Color(0xFFFF9800))
            "BMI" -> Pair("bar_chart_4_bars", Color(0xFFFF9800))
            "Lean Body Mass" -> Pair("cardiology", Color(0xFF4CAF50)) // "cardiology" is a heart with a line
            "Fat Mass" -> Pair("weight_gradient", Color(0xFFB71C1C))
            "Fat-Free Mass Index" -> Pair("analytics", Color(0xFF2196F3)) // "analytics" or "monitoring"
            "Basal Metabolic Rate", "BMR" -> Pair("whatshot", Color(0xFFFF5722)) // "whatshot" is the M3 fire icon
            "Body Surface Area" -> Pair("texture", Color(0xFF00BCD4)) // "texture" represents surface

            // Auth Icons
            "User" -> Pair("person", Color(0xFF4CAF50))
            "Lock" -> Pair("lock", Color(0xFF4CAF50))
            "Eye" -> Pair("visibility", Color(0xFFAAAAAA))
            "EyeSlash" -> Pair("visibility_off", Color(0xFFAAAAAA))
            "Envelope" -> Pair("mail", Color(0xFF4CAF50)) // "mail" is the M3 equivalent
            "SyncAlt" -> Pair("sync", Color(0xFF4CAF50))

            // Photo Categories
            "Front" -> Pair("person", Color(0xFF2196F3))
            "Back" -> Pair("person", Color(0xFF4CAF50))
            "Side" -> Pair("person", Color(0xFFFF9800))
            "Legs" -> Pair("directions_walk", Color(0xFF00BCD4)) // "directions_walk" for legs/running
            "Full Body" -> Pair("body_system", Color(0xFF9C27B0))
            "Other" -> Pair("image", Color(0xFFAAAAAA))

            // Default fallback icon
            else -> Pair("help_outline", Color(0xFFAAAAAA)) // "help_outline" is the M3 question mark
        }
    }
}