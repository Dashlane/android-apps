package com.dashlane.importlogins.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.ExpressiveIcon
import com.dashlane.design.component.ExpressiveIconSize
import com.dashlane.design.component.ListItem
import com.dashlane.design.component.Text
import com.dashlane.design.component.ThumbnailType
import com.dashlane.design.component.cardBackground
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.DecorativeColor
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.BooleanProvider
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.importlogins.ImportLoginsEvent
import com.dashlane.importlogins.ImportLoginsState.SideEffect
import com.dashlane.importlogins.ImportLoginsViewModel
import com.dashlane.importlogins.R
import com.dashlane.ui.common.compose.components.screentemplate.BoxColumn
import com.dashlane.ui.thumbnail.ThumbnailDomainIcon

private const val DASHLANE_URL = "https://dashlane.com"

@Composable
internal fun ImportLoginsScreen(
    viewModel: ImportLoginsViewModel,
    goToCsvFileImportIntro: () -> Unit,
    goToDashFileImportIntro: () -> Unit,
    goToAddLoginManually: () -> Unit,
) {
    LaunchedEffect(viewModel) {
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                SideEffect.GoToCsvFileImportIntro -> goToCsvFileImportIntro()
                SideEffect.GoToDashFileImportIntro -> goToDashFileImportIntro()
                SideEffect.GoToAddSingleLogin -> goToAddLoginManually()
            }
        }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsState()
    ImportLoginsContent(
        showAddManually = uiState.showAddLoginManually,
        onCsvFileImportClick = { viewModel.sendEvent(ImportLoginsEvent.GoToCsvFileImportClick) },
        onDashFileImportClick = { viewModel.sendEvent(ImportLoginsEvent.GoToDashFileImportClick) },
        onAddManuallyClick = { viewModel.sendEvent(ImportLoginsEvent.GoToAddSingleLoginClick) },
    )
}

@Suppress("LongMethod")
@Composable
private fun ImportLoginsContent(
    showAddManually: Boolean,
    onCsvFileImportClick: () -> Unit,
    onDashFileImportClick: () -> Unit,
    onAddManuallyClick: () -> Unit,
) {
    BoxColumn(
        contentPadding = PaddingValues(16.dp),
    ) {
        ExpressiveIcon(
            icon = IconTokens.importOutlined,
            mood = Mood.Brand,
            size = ExpressiveIconSize.XLarge,
        )
        Spacer(
            modifier = Modifier.height(24.dp),
        )
        Text(
            modifier = Modifier.semantics { heading() },
            text = stringResource(R.string.import_logins_title),
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
        )
        Spacer(
            modifier = Modifier.height(24.dp),
        )
        ImportLoginsCard(
            header = stringResource(R.string.import_logins_section1),
        ) {
            ListItem(
                thumbnailType = ThumbnailType.VaultItem.OtherIcon(
                    token = IconTokens.csvOutlined,
                    color = DecorativeColor.GREY,
                ),
                title = stringResource(R.string.import_logins_from_csv_file_line1),
                description = stringResource(R.string.import_logins_from_csv_file_line2),
                onClick = onCsvFileImportClick,
            )
        }
        Spacer(
            modifier = Modifier.height(24.dp),
        )
        ImportLoginsCard(
            header = stringResource(R.string.import_logins_section2),
        ) {
            ListItem(
                thumbnail = {
                    ThumbnailDomainIcon(
                        urlDomain = DASHLANE_URL,
                    )
                },
                title = stringResource(R.string.import_logins_from_dash_file_line1),
                description = stringResource(R.string.import_logins_from_dash_file_line2),
                onClick = onDashFileImportClick,
            )
            if (showAddManually) {
                ListItem(
                    thumbnailType = ThumbnailType.Icon(
                        token = IconTokens.actionAddOutlined,
                    ),
                    title = stringResource(R.string.import_logins_add_single_login_manually),
                    description = null,
                    onClick = onAddManuallyClick,
                )
            }
        }
    }
}

@Composable
private fun ImportLoginsCard(
    header: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .cardBackground()
            .padding(vertical = 16.dp, horizontal = 8.dp),
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .semantics { heading() },
            text = header,
            style = DashlaneTheme.typography.titleSupportingSmall,
            enforceAllCaps = true,
            color = DashlaneTheme.colors.textNeutralQuiet,
        )
        Spacer(
            modifier = Modifier.height(8.dp),
        )
        content()
    }
}

@Preview
@Composable
private fun ImportLoginsContentPreview(
    @PreviewParameter(BooleanProvider::class) showAddManually: Boolean,
) {
    DashlanePreview {
        ImportLoginsContent(
            showAddManually = showAddManually,
            onCsvFileImportClick = {},
            onDashFileImportClick = {},
            onAddManuallyClick = {},
        )
    }
}