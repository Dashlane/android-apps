package com.dashlane.premium.offer.common.model

import android.content.Context
import com.dashlane.premium.R
import com.dashlane.premium.utils.PriceUtils.MICRO
import com.dashlane.premium.utils.toFormattedPrice
import com.dashlane.ui.model.TextResource
import java.util.Locale

sealed class IntroOfferType {
    abstract val productDetails: ProductDetailsWrapper.IntroductoryOfferProduct
    abstract val offerType: OfferType

    abstract fun getNotificationTitle(context: Context): String
    abstract fun getNotificationDescription(context: Context): String

    abstract fun getAnnouncementTitle(context: Context): String
    abstract fun getAnnouncementMessage(context: Context): String

    internal val offerPricing: ProductDetailsWrapper.PricingPhase
        get() = productDetails.introductoryPricingPhase

    internal val basePlanPricing: ProductDetailsWrapper.PricingPhase
        get() = productDetails.basePricingPhase

    internal fun getLocate(context: Context): String = context.getString(R.string.language_iso_639_1)

    internal fun getOfferTypeString(context: Context) = context.getString(offerType.plansResId())

    val productId: String
        get() = productDetails.productId

    val offerId: String
        get() = productDetails.introductoryPlanOffer.idTag ?: productId

    internal val offerLength: Int
        get() = offerPricing.billingInfo.length

    internal val offerTotalLength: Int
        get() = offerLength * offerPricing.cycleCount

    internal val offerPeriodicity: ProductPeriodicity
        get() = offerPricing.billingInfo.periodicity

    internal val basePlanPeriodicity: ProductPeriodicity
        get() = basePlanPricing.billingInfo.periodicity

    internal val offerPriceValue
        get() = offerPricing.priceMicro.toFloat() / MICRO

    internal val offerUnitPriceValue
        get() = offerPriceValue / offerLength

    internal fun Float.toFormattedPrice(context: Context): String {
        return this.toDouble().toFormattedPrice(offerPricing.priceCurrencyCode, Locale(getLocate(context)))
    }

    internal fun getFreeTrialNotificationTitle(context: Context): String {
        val pluralId = when (offerPeriodicity) {
            ProductPeriodicity.MONTHLY -> R.plurals.action_item_title_free_trial_month
            ProductPeriodicity.YEARLY -> R.plurals.action_item_title_free_trial_year
        }
        return TextResource.PluralsText(
            pluralsRes = pluralId,
            quantity = offerTotalLength,
            TextResource.Arg.IntArg(offerTotalLength),
            TextResource.Arg.StringArg(getOfferTypeString(context))
        ).format(context.resources)
    }

    internal fun getFreeTrialAnnouncementTitle(context: Context): String {
        val pluralId = when (offerPeriodicity) {
            ProductPeriodicity.MONTHLY -> R.plurals.intro_offers_title_free_trial_month
            ProductPeriodicity.YEARLY -> R.plurals.intro_offers_dialog_title_free_trial_year
        }
        return TextResource.PluralsText(
            pluralsRes = pluralId,
            quantity = offerTotalLength,
            listOf(
                TextResource.Arg.IntArg(offerTotalLength),
                TextResource.Arg.StringArg(getOfferTypeString(context))
            )
        ).format(context.resources)
    }

    data class MatchingCycleFreeTrial(
        override val offerType: OfferType,
        override val productDetails: ProductDetailsWrapper.IntroductoryOfferProduct
    ) : IntroOfferType() {
        override fun getNotificationTitle(context: Context) = getFreeTrialNotificationTitle(context)

        override fun getNotificationDescription(context: Context): String {
            val pluralId = when (offerPeriodicity) {
                ProductPeriodicity.MONTHLY -> R.plurals.action_item_description_free_trial_month
                ProductPeriodicity.YEARLY -> R.plurals.action_item_description_free_trial_year
            }
            val basePlanFormattedPrice = (basePlanPricing.priceMicro.toFloat() / MICRO)
                .toFormattedPrice(context) + context.getString(basePlanPeriodicity.suffixRes)
            return TextResource.PluralsText(
                pluralsRes = pluralId,
                quantity = offerTotalLength,
                listOf(
                    TextResource.Arg.IntArg(offerTotalLength),
                    TextResource.Arg.StringArg(getOfferTypeString(context)),
                    TextResource.Arg.StringArg(basePlanFormattedPrice)
                )
            ).format(context.resources)
        }

        override fun getAnnouncementTitle(context: Context): String {
            return getFreeTrialAnnouncementTitle(context)
        }

        override fun getAnnouncementMessage(context: Context): String {
            val pluralId = when (offerPeriodicity) {
                ProductPeriodicity.MONTHLY -> R.plurals.intro_offers_dialog_message_free_trial_month
                ProductPeriodicity.YEARLY -> R.plurals.intro_offers_dialog_message_free_trial_year
            }
            return TextResource.PluralsText(
                pluralsRes = pluralId,
                quantity = offerTotalLength,
                listOf(
                    TextResource.Arg.IntArg(offerTotalLength),
                    TextResource.Arg.StringArg(getOfferTypeString(context))
                )
            ).format(context.resources)
        }
    }

