package com.dashlane.premium.current.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dashlane.design.component.Icon
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.premium.current.model.CurrentPlan
import com.dashlane.premium.ui.R

@Composable
internal fun BenefitItem(
    text: String,
    moreInfoType: CurrentPlan.Benefit.MoreInfoType?,
    onClick: ((CurrentPlan.Benefit.MoreInfoType) -> Unit),
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .then(
                if (moreInfoType != null) {
                    Modifier
                        .clickable(
                            onClick = { onClick(moreInfoType) },
                            onClickLabel = stringResource(R.string.current_benefit_more_info_description),
                        )
                } else {
                    Modifier
                        .semantics(mergeDescendants = true) { }
                },
            )
            .padding(horizontal = CurrentPlanDefaults.horizontalPadding, vertical = 6.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Image(
                modifier = Modifier
                    .size(with(LocalDensity.current) { 20.sp.toDp() })
                    .alignBy { (it.measuredHeight * 0.7f).toInt() },
                painter = painterResource(R.drawable.ic_current_benefit),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .alignByBaseline(),
                text = text,
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = DashlaneTheme.colors.textNeutralStandard,
            )
            if (moreInfoType != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    modifier = Modifier
                        .size(with(LocalDensity.current) { 16.sp.toDp() })
                        .alignBy { (it.measuredHeight * 0.8125f).toInt() },
                    token = IconTokens.feedbackInfoOutlined,
                    contentDescription = null,
                    tint = DashlaneTheme.colors.textBrandStandard,
                )
            }
        }
    }
}

@Preview
@Composable
private fun BenefitItemPreview() {
    DashlanePreview {
        Column {
            BenefitItem(
                text = stringResource(R.string.current_benefit_dark_web_monitoring),
                moreInfoType = CurrentPlan.Benefit.MoreInfoType.DARK_WEB_MONITORING,
                onClick = {},
            )
            BenefitItem(
                text = """
                    Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.
                """.trimIndent(),
                moreInfoType = CurrentPlan.Benefit.MoreInfoType.DARK_WEB_MONITORING,
                onClick = {},
            )
            BenefitItem(
                text = stringResource(R.string.current_benefit_vpn),
                moreInfoType = null,
                onClick = {},
            )
        }
    }
}