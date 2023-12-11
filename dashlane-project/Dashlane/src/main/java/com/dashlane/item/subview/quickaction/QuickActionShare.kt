package com.dashlane.item.subview.quickaction

import com.dashlane.R
import com.dashlane.item.subview.action.NewShareAction
import com.dashlane.teamspaces.manager.TeamspaceManager
import com.dashlane.ui.screens.fragments.SharingPolicyDataProvider
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.hasAttachments

class QuickActionShare(summaryObject: SummaryObject, teamspaceManager: TeamspaceManager?) :
    NewShareAction(summaryObject, teamspaceManager) {
    override val text: Int = R.string.quick_action_share

    override val tintColorRes = R.color.text_neutral_catchy

    companion object {
        fun createActionIfShareAvailable(
            summaryObject: SummaryObject,
            sharingPolicy: SharingPolicyDataProvider,
            teamspaceManager: TeamspaceManager?
        ): QuickActionShare? {
            if (sharingPolicy.canShareItem(summaryObject)) {
                if (summaryObject is SummaryObject.SecureNote) {
                    if (!summaryObject.hasAttachments()) {
                        return QuickActionShare(summaryObject, teamspaceManager)
                    }
                } else {
                    return QuickActionShare(summaryObject, teamspaceManager)
                }
            }
            return null
        }
    }
}