package com.dashlane.teamspaces.ui

import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.FragmentActivity
import com.dashlane.notificator.Notificator
import com.dashlane.teamspaces.R
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.userfeatures.UserFeaturesChecker
import com.dashlane.userfeatures.canUseSecureNotes
import com.dashlane.util.inject.OptionalProvider
import javax.inject.Inject

class TeamSpaceRestrictionNotificator @Inject constructor(
    private val notificator: Notificator,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
    private val userFeaturesChecker: UserFeaturesChecker
) {
    fun runOrNotifyTeamRestriction(
        activity: FragmentActivity,
        feature: Feature,
        block: () -> Unit
    ) {
        val teamSpaceAccessor = teamSpaceAccessorProvider.get() ?: return

        val canRun: Boolean = when (feature) {
            Feature.SHARING_DISABLED -> !teamSpaceAccessor.isSharingDisabled
            Feature.AUTOLOCK -> !teamSpaceAccessor.isLockOnExitEnabled
            Feature.CRYPTO_FORCED_PAYLOAD -> teamSpaceAccessor.cryptoForcedPayload == null
            Feature.ENFORCED_2FA -> !teamSpaceAccessor.is2FAEnforced
            Feature.SECURE_NOTES_DISABLED -> userFeaturesChecker.canUseSecureNotes()
        }

        if (canRun) {
            block()
        } else {
            notifyFeatureRestricted(activity, feature)
        }
    }

    fun notifyFeatureRestricted(activity: FragmentActivity, feature: Feature) {
        showDialog(
            activity = activity,
            title = activity.getString(feature.titleRes),
            description = activity.getString(feature.descriptionRes)
        )
    }

    @VisibleForTesting
    fun showDialog(activity: FragmentActivity?, title: String?, description: String?) {
        notificator.customErrorDialogMessage(activity, title, description, false)
    }
}

enum class Feature(@StringRes val titleRes: Int, @StringRes val descriptionRes: Int) {
    SHARING_DISABLED(
        titleRes = R.string.teamspace_restrictions_sharing_title,
        descriptionRes = R.string.teamspace_restrictions_sharing_description
    ),
    AUTOLOCK(
        titleRes = R.string.teamspace_restrictions_feature_lock_setting_title,
        descriptionRes = R.string.teamspace_restrictions_feature_lock_setting_description
    ),
    SECURE_NOTES_DISABLED(
        titleRes = R.string.teamspace_restrictions_secure_notes_title,
        descriptionRes = R.string.teamspace_restrictions_secure_notes_description
    ),
    CRYPTO_FORCED_PAYLOAD(
        titleRes = R.string.teamspace_restrictions_generic_title,
        descriptionRes = R.string.teamspace_restrictions_generic_description
    ),
    ENFORCED_2FA(
        titleRes = R.string.teamspace_restrictions_generic_title,
        descriptionRes = R.string.teamspace_restrictions_generic_description
    )
}
