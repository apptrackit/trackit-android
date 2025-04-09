package com.example.lifetracker.ui.theme

import androidx.compose.runtime.Composable
import com.guru.fontawesomecomposelib.FaIcon
import com.guru.fontawesomecomposelib.FaIcons
import com.guru.fontawesomecomposelib.FaIconType

object FontAwesomeIcons {
    val Home = FaIcons.Home
    val List = FaIcons.ListAlt
    val Dumbbell = FaIcons.Dumbbell
    val ChartLine = FaIcons.ChartLine
    val User = FaIcons.User
}

@Composable
fun FontAwesomeIcon(icon: FaIconType.SolidIcon) {
    FaIcon(faIcon = icon)
}
