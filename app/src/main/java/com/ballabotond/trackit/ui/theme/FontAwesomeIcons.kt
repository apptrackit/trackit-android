package com.example.trackit.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.guru.fontawesomecomposelib.FaIcon
import com.guru.fontawesomecomposelib.FaIcons
import com.guru.fontawesomecomposelib.FaIconType
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment

object FontAwesomeIcons {
    val Home = FaIcons.Home
    val List = FaIcons.ListAlt
    val Dumbbell = FaIcons.Dumbbell
    val ChartLine = FaIcons.ChartLine
    val User = FaIcons.User
    val Plus = FaIcons.Plus
    val Images = FaIcons.Images
    
    // Add health metric related icons
    val Weight = FaIcons.Weight
    val Percent = FaIcons.Percent
    val RulerVertical = FaIcons.RulerVertical
    val Heartbeat = FaIcons.Heartbeat
    val Running = FaIcons.Running
    val Child = FaIcons.Child
    val Tape = FaIcons.Tape
    val Fire = FaIcons.Fire
}

@Composable
fun FontAwesomeIcon(
    icon: FaIconType.SolidIcon,
    tint: Color = Color.White,  // Changed default to white
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        FaIcon(
            faIcon = icon,
            size = 20.dp,  // Changed from 24.dp to match navigation
            tint = tint
        )
    }
}
