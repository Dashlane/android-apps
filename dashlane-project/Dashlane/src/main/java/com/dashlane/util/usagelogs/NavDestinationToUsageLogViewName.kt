package com.dashlane.util.usagelogs

import androidx.navigation.NavDestination
import com.dashlane.R
import com.dashlane.useractivity.log.usage.UsageLogConstant



class NavDestinationToUsageLogViewName {
    fun convert(destination: NavDestination): String? {
        return if (destinationIsBlacklisted(destination)) {
            null
        } else getViewName(destination)
    }

    fun destinationIsBlacklisted(destination: NavDestination): Boolean {
        return destination.id == R.id.nav_share_user_groups
    }

    private fun getViewName(destination: NavDestination) = when (destination.id) {
        R.id.nav_home -> "ul-${UsageLogConstant.HomePageSubtype.dashboard}"
        R.id.nav_personal_plan -> "ul-${UsageLogConstant.HomePageSubtype.personalPlan}"
        R.id.nav_password_generator -> "ul-${UsageLogConstant.ViewType.passwordGenerator}"
        R.id.nav_sharing_center -> "ul-${UsageLogConstant.HomePageSubtype.sharingList}"
        R.id.nav_settings -> "ul-${UsageLogConstant.ViewType.settings}"
        R.id.nav_manage_devices -> "ul-${UsageLogConstant.ViewType.devices}"
        R.id.nav_identity_dashboard -> "ul-${UsageLogConstant.HomePageSubtype.identityDashboard}"
        R.id.nav_dark_web_monitoring -> "ul-${UsageLogConstant.HomePageSubtype.darkWebMonitoringDashboard}"
        R.id.nav_password_analysis -> "ul-${UsageLogConstant.HomePageSubtype.passwordAnalysis}"
        R.id.nav_credential_add_step_1 -> "ul-${UsageLogConstant.ViewType.credentialStep1}"
        R.id.nav_share_people_selection -> "ul-${UsageLogConstant.ViewType.sharingPeople}"
        R.id.nav_search -> "ul-${UsageLogConstant.ViewType.search}"
        R.id.nav_share_users_for_items -> "ul-${UsageLogConstant.ViewType.sharedWithAccounts}"
        R.id.nav_share_items_for_users -> "ul-${UsageLogConstant.ViewType.sharedWithItems}"
        R.id.nav_new_share -> "ul-${UsageLogConstant.ViewType.sharingItem}"
        R.id.nav_vpn_third_party -> "ul-${UsageLogConstant.ViewType.vpn}"
        R.id.nav_notif_center -> "ul-${UsageLogConstant.ViewType.ACTION_ITEM_CENTER}"
        R.id.nav_offers,
        R.id.nav_offers_overview,
        R.id.nav_offers_details -> "ul-${UsageLogConstant.ViewType.goPremium}"
        R.id.nav_in_app_login -> "InAppLogin"
        R.id.nav_breach_alert_detail -> "breachAlertDetail"
        else -> null
    }
}