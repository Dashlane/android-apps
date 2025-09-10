package com.dashlane.accountstatus.premiumstatus.utils

import android.content.Context
import com.dashlane.accountstatus.R
import com.dashlane.accountstatus.premiumstatus.currentTeam
import com.dashlane.accountstatus.premiumstatus.isAdvancedPlan
import com.dashlane.accountstatus.premiumstatus.isFamilyPlan
import com.dashlane.accountstatus.premiumstatus.isLegacy
import com.dashlane.accountstatus.premiumstatus.isPremium
import com.dashlane.accountstatus.premiumstatus.isPremiumPlusPlan
import com.dashlane.accountstatus.premiumstatus.isTrial
import com.dashlane.server.api.endpoints.premium.PremiumStatus

object PlansUtils {
    fun getTitle(context: Context, premiumStatus: PremiumStatus): String {
        val userPlan = getSimplePlanTitle(context, premiumStatus)

        return context.getString(R.string.plan_action_bar_title, userPlan)
    }

    fun getSimplePlanTitle(context: Context, premiumStatus: PremiumStatus): String {
        val isPremiumPlus = premiumStatus.isPremiumPlusPlan
        val isAdvanced = premiumStatus.isAdvancedPlan
        val familyUser = premiumStatus.isFamilyPlan
        val isBusiness = premiumStatus.currentTeam != null
        val userPlan = when {
            isBusiness -> R.string.plans_business
            premiumStatus.isLegacy -> R.string.plan_legacy_action_bar_title
            premiumStatus.isTrial -> R.string.plan_trial_action_bar_title
            familyUser && isPremiumPlus -> R.string.plan_premium_plus_family_action_bar_title
            isPremiumPlus -> R.string.plan_premium_plus_action_bar_title
            isAdvanced -> R.string.plans_advanced_title
            familyUser && premiumStatus.isPremium -> R.string.plan_premium_family_action_bar_title
            premiumStatus.isPremium -> R.string.plan_premium_action_bar_title
            else -> R.string.plan_free_action_bar_title
        }
        return context.getString(userPlan)
    }
}