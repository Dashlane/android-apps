package com.dashlane.premium.offer.common

import com.dashlane.premium.offer.common.model.OfferType
import com.dashlane.premium.offer.common.model.ProductPeriodicity
import com.dashlane.premium.offer.common.model.UserBenefitStatus
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLogCode127
import com.dashlane.useractivity.log.usage.UsageLogRepository

class LegacyOffersLogger(
    premiumStatusManager: FormattedPremiumStatusManager,
    sessionManager: SessionManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) {
    private val usageLogRepository: UsageLogRepository? = bySessionUsageLogRepository[sessionManager.session]

    var origin: String? = null

    fun showOfferList(
        productPeriodicity: ProductPeriodicity?,
        displayedOffers: List<OfferType>
    ) = log(
        action = "show",
        sendPremiumStatus = true,
        displayedOffers = displayedOffers.joinToString(",") { it.trackingKey().code },
        frequency = productPeriodicity?.trackingKey()
    )

    fun showOfferDetails(
        productPeriodicity: ProductPeriodicity?,
        offerType: OfferType
    ) = log(
        action = "show",
        sendPremiumStatus = true,
        frequency = productPeriodicity?.trackingKey(),
        planType = offerType.trackingKey()
    )

    fun logOpenStore(
        productPeriodicity: ProductPeriodicity?,
        offerType: OfferType,
        sku: String,
        price: Float,
        currency: String
    ) = log(
        action = "openStore",
        sendPremiumStatus = true,
        frequency = productPeriodicity?.trackingKey(),
        planType = offerType.trackingKey(),
        sku = sku,
        planPrice = price,
        planCurrency = currency
    )

    fun logPurchaseSuccess(
        productPeriodicity: ProductPeriodicity?,
        offerType: OfferType,
        sku: String,
        price: Float,
        currency: String
    ) = log(
        action = "paymentSuccess",
        sendPremiumStatus = true,
        frequency = productPeriodicity?.trackingKey(),
        planType = offerType.trackingKey(),
        sku = sku,
        planPrice = price,
        planCurrency = currency
    )

    fun logStoreOffersError(offerType: OfferType?) =
        log(
            action = "error",
            actionSub = "no_accessible_offer_result_available",
            planType = offerType?.trackingKey()
        )

    fun logSkuDetailsError(offerType: OfferType?) =
        log(
            action = "error",
            actionSub = "no_sku_details_available",
            planType = offerType?.trackingKey()
        )

    fun logNoValidOfferOption(offerType: OfferType?) =
        log(
            action = "error",
            actionSub = "no_valid_offer_option",
            planType = offerType?.trackingKey()
        )

    


    fun logVerifyingReceipt(planId: String) =
        log(action = "verifyingReceipt", sku = planId, sendPremiumStatus = false)

    fun logVerifyingReceiptPreparePurchase(planId: String) = log(action = "preparePurchase", sku = planId)
    fun logVerifyingReceiptAcknowledgeNeeded(planId: String) = log(action = "acknowledgeNeeded", sku = planId)
    fun logVerifyingReceiptHasSession(planId: String) = log(action = "hasSession", sku = planId)
    fun logVerifyingReceiptStart(planId: String) = log(action = "verifyingReceiptStart", sku = planId)
    fun logVerifyingReceiptSuccess(planId: String) = log(action = "verifyingReceiptSuccess", sku = planId)
    fun logVerifyingReceiptOnHomeStarted() = log(action = "verifyingReceiptOnHomeStarted", sku = null)
    fun logVerifyingReceiptError(planId: String) =
        log(action = "error", sku = planId, actionSub = "verifyReceiptError", sendPremiumStatus = false)

    fun logPurchaseComplete(planId: String) = log(action = "purchaseComplete", sku = planId)

    private val currentStatus: String by lazy {
        val status = premiumStatusManager.getFormattedStatus()
        return@lazy when (status.type) {
            UserBenefitStatus.Type.Free -> "free"
            UserBenefitStatus.Type.Trial -> "premium_trial"
            UserBenefitStatus.Type.AdvancedIndividual,
            UserBenefitStatus.Type.EssentialsIndividual -> "essentials"
            UserBenefitStatus.Type.PremiumIndividual -> "premium"
            UserBenefitStatus.Type.PremiumPlusIndividual -> "premium_plus"
            is UserBenefitStatus.Type.Family -> "family"
            is UserBenefitStatus.Type.FamilyPlus -> "premium_plus_family"
            else -> "unknown"
        }
    }

    private fun log(
        action: String,
        displayedOffers: String? = null,
        sendPremiumStatus: Boolean = true,
        frequency: UsageLogCode127.Frequency? = null,
        planType: UsageLogCode127.PlanType? = null,
        sku: String? = null,
        planPrice: Float? = null,
        planCurrency: String? = null,
        actionSub: String? = null
    ) {
        usageLogRepository?.enqueue(
            UsageLogCode127(
                type = UsageLogCode127.Type.PLANS_PAGE,
                action = action,
                frequency = frequency,
                planScreensKeys = displayedOffers,
                currentStatus = currentStatus.takeIf { sendPremiumStatus },
                sender = origin,
                planType = planType,
                planId = sku,
                planPrice = planPrice,
                planCurrency = planCurrency,
                actionSub = actionSub
            )
        )
    }
}

private fun OfferType.trackingKey() = when (this) {
    OfferType.ADVANCED -> UsageLogCode127.PlanType.ESSENTIALS
    OfferType.PREMIUM -> UsageLogCode127.PlanType.PREMIUM
    OfferType.FAMILY -> UsageLogCode127.PlanType.FAMILY
}

private fun ProductPeriodicity.trackingKey() = when (this) {
    ProductPeriodicity.MONTHLY -> UsageLogCode127.Frequency.MONTHLY
    ProductPeriodicity.YEARLY -> UsageLogCode127.Frequency.ANNUAL
}