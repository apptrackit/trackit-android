package com.example.lifetracker.ui.theme

import androidx.compose.ui.graphics.Color
import com.guru.fontawesomecomposelib.FaIconType
import com.guru.fontawesomecomposelib.FaIcons
import java.util.Map.entry

class IconChoose {
    companion object {
        fun getIcon(iconName: String): Pair<FaIconType.SolidIcon, Color> {
            return when (iconName) {
                // Metrics
                "Weight" -> Pair(FaIcons.Weight, Color(0xFF2196F3))
                "Body Fat" -> Pair(FaIcons.Percent, Color(0xFF4CAF50))
                "Height" -> Pair(FaIcons.RulerVertical, Color(0xFF9C27B0))
                "Chest" -> Pair(FaIcons.Male, Color(0xFFFF9800))
                "Waist" -> Pair(FaIcons.Tape, Color(0xFF00BCD4))
                "Bicep", "Biceps" -> Pair(FaIcons.Dumbbell, Color(0xFFFF5722))
                "Thigh" -> Pair(FaIcons.Child, Color(0xFF00BCD4))
                "Shoulder" -> Pair(FaIcons.Male, Color(0xFFFF9800))
                "BMI" -> Pair(FaIcons.User, Color(0xFFFF9800))
                "Lean Body Mass" -> Pair(FaIcons.Heartbeat, Color(0xFF4CAF50))
                "Fat Mass" -> Pair(FaIcons.Weight, Color(0xFFB71C1C))
                "Fat-Free Mass Index" -> Pair(FaIcons.ChartLine, Color(0xFF2196F3))
                "Basal Metabolic Rate", "BMR"-> Pair(FaIcons.Fire, Color(0xFFFF5722))
                "Body Surface Area" -> Pair(FaIcons.RulerCombined, Color(0xFF00BCD4))
                // Photo Categories
                "Front" -> Pair(FaIcons.User, Color(0xFF2196F3))
                "Back" -> Pair(FaIcons.UserAlt, Color(0xFF4CAF50))
                "Side" -> Pair(FaIcons.UserAltSlash, Color(0xFFFF9800))
                "Legs" -> Pair(FaIcons.Running, Color(0xFF00BCD4))
                "Full Body" -> Pair(FaIcons.Male, Color(0xFF9C27B0))
                "Other" -> Pair(FaIcons.Image, Color(0xFFAAAAAA))
                else -> Pair(FaIcons.QuestionCircle, Color(0xFFAAAAAA))
            }
        }
    }
}
