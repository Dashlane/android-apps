package com.dashlane.item.passwordhistory

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.IndeterminateLoader
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.item.passwordhistory.PasswordHistoryEntry.CredentialPasswordHistoryEntry
import com.dashlane.item.passwordhistory.PasswordHistoryEntry.GeneratedPasswordHistoryEntry
import com.dashlane.item.passwordhistory.PasswordHistoryViewModel.PasswordHistoryState

@Composable
fun PasswordHistoryScreen(
    modifier: Modifier = Modifier,
    state: PasswordHistoryState,
    onRevertClick: (CredentialPasswordHistoryEntry) -> Boolean,
    onCopyClick: (PasswordHistoryEntry) -> Boolean,
) {
    if (state is PasswordHistoryState.Init) {
        Box(
            modifier = modifier
                .padding(8.dp)
                .fillMaxWidth(),
        ) {
            IndeterminateLoader(
                modifier = Modifier
                    .padding(100.dp)
                    .align(Alignment.Center),
            )
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.entries) {
                PasswordHistoryItem(it, onRevertClick, onCopyClick)
            }
        }
    }
}

@Composable
private fun PasswordHistoryItem(
    entry: PasswordHistoryEntry,
    onRevertClick: (CredentialPasswordHistoryEntry) -> Boolean,
    onCopyClick: (PasswordHistoryEntry) -> Boolean,
) {
    when (entry) {
        is CredentialPasswordHistoryEntry ->
            CredentialPasswordHistoryItem(
                password = entry.password,
                dateString = entry.lastModifiedDateString,
                onRevertClick = { onRevertClick(entry) },
                onCopyClick = { onCopyClick(entry) },
            )
        is GeneratedPasswordHistoryEntry -> GeneratedPasswordHistoryItem(
            password = entry.password,
            dateString = entry.lastModifiedDateString,
            domain = entry.domain,
            saved = entry.saved,
            onCopyClick = { onCopyClick(entry) },
        )
    }
}

@SuppressLint("InternalTestExpressions")
@Composable
@Preview
private fun PasswordHistoryScreenPreview() {
    DashlanePreview {
        PasswordHistoryScreen(
            state = PasswordHistoryState.Loaded(
                listOf(
                    CredentialPasswordHistoryEntry("1", "Azerty12", "2 minutes ago"),
                    CredentialPasswordHistoryEntry("2", "Dashlane12", "last month"),
                ),
            ),
            onRevertClick = { true },
            onCopyClick = { true },
        )
    }
}

@SuppressLint("InternalTestExpressions")
@Composable
@Preview
private fun PasswordGeneratedHistoryScreenPreview() {
    DashlanePreview {
        PasswordHistoryScreen(
            state = PasswordHistoryState.Loaded(
                listOf(
                    GeneratedPasswordHistoryEntry("Azerty12", "2 minutes ago", domain = "google.com", saved = true),
                    GeneratedPasswordHistoryEntry("Dashlane12", "last month", domain = null, saved = false),
                ),
            ),
            onRevertClick = { true },
            onCopyClick = { true },
        )
    }
}

@Composable
@Preview
private fun PasswordHistoryScreenLoadingPreview() {
    DashlanePreview {
        PasswordHistoryScreen(
            state = PasswordHistoryState.Init,
            onRevertClick = { true },
            onCopyClick = { true },
        )
    }
}