package com.dashlane.item.passwordhistory

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.item.passwordhistory.PasswordHistoryViewModel.PasswordHistoryState
import com.dashlane.ui.common.compose.components.basescreen.AppBarScreenWrapper

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
        PasswordHistoryScreen(
            modifier = modifier,
            state = state,
            onRevertClick = onRevertClick,
            onCopyClick = onCopyClick,
        )
    }
}

@SuppressLint("InternalTestExpressions")
@Composable
@Preview
private fun PasswordHistoryScreenOldPreview() {
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
private fun PasswordHistoryScreenOldLoadingPreview() {
    DashlanePreview {
        PasswordHistoryScreenOld(
            state = PasswordHistoryState.Init,
            onRevertClick = { true },
            onCopyClick = { true },
            onBackNavigationClick = {}
        )
    }
}
