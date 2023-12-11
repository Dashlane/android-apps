package com.dashlane.teamspaces.manager

import androidx.annotation.VisibleForTesting
import androidx.fragment.app.FragmentActivity
import com.dashlane.R
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.ui.widgets.Notificator
import javax.inject.Inject

class TeamspaceRestrictionNotificator @Inject constructor(private val notificator: Notificator) {
    fun notifyFeatureRestricted(activity: FragmentActivity, @Teamspace.Feature feature: String?) {
        val (titleResId, descriptionResId) = when (feature) {
            Teamspace.Feature.EMERGENCY_DISABLED -> {
                R.string.teamspace_restrictions_emergency_title to
                    R.string.teamspace_restrictions_emergency_description
            }

            Teamspace.Feature.SHARING_DISABLED -> {
                R.string.teamspace_restrictions_sharing_title to
                    R.string.teamspace_restrictions_sharing_description
            }

            Teamspace.Feature.SECURE_NOTES_DISABLED -> {
                R.string.teamspace_restrictions_secure_notes_title to
                    R.string.teamspace_restrictions_secure_notes_description
            }

            Teamspace.Feature.AUTOLOCK -> {
                R.string.teamspace_restrictions_feature_lock_setting_title to
                    R.string.teamspace_restrictions_feature_lock_setting_description
            }

            else -> {
                R.string.teamspace_restrictions_generic_title to
                    R.string.teamspace_restrictions_generic_description
            }
        }

        showDialog(
            activity = activity,
            title = activity.getString(titleResId),
            description = activity.getString(descriptionResId)
        )
    }

    @VisibleForTesting
    fun showDialog(activity: FragmentActivity?, title: String?, description: String?) {
        notificator.customErrorDialogMessage(activity, title, description, false)
    }

    @VisibleForTesting
    fun getUsageLogType(@Teamspace.Feature feature: String?): String {
        return when (feature) {
            Teamspace.Feature.EMERGENCY_DISABLED -> "emergencyBlockedCompanySpace"
            Teamspace.Feature.SHARING_DISABLED -> "sharingBlockedCompanySpace"
            Teamspace.Feature.AUTOLOCK -> "autolockForceCompanyPolicy"
            else -> "UnknownFeatureBlockedCompanySpace"
        }
    }
}
