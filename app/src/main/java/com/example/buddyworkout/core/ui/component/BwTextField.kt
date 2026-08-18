package com.example.buddyworkout.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwRadius
import com.example.buddyworkout.core.ui.theme.BwSize

/**
 * Labelled text field (`.field` + `.input`). The border turns green on focus,
 * matching the `.input.focus` state in the mockups.
 */
@Composable
fun BwTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    labelHint: String? = null,
    placeholder: String = "",
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isPassword: Boolean = false,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val shape = RoundedCornerShape(BwRadius.Control)

    Column(modifier) {
        if (label != null) {
            Row(Modifier.padding(bottom = 6.dp)) {
                Text(
                    text = label,
                    color = BwColors.LabelInk,
                    style = MaterialTheme.typography.labelLarge,
                )
                if (labelHint != null) {
                    Text(
                        text = " $labelHint",
                        color = BwColors.Placeholder,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Normal,
                        ),
                    )
                }
            }
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            interactionSource = interactionSource,
            textStyle = LocalTextStyle.current.merge(
                MaterialTheme.typography.bodyLarge.copy(color = BwColors.Ink),
            ),
            cursorBrush = SolidColor(BwColors.Primary),
            keyboardOptions = keyboardOptions,
            visualTransformation =
                if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier.fillMaxWidth(),
        ) { field ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BwSize.TextField)
                    .border(
                        width = BwSize.Border,
                        color = if (focused) BwColors.Primary else BwColors.Line,
                        shape = shape,
                    )
                    .background(BwColors.Surface, shape)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = BwColors.Placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    field()
                }
                if (trailingIcon != null) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = BwColors.Placeholder,
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(20.dp)
                            .then(
                                if (onTrailingIconClick != null) {
                                    Modifier.clickable(onClick = onTrailingIconClick)
                                } else {
                                    Modifier
                                }
                            ),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 340)
@Composable
private fun BwTextFieldPreview() = BuddyWorkoutTheme {
    PreviewColumn {
        BwTextField("anurag@gmail.com", {}, label = "Email")
        BwTextField("password", {}, label = "Password", trailingIcon = BwIcons.Eye, isPassword = true)
        BwTextField("", {}, label = "Phone", labelHint = "(optional)", placeholder = "+91 ·····")
    }
}
