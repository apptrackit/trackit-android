package com.ballabotond.trackit.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

class IconChoose {
    companion object {
        fun getIcon(iconName: String): Pair<ImageVector, Color> {
            val defaultColor = Color(0xFF4CAF50)

            return when (iconName) {
                "Weight" -> Pair(Icons.Default.Scale, defaultColor)
                "Body Fat" -> Pair(Icons.Default.Percent, defaultColor)
                "Height" -> Pair(Icons.Default.Height, defaultColor)
                "Chest" -> Pair(Icons.Default.Favorite, defaultColor)
                "Waist" -> Pair(Icons.Default.SquareFoot, defaultColor)
                "Bicep", "Biceps" -> Pair(Icons.Default.FitnessCenter, defaultColor)
                "Thigh" -> Pair(Icons.Default.DirectionsRun, defaultColor)
                "Shoulder" -> Pair(Icons.Default.AccessibilityNew, defaultColor)
                "BMI" -> Pair(Icons.Default.Scale, defaultColor)
                "Lean Body Mass" -> Pair(Icons.Default.AccessibilityNew, defaultColor)
                "Fat Mass" -> Pair(Icons.Default.MonitorWeight, defaultColor)
                "Fat-Free Mass Index" -> Pair(Icons.Default.TrendingUp, defaultColor)
                "Basal Metabolic Rate", "BMR" -> Pair(Icons.Default.LocalFireDepartment, defaultColor)
                "Body Surface Area" -> Pair(Icons.Default.Person, defaultColor)

                // Auth Icons
                "User" -> Pair(Icons.Default.Person, defaultColor)
                "Lock" -> Pair(Icons.Default.Lock, defaultColor)
                "Eye" -> Pair(Icons.Default.Visibility, defaultColor)
                "EyeSlash" -> Pair(Icons.Default.VisibilityOff, defaultColor)
                "Envelope" -> Pair(Icons.Default.Email, defaultColor)
                "SyncAlt" -> Pair(Icons.Default.Sync, defaultColor)

                // Photo Categories
                "Front" -> Pair(Icons.Default.CameraFront, defaultColor)
                "Back" -> Pair(Icons.Default.CameraRear, defaultColor)
                "Side" -> Pair(Icons.Default.Camera, defaultColor)
                "Legs" -> Pair(Icons.Default.DirectionsRun, defaultColor)
                "Full Body" -> Pair(Icons.Default.Person, defaultColor)
                "Other" -> Pair(Icons.Default.Image, defaultColor)

                else -> Pair(Icons.Default.Help, defaultColor)
            }
        }
    }
}