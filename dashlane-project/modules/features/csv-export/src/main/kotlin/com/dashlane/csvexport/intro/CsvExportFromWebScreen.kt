package com.dashlane.csvexport.intro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.csvexport.R
import com.dashlane.design.component.ExpressiveIcon
import com.dashlane.design.component.ExpressiveIconSize
import com.dashlane.design.component.HtmlText
import com.dashlane.design.component.LinkButton
import com.dashlane.design.component.LinkButtonDestinationType
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.basescreen.AppBarScreenWrapper
import com.dashlane.ui.illustration.WebUrlIllustration

@Composable
fun CsvExportFromWebScreen(onBackNavigationClick: () -> Unit, onHelpCenterClick: () -> Unit) {
    AppBarScreenWrapper(
        navigationIconToken = IconTokens.arrowLeftOutlined,
        onNavigationClick = onBackNavigationClick,
        titleText = stringResource(id = R.string.csv_export_on_web_app_bar_title)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WebUrlIllustration(
                url = "dashlane.com/web",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Text(
                modifier = Modifier.padding(top = 32.dp),
                text = stringResource(id = R.string.csv_export_on_web_title),
                style = DashlaneTheme.typography.titleSectionLarge
            )
            Text(
                text = stringResource(id = R.string.csv_export_on_web_description),
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = DashlaneTheme.colors.textNeutralQuiet
            )

            Stepper(
                stepDataList = listOf(
                    StepData(IconTokens.webOutlined, stringResource(id = R.string.csv_export_on_web_step_login_on_web)),
                    StepData(IconTokens.vaultOutlined, stringResource(id = R.string.csv_export_on_web_step_open_vault)),
                    StepData(IconTokens.uploadOutlined, stringResource(id = R.string.csv_export_on_web_step_export_data)),
                )
            )

            LinkButton(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(id = R.string.csv_export_on_web_button_export_help_center),
                destinationType = LinkButtonDestinationType.EXTERNAL,
                onClick = onHelpCenterClick
            )
        }
    }
}

@Composable
internal fun Stepper(stepDataList: List<StepData>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalAlignment = Alignment.Start,
    ) {
        stepDataList.forEachIndexed { index, step ->
            if (index != 0) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    VerticalDivider(color = DashlaneTheme.colors.borderNeutralQuietIdle, thickness = 1.dp)
                }
            }
            StepItem(step)
        }
    }
}

data class StepData(val token: IconToken, val text: String)

@Composable
internal fun StepItem(step: StepData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExpressiveIcon(icon = step.token, mood = Mood.Neutral, size = ExpressiveIconSize.Small)
        HtmlText(modifier = Modifier.fillMaxWidth(), htmlText = step.text, style = DashlaneTheme.typography.bodyStandardRegular)
    }
}

@Preview
@Composable
private fun CsvExportFromWebScreenPreview() = DashlanePreview {
    CsvExportFromWebScreen(
        onBackNavigationClick = {}
    ) {}
}