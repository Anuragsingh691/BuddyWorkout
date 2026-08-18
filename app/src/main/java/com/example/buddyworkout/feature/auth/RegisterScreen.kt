package com.example.buddyworkout.feature.auth

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwTextField
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.NoticeCard
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Gap between stacked `.field` blocks in the export (`margin-top:14px`). */
private val FieldGap = 14.dp

/** Diameter of the avatar well and of the "+" badge pinned to its corner. */
private val AvatarSize = 96.dp
private val BadgeSize = 32.dp

/** White ring drawn around the badge (`border:3px solid #fff`). */
private val BadgeRing = 3.dp

@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPickPhoto: () -> Unit,
    onCreateAccount: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg)
            .imePadding(),
    ) {
        BwTopBar(title = "Create account", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = BwSpace.Gutter),
        ) {
            Spacer(Modifier.height(BwSpace.Sm))

            ProfilePhotoPicker(
                photoUri = state.photoUri,
                onClick = onPickPhoto,
                enabled = !state.isLoading,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            state.error?.let { message ->
                Spacer(Modifier.height(BwSpace.Lg))
                NoticeCard(
                    text = message,
                    icon = BwIcons.AlertTriangle,
                    containerColor = BwColors.DangerTint,
                    contentColor = BwColors.DangerInk,
                )
            }

            Spacer(Modifier.height(FieldGap))

            BwTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = "Full name",
                placeholder = "Anurag Shishodia",
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )

            Spacer(Modifier.height(FieldGap))

            BwTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = "Email",
                placeholder = "you@example.com",
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
            )

            Spacer(Modifier.height(FieldGap))

            // Not in the mockup, which shows a Google-only sign-up. The app
            // also registers with email + password, so the field has to exist.
            BwTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = "Password",
                labelHint = "At least 6 characters",
                placeholder = "••••••••",
                isPassword = true,
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next,
                ),
            )

            Spacer(Modifier.height(FieldGap))

            BwTextField(
                value = state.phone,
                onValueChange = onPhoneChange,
                label = "Phone",
                labelHint = "(optional)",
                placeholder = "+91 ·····",
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done,
                ),
            )

            Spacer(Modifier.height(BwSpace.Gutter))
        }

        // `margin-top:auto` in the export — the button sits on the bottom edge.
        BwButton(
            text = if (state.isLoading) "Creating…" else "Create account",
            onClick = onCreateAccount,
            enabled = state.canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = BwSpace.Gutter)
                .padding(bottom = BwSpace.Gutter),
        )
    }
}

/**
 * The 96dp avatar well with the green "+" badge. Tapping either opens the
 * system photo picker; the chosen image is shown locally only — nothing
 * uploads it yet.
 */
@Composable
private fun ProfilePhotoPicker(
    photoUri: String?,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val photo = rememberPhotoBitmap(photoUri)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // The badge overhangs the avatar's edge, so the circular clip belongs
        // on the well itself rather than on this wrapper.
        Box(
            modifier = Modifier
                .size(AvatarSize)
                .clickable(enabled = enabled, onClick = onClick),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(BwColors.PrimaryTint),
                contentAlignment = Alignment.Center,
            ) {
                if (photo != null) {
                    Image(
                        bitmap = photo,
                        contentDescription = "Profile photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        imageVector = BwIcons.Person,
                        contentDescription = null,
                        tint = BwColors.PrimaryDark,
                        modifier = Modifier.size(38.dp),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(y = (-2).dp)
                    .size(BadgeSize)
                    // The white ring is drawn as an outer disc the green badge
                    // is inset into, so it reads the same over any backdrop.
                    .background(BwColors.Surface, CircleShape)
                    .padding(BadgeRing)
                    .background(BwColors.Primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = BwIcons.Plus,
                    contentDescription = null,
                    tint = BwColors.Surface,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        Spacer(Modifier.height(BwSpace.Sm))

        Text(
            text = if (photoUri == null) "Add a profile photo" else "Change photo",
            style = MaterialTheme.typography.bodyMedium,
            color = BwColors.Muted,
        )
    }
}

/**
 * Decodes a picked image off the main thread, downsampled to roughly the size
 * it is drawn at — a full-resolution phone photo would be tens of megabytes in
 * memory for a 96dp circle.
 */
@Composable
private fun rememberPhotoBitmap(uri: String?): ImageBitmap? {
    val context = LocalContext.current
    var bitmap by remember(uri) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(uri) {
        bitmap = uri?.let { value ->
            withContext(Dispatchers.IO) {
                runCatching { decodeDownsampled(context, Uri.parse(value)) }.getOrNull()
            }
        }
    }
    return bitmap
}

/** Target edge length, in pixels, for the decoded avatar. */
private const val AVATAR_TARGET_PX = 288

private fun decodeDownsampled(
    context: android.content.Context,
    uri: Uri,
): ImageBitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

    var sample = 1
    val longest = maxOf(bounds.outWidth, bounds.outHeight)
    while (longest / sample > AVATAR_TARGET_PX * 2) sample *= 2

    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    return context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, options)?.asImageBitmap()
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun RegisterScreenPreview() = BuddyWorkoutTheme {
    RegisterScreen(PreviewData.register, {}, {}, {}, {}, {}, {}, {})
}
