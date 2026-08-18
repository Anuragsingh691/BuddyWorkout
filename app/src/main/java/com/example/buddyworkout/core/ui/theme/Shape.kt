package com.example.buddyworkout.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

/** Material 3 shape slots pinned to the radii used in the mockups. */
val BwShapes = Shapes(
    extraSmall = RoundedCornerShape(BwRadius.IconButton),
    small = RoundedCornerShape(BwRadius.Thumb),
    medium = RoundedCornerShape(BwRadius.Control),
    large = RoundedCornerShape(BwRadius.Card),
    extraLarge = RoundedCornerShape(BwRadius.Sheet),
)
