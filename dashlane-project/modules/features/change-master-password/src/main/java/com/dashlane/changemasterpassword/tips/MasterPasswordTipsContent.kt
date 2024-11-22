package com.dashlane.changemasterpassword.tips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.changemasterpassword.R
import com.dashlane.design.component.HtmlText
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview

@Suppress("LongMethod")
@Composable
fun MasterPasswordTipsContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.tips_change_password_title),
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier
                .padding(top = 32.dp, bottom = 16.dp)
        )
        
        Text(
            text = stringResource(id = R.string.tips_change_password_section1_title),
            style = DashlaneTheme.typography.titleSectionMedium,
            color = DashlaneTheme.colors.textNeutralStandard,
            modifier = Modifier
                .padding(bottom = 16.dp)
        )
        Text(
            text = stringResource(id = R.string.tips_change_password_section1_line1),
            style = DashlaneTheme.typography.bodyReducedRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
        )
        Text(
            text = stringResource(id = R.string.tips_change_password_section1_line2),
            style = DashlaneTheme.typography.bodyReducedRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
        )
        Text(
            text = stringResource(id = R.string.tips_change_password_section1_line3),
            style = DashlaneTheme.typography.bodyReducedRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
        )
        Text(
            text = stringResource(id = R.string.tips_change_password_section1_line4),
            style = DashlaneTheme.typography.bodyReducedRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
            modifier = Modifier
                .padding(bottom = 16.dp)
        )
        
        Text(
            text = stringResource(id = R.string.tips_change_password_section2_title),
            style = DashlaneTheme.typography.titleSectionMedium,
            color = DashlaneTheme.colors.textNeutralStandard,
            modifier = Modifier
                .padding(bottom = 16.dp)
        )
        Text(
            text = stringResource(id = R.string.tips_change_password_section2_desc),
            style = DashlaneTheme.typography.bodyReducedRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
            modifier = Modifier
                .padding(bottom = 16.dp)
        )
        Text(
            text = stringResource(id = R.string.tips_change_password_section2_example),
            style = DashlaneTheme.typography.bodyReducedRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
                .background(DashlaneTheme.colors.containerExpressiveNeutralQuietIdle)
                .padding(bottom = 8.dp, top = 8.dp)
        )
        
        Text(
            text = stringResource(id = R.string.tips_change_password_section3_title),
            style = DashlaneTheme.typography.titleSectionMedium,
            color = DashlaneTheme.colors.textNeutralStandard,
            modifier = Modifier
                .padding(bottom = 16.dp)
        )
        Text(
            text = stringResource(id = R.string.tips_change_password_section3_desc),
            style = DashlaneTheme.typography.bodyReducedRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
            modifier = Modifier
                .padding(bottom = 16.dp)
        )
        Text(
            text = stringResource(id = R.string.tips_change_password_section3_example),
            style = DashlaneTheme.typography.bodyReducedRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
                .background(DashlaneTheme.colors.containerExpressiveNeutralQuietIdle)
                .padding(bottom = 8.dp, top = 8.dp)
        )
        
        Text(
            text = stringResource(id = R.string.tips_change_password_section4_title),
            style = DashlaneTheme.typography.titleSectionMedium,
            color = DashlaneTheme.colors.textNeutralStandard,
            modifier = Modifier
                .padding(bottom = 16.dp)
        )
        HtmlText(
            htmlText = stringResource(id = R.string.tips_change_password_section4_desc),
            style = DashlaneTheme.typography.bodyReducedRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
            modifier = Modifier
                .padding(bottom = 16.dp)
        )
        Text(
            text = stringResource(id = R.string.tips_change_password_section4_example),
            style = DashlaneTheme.typography.bodyReducedRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
                .background(DashlaneTheme.colors.containerExpressiveNeutralQuietIdle)
                .padding(bottom = 8.dp, top = 8.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview
@Composable
private fun MasterPasswordTipsContentPreview() {
    DashlanePreview {
        MasterPasswordTipsContent()
    }
}
