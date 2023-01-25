package com.dashlane.plans.ui.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.android.billingclient.api.Purchase
import com.dashlane.PurchaseCheckingNavigationDirections.Companion.goToPurchaseOfferError
import com.dashlane.PurchaseCheckingNavigationDirections.Companion.goToPurchaseOfferSuccess
import com.dashlane.R
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.events.PremiumStatusChangedEvent
import com.dashlane.events.clearLastEvent
import com.dashlane.events.register
import com.dashlane.events.unregister
import com.dashlane.security.DashlaneIntent
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.usagelogs.ViewLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



open class PurchaseCheckingActivity : DashlaneActivity() {

    private val appEvent = SingletonProvider.getAppEvents()
    private val logger = SingletonProvider.getOffersLogger()
    private val announcementCenter = SingletonProvider.getAnnouncementCenter()
    private val billingVerification = SingletonProvider.getBillingVerificator()

    private val navController: NavController
        get() = findNavController(R.id.nav_host_fragment_purchase_checking)

    private var checkingDone = false
    private lateinit var planId: String
    private lateinit var purchase: Purchase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase_checking)
        
        announcementCenter.disable()
    }

    override fun onResume() {
        super.onResume()
        appEvent.register<PremiumStatusChangedEvent>(this, true) { onPremiumStatusChanged(it) }
        if (!checkingDone) {
            processPurchase()
        }
        ViewLogger().log("PurchaseConfirmation")
    }

    override fun onPause() {
        super.onPause()
        appEvent.unregister<PremiumStatusChangedEvent>(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            announcementCenter.restorePreviousState()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        
        if (checkingDone) {
            finish()
        }
    }

    

    private fun onPremiumStatusChanged(event: PremiumStatusChangedEvent) {
        appEvent.clearLastEvent<PremiumStatusChangedEvent>()
        
        if (checkingDone || (!event.premiumPlanChanged() && !event.hasBeenRenewed())) {
            return
        }
        checkingDone = true

        val familyUser = event.newStatus.isFamilyUser
        val planName = when {
            event.newStatus.premiumPlan.isPremiumPlus -> getString(R.string.plan_premium_plus_title)
            event.newStatus.isPremium && familyUser -> getString(R.string.plan_premium_family_title)
            event.newStatus.isPremium -> getString(R.string.plan_premium_title)
            else -> {
                
                logger.logVerifyingReceiptError(planId)
                finish()
                return
            }
        }

        logger.logPurchaseComplete(planId)
        navController.navigate(goToPurchaseOfferSuccess(planName, familyUser))
        SingletonProvider.getAccessibleOffersCache().flushCache()
    }

    private fun onPurchaseCheckFailed() {
        if (checkingDone) {
            return
        }
        logger.logVerifyingReceiptError(planId)
        checkingDone = true
        navController.navigate(goToPurchaseOfferError())
        SingletonProvider.getAccessibleOffersCache().flushCache()
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
            billingVerification.verifyAndConsumePurchase(purchase, currency, price, errorListener)
            
            delay(30_000)
            if (!checkingDone) onPurchaseCheckFailed()
        }
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
            totalPrice: Float?,
            userLockedOut: Boolean
        ): Intent {
            val activity =
                if (userLockedOut) PurchaseCheckingActivityLockedOut::class.java else PurchaseCheckingActivity::class.java
            return DashlaneIntent.newInstance(context, activity).apply {
                putExtra(EXTRA_PURCHASED_PLAN_ID, purchasePlanId)
                putExtra(EXTRA_PURCHASED_ORIGINAL_JSON, purchaseOriginalJson)
                putExtra(EXTRA_PURCHASED_SIGNATURE, signature)
                putExtra(PRODUCT_CURRENCY, currencyCode)
                putExtra(PRODUCT_TOTAL_PRICE, totalPrice)
            }
        }
    }
}

private fun PremiumStatusChangedEvent.hasBeenRenewed() = !hasSameExpirationDate() || hasAutoRenewBeenTurnOn()

private fun PremiumStatusChangedEvent.hasAutoRenewBeenTurnOn() =
    previousStatus?.willAutoRenew() == false &&
            newStatus?.willAutoRenew() == true

private fun PremiumStatusChangedEvent.hasSameExpirationDate(): Boolean {
    val previousEndDate = previousStatus?.endDate
    val newEndDate = newStatus?.endDate
    return previousEndDate == newEndDate
}
