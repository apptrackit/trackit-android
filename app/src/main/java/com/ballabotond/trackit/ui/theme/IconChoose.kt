package com.ballabotond.trackit.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FeatherIcons
import compose.icons.feathericons.*

class IconChoose {
    companion object {
        fun getIcon(iconName: String): Pair<ImageVector, Color> {
            return when (iconName) {
                // Metrics
                "Weight" -> Pair(FeatherIcons.Target, Color(0xFF2196F3))  // Using Target as weight alternative
                "Body Fat" -> Pair(FeatherIcons.Percent, Color(0xFF4CAF50))
                "Height" -> Pair(FeatherIcons.Move, Color(0xFF9C27B0))     // Using Move as ruler alternative
                "Chest" -> Pair(FeatherIcons.User, Color(0xFFFF9800))
                "Waist" -> Pair(FeatherIcons.Target, Color(0xFF00BCD4))
                "Bicep", "Biceps" -> Pair(FeatherIcons.Activity, Color(0xFFFF5722))
                "Thigh" -> Pair(FeatherIcons.Users, Color(0xFF00BCD4))
                "Shoulder" -> Pair(FeatherIcons.User, Color(0xFFFF9800))
                "BMI" -> Pair(FeatherIcons.User, Color(0xFFFF9800))
                "Lean Body Mass" -> Pair(FeatherIcons.Heart, Color(0xFF4CAF50))
                "Fat Mass" -> Pair(FeatherIcons.Target, Color(0xFFB71C1C))  // Using Target as scale alternative
                "Fat-Free Mass Index" -> Pair(FeatherIcons.TrendingUp, Color(0xFF2196F3))
                "Basal Metabolic Rate", "BMR"-> Pair(FeatherIcons.Zap, Color(0xFFFF5722))  // Using Zap as flame alternative
                "Body Surface Area" -> Pair(FeatherIcons.Move, Color(0xFF00BCD4))   // Using Move as ruler alternative
                // Auth Icons
                "User" -> Pair(FeatherIcons.User, Color(0xFF4CAF50))
                "Lock" -> Pair(FeatherIcons.Lock, Color(0xFF4CAF50))
                "Eye" -> Pair(FeatherIcons.Eye, Color(0xFFAAAAAA))
                "EyeSlash" -> Pair(FeatherIcons.EyeOff, Color(0xFFAAAAAA))
                "Envelope" -> Pair(FeatherIcons.Mail, Color(0xFF4CAF50))
                "SyncAlt" -> Pair(FeatherIcons.RefreshCw, Color(0xFF4CAF50))
                // Photo Categories
                "Front" -> Pair(FeatherIcons.User, Color(0xFF2196F3))
                "Back" -> Pair(FeatherIcons.Users, Color(0xFF4CAF50))
                "Side" -> Pair(FeatherIcons.UserCheck, Color(0xFFFF9800))
                "Legs" -> Pair(FeatherIcons.Activity, Color(0xFF00BCD4))
                "Full Body" -> Pair(FeatherIcons.User, Color(0xFF9C27B0))
                "Other" -> Pair(FeatherIcons.Image, Color(0xFFAAAAAA))
                else -> Pair(FeatherIcons.HelpCircle, Color(0xFFAAAAAA))
            }
        }
    }
}
