package com.ballabotond.trackit.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object AppIcons {
    val Home = Icons.Outlined.Home
    val List = Icons.Outlined.List
    val Activity = Icons.Outlined.FitnessCenter
    val TrendingUp = Icons.Outlined.TrendingUp
    val User = Icons.Outlined.Person
    val Plus = Icons.Outlined.Add
    val Camera = Icons.Outlined.PhotoCamera
    val Weight = Icons.Outlined.MonitorWeight
    val Percent = Icons.Outlined.Percent
    val Ruler = Icons.Outlined.Height
    val Heart = Icons.Outlined.Favorite
    val Zap = Icons.Outlined.LocalFireDepartment
    val Target = Icons.Outlined.MonitorWeight
    val Calendar = Icons.Outlined.CalendarToday
    val Flame = Icons.Outlined.LocalFireDepartment
    val Eye = Icons.Outlined.Visibility
    val EyeOff = Icons.Outlined.VisibilityOff
    val Lock = Icons.Outlined.Lock
    val Mail = Icons.Outlined.Email
    val RefreshCw = Icons.Outlined.Sync
    val Image = Icons.Outlined.Image
    val HelpCircle = Icons.Outlined.Help
    val Users = Icons.Outlined.Groups
    val ArrowBack = Icons.Outlined.ArrowBack
    val ArrowForward = Icons.Outlined.ArrowForward
    val Add = Icons.Outlined.Add
    val Delete = Icons.Outlined.Delete
    val ChevronRight = Icons.Outlined.ChevronRight
    val Person = Icons.Outlined.Person
    val DateRange = Icons.Outlined.DateRange
    val Check = Icons.Outlined.Check
    val Star = Icons.Outlined.Star
    val Settings = Icons.Outlined.Settings
    val Share = Icons.Outlined.Share
    val Info = Icons.Outlined.Info
    val Build = Icons.Outlined.Build
    val ExitToApp = Icons.Outlined.Logout
    val UserCheck = Icons.Outlined.PersonSearch
    val Close = Icons.Outlined.Close
}

@Composable
fun AppIcon(
    icon: ImageVector,
    tint: Color = Color.White,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size)
        )
    }
}