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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = BwSpace.Gutter),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(72.dp))

        Box(
            modifier = Modifier
                .size(64.dp)
                .background(BwColors.PrimaryTint, RoundedCornerShape(BwRadius.Tile)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = BwIcons.Dumbbell,
                contentDescription = null,
                tint = BwColors.Primary,
                modifier = Modifier.size(30.dp),
            )
        }

        Spacer(Modifier.height(BwSpace.Lg))
        Text("BuddyWorkout", style = MaterialTheme.typography.headlineMedium, color = BwColors.Ink)
        Spacer(Modifier.height(BwSpace.Xs))
        Text(
            text = "Push each other. Count every rep.",
            style = MaterialTheme.typography.bodyLarge,
            color = BwColors.Muted,
        )

        Spacer(Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            state.error?.let { message ->
                NoticeCard(
                    text = message,
                    icon = BwIcons.AlertTriangle,
                    containerColor = BwColors.DangerTint,
                    contentColor = BwColors.DangerInk,
                )
            }

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
            BwTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = "Password",
                placeholder = "••••••••",
                isPassword = true,
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
            )

            BwButton(
                text = if (state.isLoading) "Signing in…" else "Sign in",
                onClick = onSignIn,
                enabled = state.canSubmit,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = "or",
                style = MaterialTheme.typography.bodyMedium,
                color = BwColors.Placeholder,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = BwSpace.Xs),
            )

            BwButton(
                text = "Continue with Google",
                onClick = onGoogleSignIn,
                variant = BwButtonVariant.Outline,
                icon = BwIcons.Google,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(BwSpace.Lg))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "New here? ",
                style = MaterialTheme.typography.bodyMedium,
                color = BwColors.Muted,
            )
            Text(
                text = "Create an account",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = BwColors.Primary,
                modifier = Modifier.clickable(enabled = !state.isLoading, onClick = onRegisterClick),
            )
        }

        Spacer(Modifier.height(BwSpace.Gutter))
    }
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
