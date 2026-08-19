package com.example.buddyworkout.core.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSize
import com.example.buddyworkout.core.ui.theme.avatarColorFor

/**
 * A person, as initials on a coloured disc (`.av`).
 *
 * The swatch is derived from [key] so someone keeps the same colour on every
 * screen. Pass [color] to pin a specific swatch where the design calls for one —
 * six colours cannot guarantee four distinct discs in a group otherwise.
 */
data class AvatarUi(
    val initials: String,
    val key: String = initials,
    val color: Color? = null,
    /** Drawn instead of the initials when present. */
    val photo: ImageBitmap? = null,
)

@Composable
fun Avatar(
    avatar: AvatarUi,
    modifier: Modifier = Modifier,
    size: Dp = BwSize.Thumb,
    ring: Boolean = false,
) {
    Box(
        modifier = modifier
            .size(size)
            .then(if (ring) Modifier.border(BwSize.AvatarRing, BwColors.Surface, CircleShape) else Modifier)
            .clip(CircleShape)
            .background(avatar.color ?: avatarColorFor(avatar.key)),
        contentAlignment = Alignment.Center,
    ) {
        val photo = avatar.photo
        if (photo != null) {
            Image(
                bitmap = photo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            return@Box
        }
        Text(
            text = avatar.initials,
            color = Color.White,
            // Initials track the disc: 15sp at 42dp, 20sp at 60dp in the mockups.
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = (size.value * 0.35f).sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

/** Overlapping row of avatars (`.avstack`), each with a white ring. */
@Composable
fun AvatarStack(
    avatars: List<AvatarUi>,
    modifier: Modifier = Modifier,
    size: Dp = 30.dp,
    max: Int = 4,
    overlap: Dp = BwSize.AvatarOverlap,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        avatars.take(max).forEachIndexed { index, avatar ->
            Avatar(
                avatar = avatar,
                size = size,
                ring = true,
                modifier = Modifier.offset(x = -(overlap * index)),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 340)
@Composable
private fun AvatarPreview() = BuddyWorkoutTheme {
    val people = listOf(AvatarUi("RK"), AvatarUi("PM"), AvatarUi("SV"), AvatarUi("AS"))
    PreviewColumn {
        Row(verticalAlignment = Alignment.CenterVertically) {
            people.forEach { Avatar(it, Modifier.size(42.dp)) }
        }
        AvatarStack(people)
        AvatarStack(people, size = 60.dp)
    }
}
