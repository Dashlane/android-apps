package com.dashlane.core.premium

import com.dashlane.autofill.api.util.AutofillNavigationService
import com.dashlane.premium.offer.common.OffersFromAutofillResolver
import javax.inject.Inject

class OffersFromAutofillResolverImpl @Inject constructor() : OffersFromAutofillResolver {
    override fun isFromAutofill(origin: String) =
        origin.contains(AutofillNavigationService.ORIGIN_PASSWORD_LIMIT)
}