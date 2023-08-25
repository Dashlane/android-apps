package com.dashlane.item.subview.quickaction

import com.dashlane.R
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.item.subview.action.NewShareAction
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.hasAttachments

class QuickActionShare(summaryObject: SummaryObject) : NewShareAction(summaryObject) {
    override val text: Int = R.string.quick_action_share

    override val tintColorRes = R.color.text_neutral_catchy

    companion object {
        fun createActionIfShareAvailable(summaryObject: SummaryObject): QuickActionShare? {
            if (SingletonProvider.getSharingPolicyDataProvider().canShareItem(summaryObject)) {
                if (summaryObject is SummaryObject.SecureNote) {
                    if (!summaryObject.hasAttachments()) {
                        return QuickActionShare(summaryObject)
                    }
                } else {
                    return QuickActionShare(summaryObject)
                }
            }
            return null
        }
    }
}