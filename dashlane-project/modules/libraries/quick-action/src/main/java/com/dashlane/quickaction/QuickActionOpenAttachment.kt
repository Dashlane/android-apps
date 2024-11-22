package com.dashlane.quickaction

import androidx.appcompat.app.AppCompatActivity
import com.dashlane.featureflipping.FeatureFlip
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.navigation.Navigator
import com.dashlane.securefile.extensions.hasAttachments
import com.dashlane.ui.action.Action
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.utils.attachmentsAllowed

class QuickActionOpenAttachment(
    hasAttachments: Boolean,
    private val summaryObject: SummaryObject,
    private val navigator: Navigator,
) : Action {

    override val text: Int = if (!hasAttachments) {
        R.string.quick_action_add_attachment
    } else {
        R.string.quick_action_open_attachment
    }

    override val icon: Int = R.drawable.ic_attachment

    override val tintColorRes = R.color.text_neutral_catchy

    override fun onClickAction(activity: AppCompatActivity) {
        navigator.showAttachments(
            id = summaryObject.id,
            xmlObjectName = summaryObject.syncObjectType.xmlObjectName,
            attachments = summaryObject.attachments
        )
    }

    companion object {
        fun createAttachmentsAction(
            summaryObject: SummaryObject,
            userFeaturesChecker: UserFeaturesChecker,
            navigator: Navigator,
            isAccountFrozen: Boolean = false,
            hasCollections: Boolean = false,
        ): QuickActionOpenAttachment? {
            val attachmentsAllowed = summaryObject.attachmentsAllowed(
                attachmentAllItems = userFeaturesChecker.has(FeatureFlip.ATTACHMENT_ALL_ITEMS),
                isAccountFrozen = isAccountFrozen,
                hasCollections = hasCollections
            )

            if (attachmentsAllowed) {
                return QuickActionOpenAttachment(
                    hasAttachments = summaryObject.hasAttachments(),
                    summaryObject = summaryObject,
                    navigator = navigator
                )
            }
            return null
        }
    }
}