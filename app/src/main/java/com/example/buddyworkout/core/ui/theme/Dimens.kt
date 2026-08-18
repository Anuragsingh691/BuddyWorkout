package com.example.buddyworkout.core.ui.theme

import androidx.compose.ui.unit.dp

/** Corner radii, measured off the Figma export. */
object BwRadius {
    val IconButton = 12.dp
    val Thumb = 13.dp
    val Control = 14.dp   // buttons, text fields
    val Card = 18.dp
    val Tile = 20.dp      // home action tiles, camera surface
    val Pill = 20.dp
    val Sheet = 28.dp
}

/** Fixed component heights, measured off the Figma export. */
object BwSize {
    val Button = 52.dp
    val ButtonCompact = 48.dp
    val ButtonSmall = 46.dp
    val Chip = 34.dp
    val TextField = 50.dp
    val TopBar = 52.dp
    val BottomNav = 64.dp
    val IconButton = 38.dp
    val Thumb = 42.dp

    /** Border width on cards, outlined buttons and text fields. */
    val Border = 1.5.dp

    /** Overlap between adjacent avatars in a stack. */
    val AvatarOverlap = 10.dp

    /** White ring drawn around each avatar in a stack. */
    val AvatarRing = 2.dp
}

/** Spacing scale. */
object BwSpace {
    val Xs = 4.dp
    val Sm = 8.dp
    val Md = 12.dp
    val Lg = 16.dp

    /** Horizontal screen gutter (`.body` padding in the export). */
    val Gutter = 18.dp
}