    data class MismatchingCycleFreeTrial(
        override val offerType: OfferType,
        override val productDetails: ProductDetailsWrapper.IntroductoryOfferProduct
    ) : IntroOfferType() {

        override fun getNotificationTitle(context: Context) = getFreeTrialNotificationTitle(context)

        override fun getNotificationDescription(context: Context): String {
            val basePlanFormattedPrice =
                basePlanPricing.priceMicro.toFloat()
                    .toFormattedPrice(context) + context.getString(basePlanPeriodicity.suffixRes)
            return TextResource.PluralsText(
                pluralsRes = R.plurals.action_item_description_monthly_free_trial_to_yearly,
                quantity = offerTotalLength,
                listOf(
                    TextResource.Arg.IntArg(offerTotalLength),
                    TextResource.Arg.StringArg(getOfferTypeString(context)),
                    TextResource.Arg.StringArg(basePlanFormattedPrice)
                )
            ).format(context.resources)
        }

        override fun getAnnouncementTitle(context: Context): String = getFreeTrialAnnouncementTitle(context)

        override fun getAnnouncementMessage(context: Context): String {
            return TextResource.PluralsText(
                pluralsRes = R.plurals.intro_offers_dialog_message_monthly_free_trial_to_year,
                quantity = offerTotalLength,
                listOf(
                    TextResource.Arg.IntArg(offerTotalLength),
                    TextResource.Arg.StringArg(getOfferTypeString(context))
                )
            ).format(context.resources)
        }
    }

    data class MonthlySinglePayment(
        override val offerType: OfferType,
        override val productDetails: ProductDetailsWrapper.IntroductoryOfferProduct
    ) : IntroOfferType() {
        override fun getNotificationTitle(context: Context): String {
            return context.getString(
                R.string.action_item_title_fixed_percent_discount,
                productDetails.computeDiscountPercentage(getLocate(context)),
                getOfferTypeString(context)
            )
        }

        override fun getNotificationDescription(context: Context): String {
            val offerUnitFormattedPrice = offerUnitPriceValue.toFormattedPrice(context)
            return TextResource.PluralsText(
                pluralsRes = R.plurals.action_item_description_single_payment_month,
                quantity = offerLength,
                listOf(
                    TextResource.Arg.IntArg(offerLength),
                    TextResource.Arg.StringArg(getOfferTypeString(context)),
                    TextResource.Arg.StringArg(offerUnitFormattedPrice),
                    TextResource.Arg.StringArg(offerUnitFormattedPrice + context.getString(offerPeriodicity.suffixRes))
                )
            ).format(context.resources)
        }

        override fun getAnnouncementTitle(context: Context): String {
            return context.getString(
                R.string.intro_offers_dialog_title_single_payment,
                productDetails.computeDiscountPercentage(getLocate(context)),
                getOfferTypeString(context)
            )
        }

        override fun getAnnouncementMessage(context: Context): String {
            val offerUnitFormattedPrice = offerUnitPriceValue.toFormattedPrice(context)
            return TextResource.PluralsText(
                pluralsRes = R.plurals.intro_offers_dialog_message_single_payment_month,
                quantity = offerLength,
                listOf(
                    TextResource.Arg.IntArg(offerLength),
                    TextResource.Arg.StringArg(getOfferTypeString(context)),
                    TextResource.Arg.StringArg(offerUnitFormattedPrice),
                    TextResource.Arg.StringArg(offerUnitFormattedPrice + context.getString(offerPeriodicity.suffixRes))
                )
            ).format(context.resources)
        }
    }

