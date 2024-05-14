package com.dashlane.accountstatus.subscription

import com.dashlane.server.api.endpoints.premium.SubscriptionInfo


val SubscriptionInfo.autoRenewTrigger: SubscriptionInfo.B2cSubscription.AutoRenewInfo.Trigger?
    get() = b2cSubscription.autoRenewInfo.trigger

val SubscriptionInfo.autoRenewPeriodicity: SubscriptionInfo.B2cSubscription.AutoRenewInfo.Periodicity?
    get() = b2cSubscription.autoRenewInfo.periodicity

val SubscriptionInfo.willAutoRenew: Boolean
    get() = b2cSubscription.autoRenewInfo.theory

val SubscriptionInfo.isRenewAutomatic: Boolean
    get() = b2cSubscription.autoRenewInfo.trigger == SubscriptionInfo.B2cSubscription.AutoRenewInfo.Trigger.AUTOMATIC

val SubscriptionInfo.isRenewManual: Boolean
    get() = b2cSubscription.autoRenewInfo.trigger == SubscriptionInfo.B2cSubscription.AutoRenewInfo.Trigger.MANUAL
