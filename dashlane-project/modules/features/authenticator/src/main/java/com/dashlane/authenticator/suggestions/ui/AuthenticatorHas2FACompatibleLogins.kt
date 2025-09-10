package com.dashlane.authenticator.suggestions.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.authenticator.R
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsUiState.HasLogins.CredentialItem
import com.dashlane.authenticator.suggestions.ui.AuthenticatorHas2FACompatibleLoginsDefaults.DEFAULT_ITEMS_SHOWN
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.ListItem
import com.dashlane.design.component.Text
import com.dashlane.design.component.ThumbnailSize
import com.dashlane.design.component.cardBackground
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.thumbnail.ThumbnailDomainIcon

@Suppress("LongMethod")
internal fun LazyListScope.authenticatorHas2FACompatibleLogins(
    logins: List<CredentialItem>,
    maxItemsShown: Int,
    onSetupOtp: (CredentialItem) -> Unit,
    onSeeAllClick: () -> Unit,
    onSeeLessClick: () -> Unit,
    onAdd2faTokenClick: () -> Unit,
) {
    item(key = "has 2FA compatible logins title") {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { heading() }
                .cardBackground(isTop = true, isBottom = false)
                .padding(top = 16.dp, start = 16.dp, bottom = 8.dp, end = 16.dp),
            text = stringResource(R.string.authenticator_suggestions_title),
            style = DashlaneTheme.typography.titleBlockMedium,
            color = DashlaneTheme.colors.textNeutralCatchy,
        )
    }
    logins
        .take(maxItemsShown)
        .forEach { login ->
            item(key = login.id) {
                CompatibleLogin(
                    modifier = Modifier
                        .fillMaxWidth()
                        .cardBackground(isTop = false, isBottom = false),
                    login = login,
                    onSetupOtp = onSetupOtp,
                )
            }
        }
    item(key = "has 2FA compatible logins card bottom") {
        if (logins.size <= DEFAULT_ITEMS_SHOWN) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .cardBackground(isTop = false, isBottom = true),
            )
        } else {
            SeeMoreOrLessButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .cardBackground(isTop = false, isBottom = true)
                    .padding(2.dp),
                collapsed = maxItemsShown <= DEFAULT_ITEMS_SHOWN,
                onSeeAllClick = onSeeAllClick,
                onSeeLessClick = onSeeLessClick,
            )
        }
    }
    item(key = "add 2FA token action") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 2.dp),
        ) {
            ButtonMedium(
                modifier = Modifier
                    .align(Alignment.TopCenter),
                layout = ButtonLayout.TextOnly(text = stringResource(R.string.authenticator_dashboard_setup_button)),
                onClick = onAdd2faTokenClick,
                mood = Mood.Brand,
                intensity = Intensity.Quiet,
            )
        }
    }
}

@Composable
fun CompatibleLogin(
    login: CredentialItem,
    onSetupOtp: (CredentialItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
    ) {
        ListItem(
            modifier = Modifier.fillMaxWidth(),
            thumbnail = {
                ThumbnailDomainIcon(
                    urlDomain = login.domain,
                    size = ThumbnailSize.Medium,
                )
            },
            title = login.title ?: "",
            description = login.username ?: "",
            onClick = { onSetupOtp(login) },
        )
    }
}

@Composable
private fun SeeMoreOrLessButton(
    collapsed: Boolean,
    onSeeAllClick: () -> Unit,
    onSeeLessClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopEnd,
    ) {
        if (collapsed) {
            ButtonMedium(
                layout = ButtonLayout.TextOnly(stringResource(R.string.authenticator_see_all)),
                onClick = onSeeAllClick,
                mood = Mood.Brand,
                intensity = Intensity.Supershy,
            )
        } else {
            ButtonMedium(
                layout = ButtonLayout.TextOnly(stringResource(R.string.authenticator_see_less)),
                onClick = onSeeLessClick,
                mood = Mood.Brand,
                intensity = Intensity.Supershy,
            )
        }
    }
}

object AuthenticatorHas2FACompatibleLoginsDefaults {
    const val DEFAULT_ITEMS_SHOWN = 5
}

@Preview
@Composable
private fun AuthenticatorHas2FACompatibleLoginsWith3LoginsPreview() {
    DashlanePreview {
        CompositionLocalProvider(
            LocalInspectionMode provides true,
        ) {
            LazyColumn {
                authenticatorHas2FACompatibleLogins(
                    logins = List(3) { i -> buildCredentialItemForPreview(i + 1) },
                    maxItemsShown = DEFAULT_ITEMS_SHOWN,
                    onSetupOtp = {},
                    onSeeAllClick = {},
                    onSeeLessClick = {},
                    onAdd2faTokenClick = {},
                )
            }
        }
    }
}

@Preview
@Composable
private fun AuthenticatorHas2FACompatibleLoginsWith6LoginsPreview() {
    DashlanePreview {
        CompositionLocalProvider(
            LocalInspectionMode provides true,
        ) {
            var nbItemsShown by remember { mutableIntStateOf(DEFAULT_ITEMS_SHOWN) }
            LazyColumn {
                authenticatorHas2FACompatibleLogins(
                    logins = List(7) { i -> buildCredentialItemForPreview(i + 1) },
                    onSetupOtp = {},
                    onSeeAllClick = { nbItemsShown = 6 },
                    onSeeLessClick = { nbItemsShown = DEFAULT_ITEMS_SHOWN },
                    onAdd2faTokenClick = {},
                    maxItemsShown = nbItemsShown,
                )
            }
        }
    }
}

internal fun buildCredentialItemForPreview(index: Int) = CredentialItem(
    id = "login $index",
    title = "sample $index",
    domain = null,
    username = "randomemail@provider.com",
)