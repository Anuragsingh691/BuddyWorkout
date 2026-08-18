package com.example.buddyworkout.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace

/** Shared layout for the `@Preview`s in this package. Not used by real screens. */
@Composable
internal fun PreviewColumn(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BwColors.Bg)
            .padding(BwSpace.Gutter),
        verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
    ) { content() }
}
