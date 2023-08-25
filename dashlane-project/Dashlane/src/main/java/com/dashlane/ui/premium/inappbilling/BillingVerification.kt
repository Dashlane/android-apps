package com.dashlane.ui.premium.inappbilling

import com.android.billingclient.api.Purchase
import com.dashlane.inappbilling.BillingManager
import com.dashlane.inappbilling.ServiceResult
import com.dashlane.inappbilling.withServiceConnection
import com.dashlane.network.webservices.VerifyReceiptService
import com.dashlane.premium.offer.common.OffersLogger
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLogConstant
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

@Singleton
class BillingVerification @Inject constructor(
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    private val sessionManager: SessionManager,
    private val verifyReceiptService: VerifyReceiptService,
    private val billingManager: BillingManager,
    private val logger: OffersLogger,
    private val refreshPremiumAfterPurchase: RefreshPremiumAfterPurchase
) {
    private var lastVerifyPurchases: Long = 0

    val session: Session?
        get() = sessionManager.session

    fun verifyAndConsumePurchaseIfNeeded() {
        if (shouldSkip()) {
            return
        }
        logger.logVerifyingReceiptOnHomeStarted()
        applicationCoroutineScope.launch {
            when (val serviceResult = billingManager.withServiceConnection { queryPurchases(Dispatchers.IO) }) {
                is ServiceResult.Success.Purchases -> {
                    
                    val purchaseCandidate = serviceResult.purchases.filter {
                        !it.isAcknowledgedCompat() && it.purchaseState != Purchase.PurchaseState.PENDING
                    }.minByOrNull { it.purchaseTime } ?: return@launch
                    verifyAndConsumePurchase(purchaseCandidate)
                }
                else -> {}
            }
        }
    }

    suspend fun verifyAndConsumePurchase(
        purchase: Purchase,
        currency: String? = null,
        price: Float? = null,
        errorListener: () -> Unit = {}
    ) {
        val acknowledged = purchase.isAcknowledgedCompat()
        if (acknowledged) return
        val planId = purchase.products.firstOrNull()
        logger.logVerifyingReceiptAcknowledgeNeeded(planId)
        val session = session ?: return
        logger.logVerifyingReceiptHasSession(planId)

        val verifyReceipt = runCatching {
            logger.logVerifyingReceiptStart(planId)
            verifyReceipt(purchase, currency, price, session.userId, session.uki)
        }.getOrElse {
            UsageLogCode35GoPremium.send(UsageLogConstant.PremiumAction.errorAfterPurchase)
            errorListener.invoke()
            null
        }
        refreshPremiumAfterPurchase.execute(errorListener)
        if (verifyReceipt?.success != true) {
            UsageLogCode35GoPremium.send(UsageLogConstant.PremiumAction.receiptFailedValidation)
            errorListener.invoke()
            return
        }
        logger.logVerifyingReceiptSuccess(planId)
        if (verifyReceipt.isRenewable) {
            UsageLogCode35GoPremium.send(UsageLogConstant.PremiumAction.appTryingToAcknowledgePurchase, planId)
            when (val acknowledgeResult = acknowledge(purchase)) {
                is ServiceResult.Success ->
                    UsageLogCode35GoPremium.send(UsageLogConstant.PremiumAction.acknowledgementSuccess)
                is ServiceResult.Failure ->
                    UsageLogCode35GoPremium.send(
                        UsageLogConstant.PremiumAction.acknowledgementFailure,
                        acknowledgeResult.name
                    )
            }
        } else {
            UsageLogCode35GoPremium.send(UsageLogConstant.PremiumAction.appTryingToConsumePurchase, planId)
            consume(purchase)
        }
        UsageLogCode35GoPremium.send(UsageLogConstant.PremiumAction.confirmPurchase)
        UsageLogCode35GoPremium.send(
            UsageLogConstant.PremiumAction.purchasedSuccessfulPremiumStatusNotCheckedYet
        )
    }

    private suspend fun consume(purchase: Purchase) {
        billingManager.withServiceConnection { consume(purchase.purchaseToken) }
    }

    private suspend fun acknowledge(purchase: Purchase) =
        billingManager.withServiceConnection { acknowledge(purchase.purchaseToken) }

    private suspend fun verifyReceipt(
        purchase: Purchase,
        currency: String? = null,
        price: Float? = null,
        userName: String,
        uki: String
    ): VerifyReceiptService.VerifyReceiptResponse {
        UsageLogCode35GoPremium.send(UsageLogConstant.PremiumAction.appTryingToValidateReceipt)
        val purchaseToken = purchase.purchaseToken
        return verifyReceiptService.verifyReceipt(
            login = userName,
            uki = uki,
            receipt = purchaseToken,
            plan = purchase.products.firstOrNull(),
            currency = currency,
            amount = price?.let { String.format(Locale.US, "%.2f", it) }
        )
    }

    private fun shouldSkip(): Boolean {
        if (lastVerifyPurchases > 0 && System.currentTimeMillis() - lastVerifyPurchases < MIN_INTERVAL_REFRESH) {
            return true 
        }
        lastVerifyPurchases = System.currentTimeMillis()
        return false
    }

    private fun Purchase.isAcknowledgedCompat() = JSONObject(originalJson).optBoolean("acknowledged", false)

    companion object {
        private val MIN_INTERVAL_REFRESH = TimeUnit.MINUTES.toMillis(2) 
    }
}
