package com.example.buddyworkout.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwButtonVariant
import com.example.buddyworkout.core.ui.component.BwTextField
import com.example.buddyworkout.core.ui.component.NoticeCard
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwRadius
import com.example.buddyworkout.core.ui.theme.BwSpace

/** Gap between stacked `.field` blocks in the export (`margin-top:14px`). */
private val FieldGap = 14.dp

@Composable
fun LoginScreen(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg)
            .imePadding()
            .padding(horizontal = BwSpace.Gutter),
    ) {
        // `.center` in the export: the form is centred in the body and the
        // register link sits on the bottom edge, not directly under the form.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(BwColors.Primary, RoundedCornerShape(BwRadius.Tile)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = BwIcons.Dumbbell,
                    contentDescription = null,
                    tint = BwColors.Surface,
                    modifier = Modifier.size(34.dp),
                )
            }

            Spacer(Modifier.height(22.dp))

            Text(
                text = buildAnnotatedString {
                    append("Comm")
                    withStyle(SpanStyle(color = BwColors.Primary)) { append("Workout") }
                },
                style = MaterialTheme.typography.headlineMedium,
                color = BwColors.Ink,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(BwSpace.Sm))

            Text(
                text = "Train together. Compete with buddies.",
                style = MaterialTheme.typography.bodyMedium,
                color = BwColors.Muted,
                textAlign = TextAlign.Center,
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

            BwTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = "Password",
                placeholder = "••••••••",
                trailingIcon = BwIcons.Eye,
                onTrailingIconClick = { passwordVisible = !passwordVisible },
                isPassword = !passwordVisible,
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
            )

            Spacer(Modifier.height(22.dp))

            BwButton(
                text = if (state.isLoading) "Logging in…" else "Log in",
                onClick = onSignIn,
                enabled = state.canSubmit,
                modifier = Modifier.fillMaxWidth(),
            )

            OrDivider()

            BwButton(
                text = "Continue with Google",
                onClick = onGoogleSignIn,
                variant = BwButtonVariant.Outline,
                icon = BwIcons.Google,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = BwSpace.Gutter),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Don't have an account? ",
                style = MaterialTheme.typography.bodyMedium,
                color = BwColors.Muted,
            )
            Text(
                text = "Register",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = BwColors.PrimaryDark,
                modifier = Modifier.clickable(enabled = !state.isLoading, onClick = onRegisterClick),
            )
        }
    }
}

/** `.divider` — the word "or" between two hairlines. */
@Composable
private fun OrDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = BwSpace.Gutter),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BwSpace.Md),
    ) {
        Rule(Modifier.weight(1f))
        Text(
            text = "or",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Normal),
            color = BwColors.Placeholder,
        )
        Rule(Modifier.weight(1f))
    }
}

@Composable
private fun Rule(modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(1.dp)
            .background(BwColors.Line),
    )
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun LoginScreenPreview() = BuddyWorkoutTheme {
    LoginScreen(PreviewData.login, {}, {}, {}, {}, {})
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun LoginScreenErrorPreview() = BuddyWorkoutTheme {
    LoginScreen(
        PreviewData.login.copy(error = "That email and password don't match."),
        {}, {}, {}, {}, {},
    )
}
