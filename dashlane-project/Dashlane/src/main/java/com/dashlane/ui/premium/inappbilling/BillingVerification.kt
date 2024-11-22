package com.dashlane.ui.premium.inappbilling

import com.android.billingclient.api.Purchase
import com.dashlane.inappbilling.BillingManager
import com.dashlane.inappbilling.ServiceResult
import com.dashlane.inappbilling.withServiceConnection
import com.dashlane.session.authorization
import com.dashlane.premium.offer.common.OffersLogger
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.payments.VerifyPlaystoreReceiptService
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingVerification @Inject constructor(
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    private val sessionManager: SessionManager,
    private val verifyPlaystoreReceiptService: VerifyPlaystoreReceiptService,
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
        val planId = purchase.products.firstOrNull() ?: return
        logger.logVerifyingReceiptAcknowledgeNeeded(planId)
        val session = session ?: return
        logger.logVerifyingReceiptHasSession(planId)

        val verifyReceipt = runCatching {
            logger.logVerifyingReceiptStart(planId)
            verifyReceipt(
                purchase = purchase,
                plan = planId,
                currency = currency,
                price = price,
                authorization = session.authorization
            )
        }.getOrElse {
            errorListener()
            null
        }
        refreshPremiumAfterPurchase.execute(errorListener)
        if (verifyReceipt?.success != true) {
            errorListener()
            return
        }
        logger.logVerifyingReceiptSuccess(planId)
        if (verifyReceipt.planType == PLAYSTORE_RENEWABLE) {
            acknowledge(purchase)
        } else {
            consume(purchase)
        }
    }

    private suspend fun consume(purchase: Purchase) {
        billingManager.withServiceConnection { consume(purchase.purchaseToken) }
    }

    private suspend fun acknowledge(purchase: Purchase) {
        billingManager.withServiceConnection { acknowledge(purchase.purchaseToken) }
    }

    private suspend fun verifyReceipt(
        purchase: Purchase,
        plan: String,
        currency: String? = null,
        price: Float? = null,
        authorization: Authorization.User,
    ): VerifyPlaystoreReceiptService.Data {
        val purchaseToken = purchase.purchaseToken
        return verifyPlaystoreReceiptService.execute(
            userAuthorization = authorization,
            request = VerifyPlaystoreReceiptService.Request(
                receipt = purchaseToken,
                plan = plan,
                transactionIdentifier = purchase.orderId,
                amount = price?.let { String.format(Locale.US, "%.2f", it) },
                currency = currency,
            )
        ).data
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
        private const val PLAYSTORE_RENEWABLE = "playstore_renewable"
    }
}
