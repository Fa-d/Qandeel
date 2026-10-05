package dev.sadakat.qandeel.shared.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Spacing: one rhythm for every screen. [gutter] is the page's side margin. */
@Immutable
data class CelestialSpacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val gutter: Dp = 20.dp,
)

/** Corner radii and the sizes components share. */
@Immutable
data class CelestialShapes(
    val card: Dp = 28.dp,
    val tile: Dp = 22.dp,
    val control: Dp = 16.dp,
    /** The floating player and tab bar: tall enough to thumb, low enough to float. */
    val barHeight: Dp = 64.dp,
    val touchTarget: Dp = 48.dp,
    val hairline: Dp = 1.dp,
)
