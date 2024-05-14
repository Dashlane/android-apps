package com.dashlane.premium.offer.common.model

import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.dashlane.premium.R

@Keep
enum class ProductPeriodicity(@StringRes val suffixRes: Int) {
    MONTHLY(suffixRes = R.string.plans_price_billed_monthly_suffix),
    YEARLY(suffixRes = R.string.plans_price_billed_yearly_suffix);
}