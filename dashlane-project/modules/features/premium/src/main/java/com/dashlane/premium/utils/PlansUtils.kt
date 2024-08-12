package com.dashlane.premium.utils

import android.content.Context
import com.dashlane.accountstatus.premiumstatus.isAdvancedPlan
import com.dashlane.accountstatus.premiumstatus.isFamilyPlan
import com.dashlane.accountstatus.premiumstatus.isLegacy
import com.dashlane.accountstatus.premiumstatus.isPremium
import com.dashlane.accountstatus.premiumstatus.isPremiumPlusPlan
import com.dashlane.accountstatus.premiumstatus.isTrial
import com.dashlane.premium.R
import com.dashlane.server.api.endpoints.premium.PremiumStatus

object PlansUtils {

    fun getTitle(context: Context, premiumStatus: PremiumStatus): String {
        val isPremiumPlus = premiumStatus.isPremiumPlusPlan
        val isAdvanced = premiumStatus.isAdvancedPlan
        val familyUser = premiumStatus.isFamilyPlan
        val userPlan = when {
            premiumStatus.isLegacy -> R.string.plan_legacy_action_bar_title
            premiumStatus.isTrial -> R.string.plan_trial_action_bar_title
            familyUser && isPremiumPlus -> R.string.plan_premium_plus_family_action_bar_title
            isPremiumPlus -> R.string.plan_premium_plus_action_bar_title
            isAdvanced -> R.string.plans_advanced_title
            familyUser && premiumStatus.isPremium -> R.string.plan_premium_family_action_bar_title
            premiumStatus.isPremium -> R.string.plan_premium_action_bar_title
            else -> R.string.plan_free_action_bar_title
        }

        return context.getString(R.string.plan_action_bar_title, context.getString(userPlan))
    }
}