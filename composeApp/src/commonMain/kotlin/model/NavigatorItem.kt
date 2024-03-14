package model

import androidx.compose.ui.graphics.vector.ImageVector

data class NavigatorItem(
    val title: String,
    val icon: ImageVector,
    val iconSelected: ImageVector
)
