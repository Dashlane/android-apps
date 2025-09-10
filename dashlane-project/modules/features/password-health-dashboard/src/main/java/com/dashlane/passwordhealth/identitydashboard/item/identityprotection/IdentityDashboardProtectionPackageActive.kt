package com.dashlane.passwordhealth.identitydashboard.item.identityprotection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dashlane.design.component.HorizontalDivider
import com.dashlane.design.component.LinkButton
import com.dashlane.design.component.LinkButtonDestinationType
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.design.theme.typography.LocalDashlaneTextStyle
import com.dashlane.passwordhealth.R

@Composable
internal fun IdentityDashboardProtectionPackageActive(
    onActiveProtectionLearnMoreClick: () -> Unit,
    onActiveRestorationLearnMoreClick: () -> Unit,
) {
    Column {
        HorizontalDivider()
        ActiveProtectionItem(
            modifier = Modifier.padding(horizontal = 16.dp),
            onActiveProtectionLearnMoreClick = onActiveProtectionLearnMoreClick,
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        ActiveRestorationItem(
            modifier = Modifier.padding(horizontal = 16.dp),
            onActiveRestorationLearnMoreClick = onActiveRestorationLearnMoreClick,
        )
    }
}

@Composable
private fun ActiveProtectionItem(
    onActiveProtectionLearnMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val phoneNumber = stringResource(R.string.identity_protection_phone_number)
    BaseItem(
        modifier = modifier,
        title = stringResource(R.string.identity_protection_active_title),
        subtitle = stringResource(R.string.powered_by_aig),
        infos = listOf(
            stringResource(R.string.identity_protection_active_description_1, phoneNumber),
            stringResource(R.string.identity_protection_active_description_2),
            stringResource(R.string.identity_protection_active_description_3),
        ),
        onLearnMoreClick = onActiveProtectionLearnMoreClick,
    )
}

@Composable
private fun ActiveRestorationItem(
    onActiveRestorationLearnMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val phoneNumber = stringResource(R.string.identity_restoration_phone_number)
    val dashlaneCode = stringResource(R.string.identity_restoration_dashlane_code)
    BaseItem(
        modifier = modifier,
        title = stringResource(R.string.identity_restoration_active_title),
        subtitle = stringResource(R.string.powered_by_transunion),
        infos = listOf(
            stringResource(R.string.identity_restoration_active_description_1, phoneNumber, dashlaneCode),
            stringResource(R.string.identity_restoration_active_description_2),
            stringResource(R.string.identity_restoration_active_description_3),
        ),
        onLearnMoreClick = onActiveRestorationLearnMoreClick,
    )
}

@Composable
private fun BaseItem(
    title: String,
    subtitle: String,
    infos: List<String>,
    onLearnMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            modifier = Modifier.semantics { heading() },
            text = title,
            style = DashlaneTheme.typography.titleSectionMedium,
            color = DashlaneTheme.colors.textNeutralCatchy,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = DashlaneTheme.typography.bodyHelperRegular,
            color = DashlaneTheme.colors.textNeutralQuiet,
        )
        Spacer(modifier = Modifier.height(20.dp))
        infos.forEachIndexed { index, info ->
            if (index != 0) {
                Spacer(modifier = Modifier.height(with(LocalDensity.current) { 16.sp.toDp() }))
            }
            Text(
                text = buildBulletString(
                    textMeasurer = textMeasurer,
                    style = DashlaneTheme.typography.bodyStandardRegular,
                    text = info,
                ),
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = DashlaneTheme.colors.textNeutralStandard,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        LinkButton(
            text = stringResource(R.string.identity_protection_package_learn_more),
            destinationType = LinkButtonDestinationType.EXTERNAL,
            onClick = onLearnMoreClick,
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun buildBulletString(
    text: String,
    textMeasurer: TextMeasurer = rememberTextMeasurer(),
    style: TextStyle = LocalDashlaneTextStyle.current,
): AnnotatedString {
    val bulletPoint = " • "
    val layoutResult = textMeasurer.measure(bulletPoint, style)
    val indentSize = with(LocalDensity.current) { layoutResult.size.width.toSp() }
    return buildAnnotatedString {
        withStyle(
            style = ParagraphStyle(
                textIndent = TextIndent(
                    firstLine = 0.sp,
                    restLine = indentSize,
                ),
            ),
        ) {
            append(bulletPoint)
            append(text)
        }
    }
}

@Preview
@Composable
private fun IdentityDashboardProtectionPackageActivePreview() {
    DashlanePreview {
        IdentityDashboardProtectionPackageActive(
            onActiveProtectionLearnMoreClick = {},
            onActiveRestorationLearnMoreClick = {},
        )
    }
}