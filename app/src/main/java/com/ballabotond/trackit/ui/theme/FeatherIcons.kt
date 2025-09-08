package com.ballabotond.trackit.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.*

object FeatherIconsCollection {
    val Home = FeatherIcons.Home
    val List = FeatherIcons.List
    val Activity = FeatherIcons.Activity
    val TrendingUp = FeatherIcons.TrendingUp
    val User = FeatherIcons.User
    val Plus = FeatherIcons.Plus
    val Camera = FeatherIcons.Camera
    
    // Health metric related icons
    val Weight = FeatherIcons.Target  // Using Target as weight alternative
    val Percent = FeatherIcons.Percent
    val Ruler = FeatherIcons.Move     // Using Move as ruler alternative  
    val Heart = FeatherIcons.Heart
    val Zap = FeatherIcons.Zap
    val Target = FeatherIcons.Target
    val Calendar = FeatherIcons.Calendar
    val Eye = FeatherIcons.Eye
    val EyeOff = FeatherIcons.EyeOff
    val Lock = FeatherIcons.Lock
    val Mail = FeatherIcons.Mail
    val RefreshCw = FeatherIcons.RefreshCw
    val Image = FeatherIcons.Image
    val HelpCircle = FeatherIcons.HelpCircle
    val Users = FeatherIcons.Users
    val Flame = FeatherIcons.Zap      // Using Zap as flame alternative
    val ArrowBack = FeatherIcons.ArrowLeft
    val ArrowForward = FeatherIcons.ArrowRight
    val Add = FeatherIcons.Plus
    val Delete = FeatherIcons.Trash2
    val ChevronRight = FeatherIcons.ChevronRight
    val Person = FeatherIcons.User
    val DateRange = FeatherIcons.Calendar
    val Check = FeatherIcons.Check
    val Star = FeatherIcons.Star
    val Settings = FeatherIcons.Settings
    val Share = FeatherIcons.Share2
    val Info = FeatherIcons.Info
    val Build = FeatherIcons.Tool
    val ExitToApp = FeatherIcons.LogOut
    val UserCheck = FeatherIcons.UserCheck
}

@Composable
fun FeatherIcon(
    icon: ImageVector,
    tint: Color = Color.White,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size)
        )
    }
}
