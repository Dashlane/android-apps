package com.dashlane.authenticator.suggestions.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.authenticator.R
import com.dashlane.design.component.LinkButton
import com.dashlane.design.component.LinkButtonDestinationType
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.ExpandableFaqItem

@Composable
fun AuthenticatorFaq(
    goToLearnMoreAbout2FA: () -> Unit,
    goToHelpCenter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            modifier = Modifier
                .semantics { heading() }
                .fillMaxWidth(),
            text = stringResource(R.string.authenticator_faq_title),
            enforceAllCaps = true,
            style = DashlaneTheme.typography.titleSupportingSmall,
            color = DashlaneTheme.colors.textNeutralQuiet,
        )
        Spacer(
            modifier = Modifier.height(16.dp),
        )
        val expandedItemIndexState = rememberSaveable { mutableIntStateOf(-1) }
        FaqItem(
            index = 0,
            expandedItemIndexState = expandedItemIndexState,
            title = stringResource(R.string.authenticator_faq_question_1_title),
            details = stringResource(R.string.authenticator_faq_question_1_body),
            linkLabel = stringResource(R.string.authenticator_faq_question_1_button),
            onLinkClick = goToLearnMoreAbout2FA,
        )
        Spacer(
            modifier = Modifier.height(8.dp),
        )
        FaqItem(
            index = 1,
            expandedItemIndexState = expandedItemIndexState,
            title = stringResource(R.string.authenticator_faq_question_2_title),
            details = stringResource(R.string.authenticator_faq_question_2_body),
            linkLabel = stringResource(R.string.authenticator_faq_question_2_button),
            onLinkClick = goToLearnMoreAbout2FA,
        )
        Spacer(
            modifier = Modifier.height(8.dp),
        )
        FaqItem(
            index = 2,
            expandedItemIndexState = expandedItemIndexState,
            title = stringResource(R.string.authenticator_faq_question_3_title),
            details = stringResource(R.string.authenticator_faq_question_3_body),
            linkLabel = stringResource(R.string.authenticator_faq_question_3_button),
            onLinkClick = goToHelpCenter,
        )
    }
}

@Composable
private fun FaqItem(
    index: Int,
    expandedItemIndexState: MutableState<Int>,
    title: String,
    details: String,
    linkLabel: String,
    onLinkClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ExpandableFaqItem(
        modifier = modifier
            .fillMaxWidth(),
        expanded = expandedItemIndexState.value == index,
        onClick = { expandedItemIndexState.value = if (expandedItemIndexState.value == index) -1 else index },
        question = title,
        answer = details,
        linkButton = {
            LinkButton(
                text = linkLabel,
                destinationType = LinkButtonDestinationType.EXTERNAL,
                onClick = onLinkClick,
            )
        },
    )
}

@Preview
@Composable
private fun AuthenticatorFaqContentPreview() {
    DashlanePreview {
        AuthenticatorFaq(
            goToLearnMoreAbout2FA = {},
            goToHelpCenter = {},
        )
    }
}