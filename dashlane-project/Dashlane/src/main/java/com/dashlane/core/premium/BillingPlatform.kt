package com.dashlane.core.premium

enum class BillingPlatform {
    PLAY_STORE, 
    APP_STORE, 
    PAYPAL,
    STRIPE,
    OTHER;

    companion object {
        fun fromPlanType(value: String?) = when (value) {
            "playstore_renewable" -> PLAY_STORE
            "ios_renewable" -> APP_STORE
            "paypal_renewable" -> PAYPAL
            "stripe" -> STRIPE
            else -> OTHER
        }
    }
}

fun PremiumStatus.getBillingPlatform() = BillingPlatform.fromPlanType(this.planType)