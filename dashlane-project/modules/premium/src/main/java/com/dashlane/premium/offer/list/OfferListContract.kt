package com.dashlane.premium.offer.list

import com.dashlane.premium.offer.common.model.OfferType
import com.dashlane.premium.offer.common.model.Offers
import com.dashlane.premium.offer.common.model.OffersState
import com.skocken.presentation.definition.Base



internal interface OfferListContract {
    interface ViewProxy : Base.IView {
        

        fun showAvailableOffers(offers: Offers)

        fun showEmptyState()

        fun showProgress()

        fun hideProgress()
    }

    interface DataProvider : Base.IDataProvider {
        

        suspend fun getOffers(): OffersState
    }

    interface Presenter : Base.IPresenter {

        fun onOfferClicked(type: OfferType)

        fun onMonthlyPeriodicityClicked()

        fun onYearlyPeriodicityClicked()
    }
}