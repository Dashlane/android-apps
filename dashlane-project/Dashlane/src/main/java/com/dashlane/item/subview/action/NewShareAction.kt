package com.dashlane.item.subview.action

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.dashlane.R
import com.dashlane.item.subview.Action
import com.dashlane.teamspaces.ui.Feature
import com.dashlane.teamspaces.ui.TeamSpaceRestrictionNotificator
import com.dashlane.util.getBaseActivity
import com.dashlane.vault.summary.SummaryObject

open class NewShareAction(
    private val summaryObject: SummaryObject,
    private val restrictionNotificator: TeamSpaceRestrictionNotificator
) : Action {
    override val text: Int = R.string.share_from_services_menu_title

    override val icon: Int = R.drawable.ic_share

    override val tintColorRes: Int? = null

    override fun onClickAction(activity: AppCompatActivity) {
        (activity.getBaseActivity() as? FragmentActivity)?.let {
            restrictionNotificator.runOrNotifyTeamRestriction(
                activity = it,
                feature = Feature.SHARING_DISABLED
            ) {
                summaryObject.showSharing(NEW_SHARE_REQUEST_CODE, activity, true)
            }
        }
    }

    companion object {
        const val NEW_SHARE_REQUEST_CODE = 6243
    }
}