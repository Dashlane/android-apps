package com.dashlane.premium.offer.common

interface OffersFromAutofillResolver {
    fun isFromAutofill(origin: String): Boolean
}