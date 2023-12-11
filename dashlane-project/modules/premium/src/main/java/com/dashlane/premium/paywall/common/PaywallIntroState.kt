package com.dashlane.premium.paywall.common

import androidx.annotation.StringRes
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.premium.offer.common.model.OfferType
import com.dashlane.ui.activities.intro.DescriptionItem
import com.dashlane.ui.activities.intro.LinkItem

data class PaywallIntroState(
    @get:StringRes
    val title: Int,
    val descriptionList: List<DescriptionItem>,
    val linkList: List<LinkItem>,
    val destinationOfferType: OfferType,
    val page: AnyPage,
    @get:StringRes
    val goToOfferCTA: Int
)
