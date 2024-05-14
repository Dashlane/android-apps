package com.dashlane.item.subview.quickaction

import com.dashlane.R
import com.dashlane.item.subview.action.NewShareAction
import com.dashlane.teamspaces.ui.TeamSpaceRestrictionNotificator
import com.dashlane.ui.screens.fragments.SharingPolicyDataProvider
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.hasAttachments

class QuickActionShare(
    summaryObject: SummaryObject,
    restrictionNotificator: TeamSpaceRestrictionNotificator
) : NewShareAction(summaryObject, restrictionNotificator) {
    override val text: Int = R.string.quick_action_share

    override val tintColorRes = R.color.text_neutral_catchy

    companion object {
        fun createActionIfShareAvailable(
            summaryObject: SummaryObject,
            sharingPolicy: SharingPolicyDataProvider,
            restrictionNotificator: TeamSpaceRestrictionNotificator
        ): QuickActionShare? {
            if (sharingPolicy.canShareItem(summaryObject)) {
                if (summaryObject is SummaryObject.SecureNote) {
                    if (!summaryObject.hasAttachments()) {
                        return QuickActionShare(summaryObject, restrictionNotificator)
                    }
                } else {
                    return QuickActionShare(summaryObject, restrictionNotificator)
                }
            }
            return null
        }
    }
}