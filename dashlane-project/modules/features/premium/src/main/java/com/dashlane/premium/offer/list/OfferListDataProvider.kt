package com.dashlane.premium.offer.list

import android.content.Context
import com.dashlane.accountstatus.AccountStatus
import com.dashlane.premium.R
import com.dashlane.premium.offer.common.ProductDetailsManager
import com.dashlane.premium.offer.common.StoreOffersFormatter
import com.dashlane.premium.offer.common.StoreOffersManager
import com.dashlane.premium.offer.common.UserBenefitStatusProvider
import com.dashlane.premium.offer.common.YearlySavingCalculator
import com.dashlane.premium.offer.common.getOffer
import com.dashlane.premium.offer.common.model.FormattedStoreOffer
import com.dashlane.premium.offer.common.model.OfferType
import com.dashlane.premium.offer.common.model.OfferType.ADVANCED
import com.dashlane.premium.offer.common.model.OfferType.FAMILY
import com.dashlane.premium.offer.common.model.OfferType.PREMIUM
import com.dashlane.premium.offer.common.model.OffersState
import com.dashlane.premium.offer.common.model.ProductPeriodicity
import com.dashlane.premium.offer.common.model.ProductPeriodicity.MONTHLY
import com.dashlane.premium.offer.common.model.ProductPeriodicity.YEARLY
import com.dashlane.premium.offer.common.model.UserBenefitStatus
import com.dashlane.premium.offer.common.model.UserBenefitStatus.RenewPeriodicity
import com.dashlane.premium.offer.common.model.UserBenefitStatus.Type
import com.dashlane.premium.offer.list.model.CurrentOffer
import com.dashlane.server.api.endpoints.payments.StoreOffer
import com.dashlane.server.api.endpoints.payments.StoreOffersService
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.featureflipping.canShowVpn
import com.dashlane.util.inject.OptionalProvider
import com.skocken.presentation.provider.BaseDataProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class OfferListDataProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val accountStatusProvider: OptionalProvider<AccountStatus>,
    private val storeOffersManager: StoreOffersManager,
    private val storeOffersFormatter: StoreOffersFormatter,
    private val premiumStatusManager: UserBenefitStatusProvider,
    private val userFeaturesChecker: UserFeaturesChecker
) : BaseDataProvider<OfferListContract.Presenter>(), OfferListContract.DataProvider {

    override suspend fun getOffers(): OffersState {
        val storeOffers = getStoreOffers()
            ?: return OffersState.NoStoreOffersResultAvailable
        val formattedStatus = premiumStatusManager.getFormattedStatus(accountStatusProvider.get())
        val formattedOffers = try {
            storeOffersFormatter.build(storeOffers)
        } catch (e: ProductDetailsManager.NoProductDetailsResult) {
            
            val currentOffer = getCurrentOfferInfo(formattedStatus, hasPlayStore = false)
            return OffersBuilder(
                monthlyOfferTypes = getOrderedAvailableOfferTypesWithoutPlayStore(storeOffers, MONTHLY),
                yearlyOfferTypes = getOrderedAvailableOfferTypesWithoutPlayStore(storeOffers, YEARLY),
                currentOffer = currentOffer,
                vpnMentionAllowed = isVpnMentionAllowed()
            ).build(context.resources)
        }
        if (formattedOffers.isEmpty()) {
            return OffersState.NoValidOfferAvailable
        }
        val currencyFormatter = YearlySavingCalculator.getFormatter(formattedOffers)
        val yearlySavings = computeYearlySaving(formattedOffers)
        return OffersBuilder(
            monthlyOfferTypes = getOrderedAvailableOfferTypes(formattedOffers, MONTHLY),
            yearlyOfferTypes = getOrderedAvailableOfferTypes(formattedOffers, YEARLY),
            formattedOffers = formattedOffers,
            currencyFormatter = currencyFormatter,
            yearlySavings = yearlySavings,
            currentOffer = getCurrentOfferInfo(formattedStatus),
            vpnMentionAllowed = isVpnMentionAllowed()
        ).build(context.resources)
    }

    private suspend fun getStoreOffers(): StoreOffersService.Data? {
        return try {
            this.storeOffersManager.fetchProductsForCurrentUser()
        } catch (e: DashlaneApiException) {
            
            null
        } catch (e: StoreOffersManager.UserNotLoggedException) {
            
            null
        }
    }

    private fun getOrderedAvailableOfferTypes(
        formattedOffers: List<FormattedStoreOffer>,
        periodicity: ProductPeriodicity
    ) = listOf(ADVANCED, PREMIUM, FAMILY)
        .filter { type ->
            val offer = formattedOffers.find { it.offerType == type }
            when (periodicity) {
                MONTHLY -> offer?.monthly != null
                YEARLY -> offer?.yearly != null
            }
        }

    private fun getOrderedAvailableOfferTypesWithoutPlayStore(
        storeOffers: StoreOffersService.Data,
        periodicity: ProductPeriodicity
    ): List<OfferType> {
        val periodicityAsDuration = when (periodicity) {
            MONTHLY -> StoreOffer.Product.Duration.MONTHLY
            YEARLY -> StoreOffer.Product.Duration.YEARLY
        }
        return listOf(ADVANCED, PREMIUM, FAMILY)
            .filter { type ->
                storeOffers.getOffer(type).any { it.duration == periodicityAsDuration }
            }
    }

    private fun computeYearlySaving(formattedOffers: List<FormattedStoreOffer>) =
        formattedOffers.associateBy(
            { it.offerType },
            { YearlySavingCalculator(it) }
        )

    private fun getCurrentOfferInfo(
        formattedStatus: UserBenefitStatus,
        hasPlayStore: Boolean = true
    ): CurrentOffer? {
        val currentOfferType = when (formattedStatus.type) {
            Type.Trial -> return CurrentOffer(PREMIUM, MONTHLY, R.string.plans_on_going_trial)
            Type.AdvancedIndividual -> ADVANCED
            Type.PremiumIndividual -> PREMIUM
            is Type.Family -> FAMILY
            else -> return null
        }
        val currentPeriodicity = when (formattedStatus.renewPeriodicity) {
            RenewPeriodicity.MONTHLY -> MONTHLY
            RenewPeriodicity.YEARLY -> YEARLY
            else -> if (hasPlayStore) {
                return null
            } else {
                
                MONTHLY
            }
        }
        return CurrentOffer(currentOfferType, currentPeriodicity, R.string.plans_on_going_plan)
    }

    private fun isVpnMentionAllowed() = userFeaturesChecker.canShowVpn()
}
