package com.dashlane.premium.offer.details

import android.content.Context
import androidx.annotation.StringRes
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.premium.R
import com.dashlane.premium.offer.common.ProductDetailsManager
import com.dashlane.premium.offer.common.StoreOffersFormatter
import com.dashlane.premium.offer.common.StoreOffersManager
import com.dashlane.premium.offer.common.model.FormattedStoreOffer
import com.dashlane.premium.offer.common.model.OfferDetails
import com.dashlane.premium.offer.common.model.OfferType
import com.dashlane.premium.offer.common.model.OfferType.ADVANCED
import com.dashlane.premium.offer.common.model.OfferType.FAMILY
import com.dashlane.premium.offer.common.model.OfferType.FREE
import com.dashlane.premium.offer.common.model.OfferType.PREMIUM
import com.dashlane.premium.offer.common.model.OffersState
import com.dashlane.premium.offer.common.model.ProductDetailsWrapper
import com.dashlane.premium.offer.common.model.ProductPeriodicity
import com.dashlane.premium.offer.common.model.ProductPeriodicity.MONTHLY
import com.dashlane.premium.offer.common.model.ProductPeriodicity.YEARLY
import com.dashlane.server.api.endpoints.payments.StoreOffersService
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.featureflipping.UserFeaturesChecker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class OfferDetailsDataProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storeOffersManager: StoreOffersManager,
    private val storeOffersFormatter: StoreOffersFormatter,
    private val transitionHelper: TransitionHelper,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val conflictingBillingPlatformProvider: ConflictingBillingPlatformProvider,
    private val frozenStateManager: FrozenStateManager,
) {

    @StringRes
    fun getTitle(offerType: OfferType) =
        when (offerType) {
            ADVANCED -> R.string.plan_details_advanced_title
            PREMIUM -> R.string.plan_details_premium_title
            FAMILY -> R.string.plan_details_family_title
            FREE -> R.string.plan_details_free_title
        }

    suspend fun getOffer(offerType: OfferType): OffersState {
        val storeOffers = getStoreOffers()
            ?: return OffersState.NoStoreOffersResultAvailable
        val benefits = offerType.getBenefits(storeOffers).map { it.format(context.resources) }
        val formattedOffers = try {
            storeOffersFormatter.build(storeOffers).find { it.offerType == offerType }
        } catch (e: ProductDetailsManager.NoProductDetailsResult) {
            return OfferDetails(benefits = benefits)
        }
        
        if (offerType == FREE) {
            return OfferDetails(
                warning = if (frozenStateManager.isAccountFrozen) {
                    context.resources.getString(
                        R.string.frozen_account_free_plan_detail_warning,
                        frozenStateManager.passwordLimitCount.toString()
                    )
                } else {
                    null
                },
                benefits = benefits,
                monthlyProduct = null,
                yearlyProduct = null,
                extraCtaString = context.getString(
                    R.string.frozen_account_free_plan_cta_manage_login.takeIf { frozenStateManager.isAccountFrozen }
                        ?: R.string.frozen_account_free_plan_cta_keep_free
                )
            )
        }
        if (formattedOffers == null || (formattedOffers.monthly == null && formattedOffers.yearly == null)) {
            
            
            return OffersState.NoValidOfferAvailable
        }
        val warning = conflictingBillingPlatformProvider.getWarning()?.format(context.resources)
        val monthlyProduct = buildProduct(MONTHLY, formattedOffers, storeOffers)
        val yearlyProduct = buildProduct(YEARLY, formattedOffers, storeOffers)

        return OfferDetails(
            warning = warning,
            benefits = benefits,
            monthlyProduct = monthlyProduct,
            yearlyProduct = yearlyProduct
        )
    }

    fun getCtaString(priceInfo: OfferDetails.PriceInfo) = priceInfo.getCtaString(context)

    private suspend fun getStoreOffers(): StoreOffersService.Data? {
        return try {
            this.storeOffersManager.fetchProductsForCurrentUser()
        } catch (e: DashlaneApiException) {
            
            null
        } catch (e: StoreOffersManager.UserNotLoggedException) {
            
            null
        }
    }

    private fun buildProduct(
        periodicity: ProductPeriodicity,
        formattedOffer: FormattedStoreOffer,
        storeOffers: StoreOffersService.Data
    ): OfferDetails.Product? {
        val option = when (periodicity) {
            MONTHLY -> formattedOffer.monthly
            YEARLY -> formattedOffer.yearly
        } ?: return null
        val productDetails = option.productDetails
        val basePricingPhase = productDetails.basePricingPhase
        val originalPriceMicro = basePricingPhase.priceMicro
        val currencyCode = basePricingPhase.priceCurrencyCode
        val (enabled, update) = transitionHelper(
            option = option,
            storeOffers = storeOffers
        )
        val priceInfo = if (productDetails is ProductDetailsWrapper.IntroductoryOfferProduct) {
            val introOfferPricingPhase = productDetails.introductoryPlanOffer.pricingPhases.first()
            OfferDetails.PriceInfo.PendingOffer(
                currencyCode = currencyCode,
                baseOfferPriceValue = originalPriceMicro / MICRO,
                baseOfferPeriodicity = productDetails.basePricingPhase.billingInfo,
                introOfferPriceValue = introOfferPricingPhase.priceMicro / MICRO,
                introOfferPeriodicity = productDetails.introductoryPricingPhase.billingInfo,
                subscriptionOfferToken = productDetails.introductoryPlanOffer.offerIdToken,
                introOfferCycleLength = productDetails.introductoryPricingPhase.cycleCount
            )
        } else {
            OfferDetails.PriceInfo.BaseOffer(
                currencyCode = currencyCode,
                baseOfferPriceValue = originalPriceMicro / MICRO,
                baseOfferPeriodicity = productDetails.basePricingPhase.billingInfo,
                subscriptionOfferToken = productDetails.basePlanOffer.offerIdToken

            )
        }

        return OfferDetails.Product(
            productId = productDetails.productId,
            update = update,
            enabled = enabled,
            priceInfo = priceInfo,
            productDetails = productDetails
        )
    }

    private fun OfferType.getBenefits(storeOffers: StoreOffersService.Data) =
        when (this) {
            ADVANCED -> storeOffers.essentials.capabilities
            PREMIUM -> storeOffers.premium.capabilities
            FAMILY -> storeOffers.family.capabilities
            FREE -> storeOffers.free.capabilities
        }.let { BenefitsBuilder(it, userFeaturesChecker).build(this == FAMILY) }

    fun getMonthlyInfoString(priceInfo: OfferDetails.PriceInfo?) =
        priceInfo?.getMonthlyInfoString(context)

    fun getYearlyInfoString(priceInfo: OfferDetails.PriceInfo?) =
        priceInfo?.getYearlyInfoString(context)

    companion object {
        private const val MICRO = 1_000_000.0f
    }
}