    data class MonthlyRecurringPayment(
        override val offerType: OfferType,
        override val productDetails: ProductDetailsWrapper.IntroductoryOfferProduct
    ) : IntroOfferType() {
        override fun getNotificationTitle(context: Context): String {
            return context.getString(
                R.string.action_item_title_fixed_percent_discount,
                productDetails.computeDiscountPercentage(getLocate(context)),
                getOfferTypeString(context)
            )
        }

        override fun getNotificationDescription(context: Context): String {
            val offerUnitFormattedPrice =
                (offerPricing.priceMicro.toFloat() / offerLength / MICRO).toFormattedPrice(context)
            return context.getString(
                R.string.action_item_description_monthly_recurring_payment_to_monthly_plan,
                getOfferTypeString(context),
                offerUnitFormattedPrice + context.getString(offerPeriodicity.suffixRes),
                offerLength * offerPricing.cycleCount
            )
        }

        override fun getAnnouncementTitle(context: Context): String {
            return context.getString(
                R.string.intro_offers_dialog_title_percent_discount,
                productDetails.computeDiscountPercentage(getLocate(context)),
                getOfferTypeString(context)
            )
        }

        override fun getAnnouncementMessage(context: Context): String {
            val totalLength = offerLength * offerPricing.cycleCount
            val offerUnitFormattedPrice =
                (offerPricing.priceMicro.toFloat() / offerLength / MICRO).toFormattedPrice(context)
            return TextResource.PluralsText(
                pluralsRes = R.plurals.intro_offers_dialog_message_recurring_payment_month,
                quantity = totalLength,
                listOf(
                    TextResource.Arg.StringArg(getOfferTypeString(context)),
                    TextResource.Arg.StringArg(offerUnitFormattedPrice + context.getString(offerPeriodicity.suffixRes)),
                    TextResource.Arg.IntArg(totalLength)
                )
            ).format(context.resources)
        }
    }

    data class MonthlySinglePaymentToYearly(
        override val offerType: OfferType,
        override val productDetails: ProductDetailsWrapper.IntroductoryOfferProduct
    ) : IntroOfferType() {
        override fun getNotificationTitle(context: Context): String {
            return context.getString(
                R.string.action_item_title_fixed_price_discount,
                getOfferTypeString(context),
                offerPriceValue.toFormattedPrice(context)
            )
        }

        override fun getNotificationDescription(context: Context): String {
            val offerFormattedPrice = offerPriceValue.toFormattedPrice(context)
            val offerUnitFormattedPrice = offerUnitPriceValue.toFormattedPrice(context)
            return TextResource.PluralsText(
                pluralsRes = R.plurals.action_item_description_single_payment_month,
                quantity = offerLength,
                listOf(
                    TextResource.Arg.IntArg(offerLength),
                    TextResource.Arg.StringArg(getOfferTypeString(context)),
                    TextResource.Arg.StringArg(offerFormattedPrice),
                    TextResource.Arg.StringArg(offerUnitFormattedPrice + context.getString(offerPeriodicity.suffixRes))
                )
            ).format(context.resources)
        }

        override fun getAnnouncementTitle(context: Context): String {
            return context.getString(
                R.string.intro_offers_dialog_title_single_payment_month,
                getOfferTypeString(context),
                offerPriceValue.toFormattedPrice(context)
            )
        }

        override fun getAnnouncementMessage(context: Context): String {
            val offerFormattedPrice = offerPriceValue.toFormattedPrice(context)
            val offerUnitFormattedPrice = offerUnitPriceValue.toFormattedPrice(context)
            return TextResource.PluralsText(
                pluralsRes = R.plurals.intro_offers_dialog_message_single_payment_month,
                quantity = offerLength,
                args = listOf(
                    TextResource.Arg.IntArg(offerLength),
                    TextResource.Arg.StringArg(getOfferTypeString(context)),
                    TextResource.Arg.StringArg(offerFormattedPrice),
                    TextResource.Arg.StringArg(offerUnitFormattedPrice + context.getString(offerPeriodicity.suffixRes))
                )
            ).format(context.resources)
        }
    }

    data class YearlySinglePaymentToYearly(
        override val offerType: OfferType,
        override val productDetails: ProductDetailsWrapper.IntroductoryOfferProduct
    ) : IntroOfferType() {
        override fun getNotificationTitle(context: Context): String {
            return TextResource.PluralsText(
                pluralsRes = R.plurals.action_item_title_single_payment_year,
                quantity = offerLength,
                listOf(
                    TextResource.Arg.IntArg(offerLength),
                    TextResource.Arg.StringArg(getOfferTypeString(context))
                )
            ).format(context.resources)
        }

        override fun getNotificationDescription(context: Context): String {
            return TextResource.PluralsText(
                pluralsRes = R.plurals.action_item_description_single_payment_year,
                quantity = offerLength,
                listOf(
                    TextResource.Arg.IntArg(offerLength),
                    TextResource.Arg.StringArg(getOfferTypeString(context)),
                    TextResource.Arg.StringArg(offerPriceValue.toFormattedPrice(context))
                )
            ).format(context.resources)
        }

        override fun getAnnouncementTitle(context: Context): String {
            return context.getString(
                R.string.intro_offers_dialog_title_single_payment_year,
                getOfferTypeString(context),
                offerPriceValue.toFormattedPrice(context)
            )
        }

        override fun getAnnouncementMessage(context: Context): String {
            return TextResource.PluralsText(
                pluralsRes = R.plurals.intro_offers_dialog_message_single_payment_year,
                quantity = offerLength,
                args = listOf(
                    TextResource.Arg.IntArg(offerLength),
                    TextResource.Arg.StringArg(getOfferTypeString(context)),
                    TextResource.Arg.StringArg(offerPriceValue.toFormattedPrice(context))
                )
            ).format(context.resources)
        }
    }

