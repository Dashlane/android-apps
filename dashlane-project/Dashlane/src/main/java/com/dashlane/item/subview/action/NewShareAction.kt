package com.dashlane.item.subview.action

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.dashlane.R
import com.dashlane.item.subview.Action
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.manager.TeamspaceManager
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.vault.summary.SummaryObject

open class NewShareAction(
    private val summaryObject: SummaryObject,
    private val teamspaceManager: TeamspaceManager?
) : Action {
    override val text: Int = R.string.share_from_services_menu_title

    override val icon: Int = R.drawable.ic_share

    override val tintColorRes: Int? = null

    override fun onClickAction(activity: AppCompatActivity) {
        proceedItemIfTeamspaceAllows(
            activity,
            object : TeamspaceAccessor.FeatureCall {
                override fun startFeature() {
                    summaryObject.showSharing(NEW_SHARE_REQUEST_CODE, activity, true)
                }
            }
        )
    }

    private fun proceedItemIfTeamspaceAllows(
        activity: FragmentActivity,
        callback: TeamspaceAccessor.FeatureCall
    ) {
        teamspaceManager?.startFeatureOrNotify(activity, Teamspace.Feature.SHARING_DISABLED, callback)
    }

    companion object {
        const val NEW_SHARE_REQUEST_CODE = 6243
    }
}