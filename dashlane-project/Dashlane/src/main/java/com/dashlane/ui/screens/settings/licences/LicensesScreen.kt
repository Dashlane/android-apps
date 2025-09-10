package com.dashlane.ui.screens.settings.licences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.component.LinkButton
import com.dashlane.design.component.LinkButtonDestinationType
import com.dashlane.design.component.Text
import com.dashlane.design.component.cardBackground
import com.dashlane.design.component.tooling.plus
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.basescreen.AppBarScreenWrapper
import com.dashlane.ui.common.compose.components.components.DashlaneTopAppBar

@Composable
internal fun LicensesScreen(
    onNavigateUp: () -> Unit,
    licences: List<License>,
    openUrl: (String) -> Unit,
) {
    AppBarScreenWrapper(
        titleText = stringResource(R.string.activity_title_licenses),
        navigationAction = DashlaneTopAppBar.NavigationAction(
            iconToken = IconTokens.arrowLeftOutlined,
            contentDescription = stringResource(R.string.and_accessibility_back_button),
            onClick = onNavigateUp,
        ),
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues + PaddingValues(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(licences) { license ->
                LicenseItem(license = license, openUrl = openUrl)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LicenseItem(
    license: License,
    openUrl: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .cardBackground()
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = license.title,
            style = DashlaneTheme.typography.titleSectionMedium,
            color = DashlaneTheme.colors.textNeutralCatchy,
        )
        Text(
            text = license.description,
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
        )
        FlowRow(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LinkButton(
                text = stringResource(R.string.license_go_url),
                destinationType = LinkButtonDestinationType.EXTERNAL,
                onClick = {
                    openUrl(license.websiteUrl)
                },
            )
            LinkButton(
                text = stringResource(R.string.license_show_license),
                destinationType = LinkButtonDestinationType.EXTERNAL,
                onClick = {
                    openUrl(license.licenseUrl)
                },
            )
        }
    }
}

@Preview
@Composable
private fun LicensesScreenPreview() {
    DashlanePreview {
        LicensesScreen(
            onNavigateUp = {},
            licences = (1..5).map { i ->
                License(
                    title = "Library $i",
                    description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt…",
                    websiteUrl = "website url",
                    licenseUrl = "license url",
                )
            },
            openUrl = {},
        )
    }
}