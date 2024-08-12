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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.item.passwordhistory.PasswordHistoryViewModel.PasswordHistoryState
import com.dashlane.ui.common.compose.components.basescreen.AppBarScreenWrapper
import com.dashlane.ui.widgets.view.CircularProgressIndicator

@Deprecated(
    "This version of password history is deprecated, please check with the team before any modification.",
    replaceWith = ReplaceWith("PasswordHistoryScreen")
)
@Composable
fun PasswordHistoryScreenOld(
    modifier: Modifier = Modifier,
    state: PasswordHistoryState,
    onRevertClick: (PasswordHistoryEntry) -> Boolean,
    onCopyClick: (PasswordHistoryEntry) -> Boolean,
    onBackNavigationClick: () -> Unit,
) {
    AppBarScreenWrapper(
        titleText = stringResource(R.string.password_history_action_bar_title),
        navigationIconToken = IconToken(R.drawable.ic_arrow_left_outlined),
        onNavigationClick = onBackNavigationClick,
    ) {
        if (state is PasswordHistoryState.Init) {
            Box(
                modifier = modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                CircularProgressIndicator(
                    color = DashlaneTheme.colors.textBrandQuiet.value,
                    modifier = Modifier
                        .padding(100.dp)
                        .align(Alignment.Center)
                )
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.entries) {
                    PasswordHistoryItem(
                        password = it.password,
                        dateString = it.lastModifiedDateString,
                        onRevertClick = { onRevertClick(it) },
                        onCopyClick = { onCopyClick(it) },
                    )
                }
            }
        }
    }
}

@SuppressLint("InternalTestExpressions")
@Composable
@Preview
private fun PasswordHistoryScreenPreview() {
    DashlanePreview {
        PasswordHistoryScreenOld(
            state = PasswordHistoryState.Loaded(
                listOf(
                    PasswordHistoryEntry("Azerty12", "2 minutes ago"),
                    PasswordHistoryEntry("Dashlane12", "last month")
                )
            ),
            onRevertClick = { true },
            onCopyClick = { true },
            onBackNavigationClick = {}
        )
    }
}

@Composable
@Preview
private fun PasswordHistoryScreenLoadingPreview() {
    DashlanePreview {
        PasswordHistoryScreenOld(
            state = PasswordHistoryState.Init,
            onRevertClick = { true },
            onCopyClick = { true },
            onBackNavigationClick = {}
        )
    }
}
