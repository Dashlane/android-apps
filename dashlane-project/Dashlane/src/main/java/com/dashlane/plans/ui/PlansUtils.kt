package com.dashlane.plans.ui

import android.content.Context
import com.dashlane.R
import com.dashlane.core.premium.PremiumStatus

object PlansUtils {

    

    fun getTitle(context: Context, premiumStatus: PremiumStatus): String {
        val isPremiumPlus = premiumStatus.premiumPlan.isPremiumPlus
        val isAdvanced = premiumStatus.premiumPlan.isAdvanced
        val isEssentials = premiumStatus.premiumPlan.isEssentials
        val familyUser = premiumStatus.isFamilyUser
        val userPlan = when {
            premiumStatus.isLegacy -> R.string.plan_legacy_action_bar_title
            premiumStatus.isTrial -> R.string.plan_trial_action_bar_title
            familyUser && isPremiumPlus -> R.string.plan_premium_plus_family_action_bar_title
            isPremiumPlus -> R.string.plan_premium_plus_action_bar_title
            isAdvanced -> R.string.plans_advanced_title
            isEssentials -> R.string.plan_essentials_action_bar_title
            familyUser && premiumStatus.isPremium -> R.string.plan_premium_family_action_bar_title
            premiumStatus.isPremium -> R.string.plan_premium_action_bar_title
            else -> R.string.plan_free_action_bar_title
        }

        return context.getString(R.string.plan_action_bar_title, context.getString(userPlan))
    }
}