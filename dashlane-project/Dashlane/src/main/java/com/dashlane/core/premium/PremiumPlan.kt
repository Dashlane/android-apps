package com.dashlane.core.premium

import org.json.JSONObject



data class PremiumPlan constructor(
    val name: String? = null,
    val type: String? = null,
    val feature: String? = null
) {

    val isEssentials: Boolean
        get() = ESSENTIALS == feature

    val isAdvanced: Boolean
        get() = ADVANCED == feature

    val isPremium: Boolean
        get() = PREMIUM == feature

    val isPremiumPlus: Boolean
        get() = PREMIUM_PLUS == feature

    constructor(jsonObject: JSONObject) : this(
        jsonObject.optString("planName"),
        jsonObject.optString("planType"),
        jsonObject.optString("planFeature")
    )

    companion object {
        const val FREE_TRIAL_30D_PLAN_NAME = "free_trial_30d"
        private const val ESSENTIALS = "essentials"
        private const val ADVANCED = "advanced"
        private const val PREMIUM = "sync"
        private const val PREMIUM_PLUS = "premiumplus"
    }
}