    data class MonthlyRecurringPaymentToYearly(
        override val offerType: OfferType,
        override val productDetails: ProductDetailsWrapper.IntroductoryOfferProduct
    ) : IntroOfferType() {
        override fun getNotificationTitle(context: Context): String {
            return context.getString(
                R.string.action_item_title_fixed_price_discount,
                getOfferTypeString(context),
                offerPriceValue.toFormattedPrice(context)
            )
        }

        override fun getNotificationDescription(context: Context): String {
            return context.getString(
                R.string.action_item_description_monthly_recurring_payment_to_yearly_plan,
                getOfferTypeString(context),
                offerUnitPriceValue.toFormattedPrice(context) + context.getString(offerPeriodicity.suffixRes),
                offerLength * offerPricing.cycleCount
            )
        }

        override fun getAnnouncementTitle(context: Context): String {
            return context.getString(
                R.string.intro_offers_dialog_title_single_payment_month,
                getOfferTypeString(context),
                offerPriceValue.toFormattedPrice(context)
            )
        }

        override fun getAnnouncementMessage(context: Context): String {
            val totalLength = offerLength * offerPricing.cycleCount
            val offerFormattedPrice = offerUnitPriceValue.toFormattedPrice(context)
            return TextResource.PluralsText(
                pluralsRes = R.plurals.intro_offers_dialog_message_recurring_payment_month,
                quantity = totalLength,
                args = listOf(
                    TextResource.Arg.StringArg(getOfferTypeString(context)),
                    TextResource.Arg.StringArg(offerFormattedPrice + context.getString(offerPeriodicity.suffixRes)),
                    TextResource.Arg.IntArg(totalLength)
                )
            ).format(context.resources)
        }
    }

    data class YearlyRecurringPaymentToYearly(
        override val offerType: OfferType,
        override val productDetails: ProductDetailsWrapper.IntroductoryOfferProduct
    ) : IntroOfferType() {
        override fun getNotificationTitle(context: Context): String {
            val totalLength = offerLength * offerPricing.cycleCount
            return TextResource.PluralsText(
                pluralsRes = R.plurals.action_item_title_yearly_recurring_payment_to_yearly_plan,
                quantity = totalLength,
                listOf(
                    TextResource.Arg.IntArg(totalLength)
                )
            ).format(context.resources)
        }

        override fun getNotificationDescription(context: Context): String {
            val totalLength = offerLength * offerPricing.cycleCount
            return TextResource.PluralsText(
                pluralsRes = R.plurals.action_item_description_yearly_recurring_payment_to_yearly_plan,
                quantity = totalLength,
                args = listOf(
                    TextResource.Arg.StringArg(getOfferTypeString(context)),
                    TextResource.Arg.StringArg(
                        offerPriceValue.toFormattedPrice(context) + context.getString(
                            offerPeriodicity.suffixRes
                        )
                    ),
                    TextResource.Arg.IntArg(totalLength)
                )
            ).format(context.resources)
        }

        override fun getAnnouncementTitle(context: Context): String {
            val totalLength = offerLength * offerPricing.cycleCount
            return TextResource.PluralsText(
                pluralsRes = R.plurals.intro_offers_dialog_title_recurring_payment_year,
                quantity = totalLength,
                args = listOf(
                    TextResource.Arg.IntArg(totalLength),
                    TextResource.Arg.StringArg(getOfferTypeString(context))
                )
            ).format(context.resources)
        }

        override fun getAnnouncementMessage(context: Context): String {
            val totalLength = offerLength * offerPricing.cycleCount
            return TextResource.PluralsText(
                pluralsRes = R.plurals.intro_offers_dialog_message_recurring_payment_year,
                quantity = totalLength,
                args = listOf(
                    TextResource.Arg.StringArg(getOfferTypeString(context)),
                    TextResource.Arg.StringArg(
                        offerPriceValue
                            .toFormattedPrice(context) + context.getString(offerPeriodicity.suffixRes)
                    ),
                    TextResource.Arg.IntArg(totalLength)
                )
            ).format(context.resources)
        }
    }
}
