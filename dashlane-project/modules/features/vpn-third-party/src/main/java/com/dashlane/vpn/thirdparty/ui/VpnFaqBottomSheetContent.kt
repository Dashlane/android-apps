package com.dashlane.vpn.thirdparty.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.design.component.BottomSheetActions
import com.dashlane.design.component.BottomSheetContent
import com.dashlane.design.component.LinkButton
import com.dashlane.design.component.LinkButtonDestinationType
import com.dashlane.design.component.Text
import com.dashlane.design.component.tooling.DashlaneModalBottomSheetPreview
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.common.compose.components.FaqDefaults
import com.dashlane.ui.common.compose.components.FaqItem
import com.dashlane.vpn.thirdparty.R

@Composable
internal fun VpnFaqBottomSheetContent(
    onVpnIntroHelpCenterArticleClick: () -> Unit,
    onVpnFaqHelpCenterArticleClick: () -> Unit,
) {
    BottomSheetContent(
        title = stringResource(R.string.vpn_third_party_faq_title),
        decoration = null,
        description = null,
        extraContent = {
            FaqItem(
                question = stringResource(R.string.vpn_third_party_faq_question_1_title),
                answer = stringResource(R.string.vpn_third_party_faq_question_1_body),
            )
            LinkButton(
                text = stringResource(R.string.vpn_third_party_faq_question_button),
                destinationType = LinkButtonDestinationType.EXTERNAL,
                onClick = onVpnIntroHelpCenterArticleClick,
            )
            Spacer(modifier = Modifier.height(FaqDefaults.verticalSpacing))
            FaqItem(
                question = stringResource(R.string.vpn_third_party_faq_question_2_title),
                answer = stringResource(R.string.vpn_third_party_faq_question_2_body),
            )
            Spacer(modifier = Modifier.height(FaqDefaults.verticalSpacing))
            Text(
                modifier = Modifier.semantics { heading() },
                text = stringResource(R.string.vpn_third_party_faq_question_3_title),
                color = DashlaneTheme.colors.textNeutralCatchy,
                style = DashlaneTheme.typography.titleBlockMedium,
            )
            LinkButton(
                text = stringResource(R.string.vpn_third_party_faq_question_3_link),
                destinationType = LinkButtonDestinationType.EXTERNAL,
                onClick = onVpnFaqHelpCenterArticleClick,
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

@Preview
@Composable
private fun VpnFaqBottomSheetContentPreview() {
    DashlaneModalBottomSheetPreview {
        VpnFaqBottomSheetContent(
            onVpnIntroHelpCenterArticleClick = {},
            onVpnFaqHelpCenterArticleClick = {},
        )
    }
}