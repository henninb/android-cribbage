package com.brianhenning.cribbage.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object First : Screen(
        route = "first",
        title = "Cribbage",
        icon = Icons.Default.Home
    )

    object Second : Screen(
        route = "second",
        title = "Second",
        icon = Icons.Default.Favorite
    )

    object Third : Screen(
        route = "third",
        title = "Third",
        icon = Icons.Default.Settings
    )
}