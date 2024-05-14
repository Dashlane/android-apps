package com.dashlane.premium.offer.common

import com.android.billingclient.api.BillingFlowParams
import com.dashlane.premium.offer.common.model.FormattedStoreOffer
import com.dashlane.premium.offer.common.model.OfferType
import com.dashlane.premium.offer.common.model.OfferType.ADVANCED
import com.dashlane.premium.offer.common.model.OfferType.FAMILY
import com.dashlane.premium.offer.common.model.OfferType.PREMIUM
import com.dashlane.premium.offer.common.model.ProductDetailsWrapper
import com.dashlane.premium.offer.common.model.ProductPeriodicity
import com.dashlane.premium.offer.common.model.ProductPeriodicity.MONTHLY
import com.dashlane.premium.offer.common.model.ProductPeriodicity.YEARLY
import com.dashlane.server.api.endpoints.payments.StoreOffer
import com.dashlane.server.api.endpoints.payments.StoreOffersService
import java.time.Period
import javax.inject.Inject

class StoreOffersFormatterImpl @Inject constructor(
    private val productDetailsManager: ProductDetailsManager
) : StoreOffersFormatter {

    @Throws(ProductDetailsManager.NoProductDetailsResult::class)
    override suspend fun build(storeOffers: StoreOffersService.Data): List<FormattedStoreOffer> {
        val productsByOffer =
            listOf(ADVANCED, PREMIUM, FAMILY).associateWith {
                storeOffers.getOffer(it)
            }

        
        val skus = productsByOffer.getSKUs()
        productDetailsManager.getProductDetailsMap(skus)

        return productsByOffer.mapNotNull { (offerType, products) ->
            val monthly = buildOption(products, MONTHLY)
            val yearly = buildOption(products, YEARLY)
            FormattedStoreOffer(offerType, monthly, yearly)
        }
    }

    private suspend fun buildOption(
        products: List<StoreOffer.Product>,
        periodicity: ProductPeriodicity
    ): FormattedStoreOffer.Option? {
        products.forEach {
            try {
                val productDetails: ProductDetailsWrapper? = productDetailsManager.getProductDetails(it.sku)
                if (productDetails?.basePricingPhase?.billingInfo?.periodicity == periodicity) {
                    return FormattedStoreOffer.Option(
                        productDetails = productDetails,
                        enable = it.enabled ?: false,
                        mode = it.mode?.toProratedMode()
                    )
                }
            } catch (e: ProductDetailsManager.NoProductDetailsResult) {
                return null
            }
        }
        return null
    }

    companion object {
        val monthly: Period = Period.ofMonths(1)
        val yearly: Period = Period.ofYears(1)
    }
}

fun StoreOffersService.Data.getOffer(type: OfferType) = when (type) {
    ADVANCED -> essentials.products
    PREMIUM -> premium.products
    FAMILY -> family.products
}

private fun Map<OfferType, List<StoreOffer.Product>>.getSKUs(): List<String> {
    return flatMap { (_, products) ->
        products.filter { it.sku.isNotBlank() }
            .map { it.sku }
    }
}

private fun StoreOffer.Product.Mode.toProratedMode() = when (this) {
    StoreOffer.Product.Mode.IMMEDIATE_AND_CHARGE_PRORATED_PRICE -> BillingFlowParams.ProrationMode.IMMEDIATE_AND_CHARGE_PRORATED_PRICE
    StoreOffer.Product.Mode.IMMEDIATE_WITHOUT_PRORATION -> BillingFlowParams.ProrationMode.IMMEDIATE_WITHOUT_PRORATION
    StoreOffer.Product.Mode.DEFERRED -> BillingFlowParams.ProrationMode.DEFERRED
    StoreOffer.Product.Mode.IMMEDIATE_WITH_TIME_PRORATION -> BillingFlowParams.ProrationMode.IMMEDIATE_WITH_TIME_PRORATION
    else -> null
}