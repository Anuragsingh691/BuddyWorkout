package com.example.buddyworkout.feature.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.buddyworkout.core.ui.component.Avatar
import com.example.buddyworkout.core.ui.component.AvatarUi
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwButtonVariant
import com.example.buddyworkout.core.ui.component.BwTextField
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwRadius
import com.example.buddyworkout.core.ui.theme.BwSize
import com.example.buddyworkout.core.ui.theme.BwSpace

/**
 * Adds a buddy to an existing challenge by the email they signed up with.
 *
 * Nothing in the export covers this — the mockups only ever add buddies while
 * creating a challenge, from a buddy list that does not exist yet. Look-up then
 * confirm, in two steps, because adding somebody spends one of their two
 * challenge slots without asking them.
 */
@Composable
fun AddBuddyDialog(
    state: AddBuddyUiState,
    onEmailChange: (String) -> Unit,
    onSearch: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!state.isOpen) return

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BwColors.Surface, RoundedCornerShape(BwRadius.Card))
                .padding(BwSpace.Gutter),
        ) {
            Text(
                text = "Add a buddy",
                style = MaterialTheme.typography.titleLarge,
                color = BwColors.Ink,
            )
            Text(
                text = "They join straight away, using one of their two challenge slots.",
                style = MaterialTheme.typography.bodyMedium,
                color = BwColors.Muted,
                modifier = Modifier.padding(top = BwSpace.Xs),
            )

            BwTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = "Their email",
                placeholder = "buddy@example.com",
                enabled = !state.isSearching,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Search,
                ),
                modifier = Modifier.padding(top = BwSpace.Lg),
            )

            state.found?.let { person ->
                Row(
                    modifier = Modifier
                        .padding(top = BwSpace.Md)
                        .fillMaxWidth()
                        .background(BwColors.PrimaryTint, RoundedCornerShape(BwRadius.Control))
                        .padding(BwSpace.Md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(BwSpace.Md),
                ) {
                    Avatar(AvatarUi(initials = person.name.initials(), key = person.uid))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = person.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = BwColors.Ink,
                        )
                        Text(
                            text = person.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = BwColors.PrimaryDark,
                        )
                    }
                }
            }

            state.error?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = BwColors.DangerInk,
                    modifier = Modifier.padding(top = BwSpace.Md),
                )
            }

            Row(
                modifier = Modifier
                    .padding(top = BwSpace.Lg)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(BwSpace.Md),
            ) {
                BwButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    variant = BwButtonVariant.Outline,
                    height = BwSize.ButtonCompact,
                    modifier = Modifier.weight(1f),
                )
                // One button, two jobs: find them, then add them. A separate
                // disabled Add button beside Find would just be dead weight
                // until a match exists.
                if (state.found == null) {
                    BwButton(
                        text = if (state.isSearching) "Finding…" else "Find",
                        onClick = onSearch,
                        enabled = state.canSearch,
                        height = BwSize.ButtonCompact,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    BwButton(
                        text = if (state.isSearching) "Adding…" else "Add",
                        onClick = onConfirm,
                        enabled = state.canAdd,
                        height = BwSize.ButtonCompact,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/** First letter of each of the first two words, e.g. "Rohit Kumar" -> "RK". */
private fun String.initials(): String = trim()
    .split(Regex("\\s+"))
    .filter { it.isNotBlank() }
    .take(2)
    .map { it.first().uppercaseChar() }
    .joinToString("")

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun AddBuddyDialogPreview() = BuddyWorkoutTheme {
    AddBuddyDialog(
        state = AddBuddyUiState(
            isOpen = true,
            email = "rohit@example.com",
            found = FoundBuddyUi("u1", "Rohit Kumar", "rohit@example.com", null),
        ),
        onEmailChange = {}, onSearch = {}, onConfirm = {}, onDismiss = {},
    )
}
