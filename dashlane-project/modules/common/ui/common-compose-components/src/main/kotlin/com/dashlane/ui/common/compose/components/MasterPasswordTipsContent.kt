package com.dashlane.ui.common.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.BottomSheetActions
import com.dashlane.design.component.BottomSheetContent
import com.dashlane.design.component.BottomSheetDefaults
import com.dashlane.design.component.HtmlText
import com.dashlane.design.component.Text
import com.dashlane.design.component.tooling.DashlaneModalBottomSheetPreview
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.typography.withSpecialCharsHighlight
import com.dashlane.ui.commoncomposecomponents.R

@Composable
fun MasterPasswordTipsContent(
    modifier: Modifier = Modifier,
) {
    BottomSheetContent(
        modifier = modifier,
        title = stringResource(id = R.string.tips_change_password_title),
        description = null,
        extraContent = {
            
            PasswordTipItem(
                title = stringResource(id = R.string.tips_change_password_section1_title),
                description = listOf(
                    stringResource(R.string.tips_change_password_section1_line1),
                    stringResource(R.string.tips_change_password_section1_line2),
                    stringResource(R.string.tips_change_password_section1_line3),
                    stringResource(R.string.tips_change_password_section1_line4),
                ).joinToString(separator = "\n"),
                example = null,
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            PasswordTipItem(
                title = stringResource(id = R.string.tips_change_password_section2_title),
                description = stringResource(id = R.string.tips_change_password_section2_desc),
                example = stringResource(id = R.string.tips_change_password_section2_example),
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            PasswordTipItem(
                title = stringResource(id = R.string.tips_change_password_section3_title),
                description = stringResource(id = R.string.tips_change_password_section3_desc),
                example = stringResource(id = R.string.tips_change_password_section3_example),
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            PasswordTipItem(
                title = stringResource(id = R.string.tips_change_password_section4_title),
                description = stringResource(id = R.string.tips_change_password_section4_desc),
                useHtml = true,
                example = stringResource(id = R.string.tips_change_password_section4_example),
            )
        },
        actions = BottomSheetActions(
            primaryButtonText = null,
            onPrimaryButtonClick = null,
            secondaryButtonText = null,
            onSecondaryButtonClick = null,
        ),
    )
}

@Composable
private fun PasswordTipItem(
    title: String,
    description: String,
    useHtml: Boolean = false,
    example: String?,
) {
    Text(
        text = title,
        style = DashlaneTheme.typography.titleBlockMedium,
        color = DashlaneTheme.colors.textNeutralCatchy,
    )
    Spacer(modifier = Modifier.height(8.dp))
    if (useHtml) {
        HtmlText(
            htmlText = description,
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
        )
    } else {
        Text(
            text = description,
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
        )
    }
    if (example != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .background(DashlaneTheme.colors.containerExpressiveNeutralQuietIdle)
                .padding(bottom = 8.dp, top = 8.dp),
            text = example.withSpecialCharsHighlight(),
            style = DashlaneTheme.typography.bodyStandardMonospace,
            color = DashlaneTheme.colors.textNeutralStandard,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun MasterPasswordTipsContentPreview() {
    DashlaneModalBottomSheetPreview {
        MasterPasswordTipsContent(
            modifier = Modifier.background(BottomSheetDefaults.background),
        )
    }
}