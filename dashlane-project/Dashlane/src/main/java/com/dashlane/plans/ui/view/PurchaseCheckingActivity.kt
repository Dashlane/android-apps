package com.dashlane.plans.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.android.billingclient.api.Purchase
import com.dashlane.PurchaseCheckingNavigationDirections.Companion.goToPurchaseOfferError
import com.dashlane.PurchaseCheckingNavigationDirections.Companion.goToPurchaseOfferSuccess
import com.dashlane.R
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.accountstatus.premiumstatus.autoRenewal
import com.dashlane.accountstatus.premiumstatus.endDate
import com.dashlane.accountstatus.premiumstatus.isFamilyPlan
import com.dashlane.accountstatus.premiumstatus.isPremiumPlan
import com.dashlane.accountstatus.premiumstatus.isPremiumPlusPlan
import com.dashlane.accountstatus.premiumstatus.planFeature
import com.dashlane.accountstatus.premiumstatus.planName
import com.dashlane.accountstatus.premiumstatus.planType
import com.dashlane.announcements.AnnouncementCenter
import com.dashlane.premium.offer.common.OffersLogger
import com.dashlane.security.DashlaneIntent
import com.dashlane.server.api.endpoints.premium.PremiumStatus
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.premium.inappbilling.BillingVerification
import com.dashlane.ui.premium.inappbilling.service.StoreOffersCache
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@AndroidEntryPoint
open class PurchaseCheckingActivity : DashlaneActivity() {

    @Inject
    lateinit var logger: OffersLogger

    @Inject
    lateinit var announcementCenter: AnnouncementCenter

    @Inject
    lateinit var billingVerification: BillingVerification

    @Inject
    lateinit var storeOffersCache: StoreOffersCache

    @Inject
    lateinit var accountStatusRepository: AccountStatusRepository

    @Inject
    lateinit var sessionManager: SessionManager

    private val navController: NavController
        get() = findNavController(R.id.nav_host_fragment_purchase_checking)
    private val session: Session?
        get() = sessionManager.session

    private var checkingDone = false
    private lateinit var planId: String
    private lateinit var purchase: Purchase

    private var initialPremiumStatus: PremiumStatus? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase_checking)
        
        announcementCenter.disable()

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                accountStatusRepository.accountStatusState.collect { accountStatuses ->
                    accountStatuses[session]?.premiumStatus?.let { newStatus ->
                        
                        val previousStatus = initialPremiumStatus
                        if (previousStatus != null) {
                            onPremiumStatusChanged(previousStatus, newStatus)
                        } else {
                            initialPremiumStatus = newStatus
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!checkingDone) {
            processPurchase()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            announcementCenter.restorePreviousState()
        }
    }

    @Suppress("DEPRECATION")
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        
        if (checkingDone) {
            finish()
        }
    }

    private fun onPremiumStatusChanged(previousStatus: PremiumStatus, newStatus: PremiumStatus) {
        
        if (checkingDone || (!premiumPlanChanged(previousStatus, newStatus) && !hasBeenRenewed(previousStatus, newStatus))) {
            return
        }
        checkingDone = true

        val isFamilyPlan = newStatus.isFamilyPlan
        val planName = when {
            newStatus.isPremiumPlusPlan -> getString(R.string.plan_premium_plus_title)
            isFamilyPlan -> getString(R.string.plan_premium_family_title)
            newStatus.isPremiumPlan -> getString(R.string.plan_premium_title)
            else -> {
                
                logger.logVerifyingReceiptError(planId)
                finish()
                return
            }
        }

        logger.logPurchaseComplete(planId)
        navController.navigate(goToPurchaseOfferSuccess(planName, isFamilyPlan))
        storeOffersCache.flushCache()
    }

    private fun onPurchaseCheckFailed() {
        if (checkingDone) {
            return
        }
        logger.logVerifyingReceiptError(planId)
        checkingDone = true
        navController.navigate(goToPurchaseOfferError())
        storeOffersCache.flushCache()
    }

    private fun processPurchase() {
        
        
        val errorListener = { runOnUiThread { onPurchaseCheckFailed() } }
        planId = intent.getStringExtra(EXTRA_PURCHASED_PLAN_ID) ?: "unknown"
        logger.logVerifyingReceipt(planId)
        val originalJson = checkNotNull(intent.getStringExtra(EXTRA_PURCHASED_ORIGINAL_JSON))
        val signature = checkNotNull(intent.getStringExtra(EXTRA_PURCHASED_SIGNATURE))
        val currencyCode = checkNotNull(intent.getStringExtra(PRODUCT_CURRENCY))
        val totalPrice = checkNotNull(intent.getFloatExtra(PRODUCT_TOTAL_PRICE, 0f))

        purchase = Purchase(originalJson, signature)
        logger.logVerifyingReceiptPreparePurchase(planId)
        verifyAndConsume(purchase, currencyCode, totalPrice, errorListener)
    }

    private fun verifyAndConsume(purchase: Purchase, currency: String, price: Float, errorListener: () -> Unit) {
        launch {
            try {
                
                withTimeout(30_000) {
                    billingVerification.verifyAndConsumePurchase(purchase, currency, price, errorListener)
                }
            } catch (e: TimeoutCancellationException) {
                if (!checkingDone) onPurchaseCheckFailed()
            }
        }
    }

    private fun premiumPlanChanged(previousStatus: PremiumStatus, newStatus: PremiumStatus): Boolean {
        return previousStatus.planName != newStatus.planName ||
            previousStatus.planType != newStatus.planType ||
            previousStatus.planFeature != newStatus.planFeature
    }

    private fun hasBeenRenewed(previousStatus: PremiumStatus, newStatus: PremiumStatus) =
        !hasSameExpirationDate(previousStatus, newStatus) || hasAutoRenewBeenTurnOn(previousStatus, newStatus)

    private fun hasAutoRenewBeenTurnOn(previousStatus: PremiumStatus, newStatus: PremiumStatus) =
        !previousStatus.autoRenewal && newStatus.autoRenewal

    private fun hasSameExpirationDate(previousStatus: PremiumStatus, newStatus: PremiumStatus): Boolean {
        val previousEndDate = previousStatus.endDate
        val newEndDate = newStatus.endDate
        return previousEndDate == newEndDate
    }

    companion object {
        const val EXTRA_PURCHASED_PLAN_ID = "purchasedPlanId"
        const val EXTRA_PURCHASED_ORIGINAL_JSON = "purchasedOriginalJson"
        const val EXTRA_PURCHASED_SIGNATURE = "purchasedSignature"
        const val PRODUCT_TOTAL_PRICE = "product_total_price"
        const val PRODUCT_CURRENCY = "product_currency"

        fun newIntentForPlayStorePurchase(
            context: Context,
            purchasePlanId: String,
            purchaseOriginalJson: String,
            signature: String,
            currencyCode: String?,
            totalPrice: Float?
        ): Intent {
            return DashlaneIntent.newInstance(context, PurchaseCheckingActivity::class.java).apply {
                putExtra(EXTRA_PURCHASED_PLAN_ID, purchasePlanId)
                putExtra(EXTRA_PURCHASED_ORIGINAL_JSON, purchaseOriginalJson)
                putExtra(EXTRA_PURCHASED_SIGNATURE, signature)
                putExtra(PRODUCT_CURRENCY, currencyCode)
                putExtra(PRODUCT_TOTAL_PRICE, totalPrice)
            }
        }
    }
}
