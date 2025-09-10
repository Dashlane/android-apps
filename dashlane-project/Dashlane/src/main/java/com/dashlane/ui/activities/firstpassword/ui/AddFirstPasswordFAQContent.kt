package com.dashlane.ui.activities.firstpassword.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.design.component.BottomSheetActions
import com.dashlane.design.component.BottomSheetContent
import com.dashlane.design.component.tooling.DashlaneModalBottomSheetPreview
import com.dashlane.ui.common.compose.components.FaqDefaults
import com.dashlane.ui.common.compose.components.FaqItem

@Composable
internal fun AddFirstPasswordFAQContent() {
    BottomSheetContent(
        title = stringResource(R.string.faq_add_first_password_title),
        description = null,
        extraContent = {
            listOf(
                R.string.faq_add_first_password_what_if_hacked_title to
                    R.string.faq_add_first_password_what_if_hacked_detail,
                R.string.faq_add_first_password_can_dashlane_see_title to
                    R.string.faq_add_first_password_can_dashlane_see_detail,
                R.string.faq_add_first_password_how_dashlane_make_money_title to
                    R.string.faq_add_first_password_how_dashlane_make_money_detail,
                R.string.faq_add_first_password_can_i_leave_title to
                    R.string.faq_add_first_password_can_i_leave_detail,
                R.string.faq_add_first_password_is_it_more_secure_title to
                    R.string.faq_add_first_password_is_it_more_secure_detail,
            ).forEachIndexed { index, (questionRes, answerRes) ->
                if (index != 0) {
                    Spacer(modifier = Modifier.height(FaqDefaults.verticalSpacing))
                }
                FaqItem(
                    question = stringResource(questionRes),
                    answer = stringResource(answerRes),
                )
            }
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
private fun AddFirstPasswordFAQContentPreview() {
    DashlaneModalBottomSheetPreview {
        AddFirstPasswordFAQContent()
    }
}