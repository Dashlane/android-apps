package com.dashlane.premium.offer.details

import com.dashlane.ui.model.TextResource

interface ConflictingBillingPlatformProvider {
    fun getWarning(): TextResource?
}