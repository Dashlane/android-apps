package com.dashlane.teamspaces.manager;

import com.dashlane.R;
import com.dashlane.teamspaces.model.Teamspace;
import com.dashlane.ui.widgets.Notificator;
import com.dashlane.useractivity.log.usage.UsageLogRepository;
import com.dashlane.useractivity.log.inject.UserActivityComponent;
import com.dashlane.useractivity.log.usage.UsageLogCode33;

import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.FragmentActivity;



public class TeamspaceRestrictionNotificator {

    public void notifyFeatureRestricted(FragmentActivity activity, @Teamspace.Feature String feature) {
        UsageLogRepository usageLogRepository =
                UserActivityComponent.Companion.invoke(activity).getCurrentSessionUsageLogRepository();
        notifyFeatureRestricted(activity, usageLogRepository, feature);
    }

    @VisibleForTesting
    void notifyFeatureRestricted(FragmentActivity activity,
                                 UsageLogRepository usageLogRepository,
                                 @Teamspace.Feature String feature) {
        int titleResId;
        int descriptionResId;
        switch (feature) {
            case Teamspace.Feature.EMERGENCY_DISABLED:
                titleResId = R.string.teamspace_restrictions_emergency_title;
                descriptionResId = R.string.teamspace_restrictions_emergency_description;
                break;
            case Teamspace.Feature.SHARING_DISABLED:
                titleResId = R.string.teamspace_restrictions_sharing_title;
                descriptionResId = R.string.teamspace_restrictions_sharing_description;
                break;
            case Teamspace.Feature.SECURE_NOTES_DISABLED:
                titleResId = R.string.teamspace_restrictions_secure_notes_title;
                descriptionResId = R.string.teamspace_restrictions_secure_notes_description;
                break;
            case Teamspace.Feature.AUTOLOCK:
                titleResId = R.string.teamspace_restrictions_feature_lock_setting_title;
                descriptionResId = R.string.teamspace_restrictions_feature_lock_setting_description;
                break;
            default:
                titleResId = R.string.teamspace_restrictions_generic_title;
                descriptionResId = R.string.teamspace_restrictions_generic_description;
                break;
        }
        String usageLogType = getUsageLogType(feature);
        if (usageLogRepository != null) {
            usageLogRepository.enqueue(TeamspaceRestrictionLogHelper.getLog(usageLogType, "display"), false);
        }

        String title = activity.getString(titleResId);
        String description = activity.getString(descriptionResId);
        showDialog(activity, title, description, usageLogRepository,
                   TeamspaceRestrictionLogHelper.getLog(usageLogType, "ok"));
    }

    @VisibleForTesting
    void showDialog(FragmentActivity activity, String title, String description,
                    UsageLogRepository usageLogRepository,
                    UsageLogCode33 positiveButtonUsageLog) {
        Notificator.customErrorDialogMessage(activity, title, description, false,
                                             usageLogRepository,
                                             positiveButtonUsageLog);
    }

    @VisibleForTesting
    String getUsageLogType(@Teamspace.Feature String feature) {
        switch (feature) {
            case Teamspace.Feature.EMERGENCY_DISABLED:
                return "emergencyBlockedCompanySpace";
            case Teamspace.Feature.SHARING_DISABLED:
                return "sharingBlockedCompanySpace";
            case Teamspace.Feature.AUTOLOCK:
                return "autolockForceCompanyPolicy";
            default:
                return "UnknownFeatureBlockedCompanySpace";
        }
    }
}
