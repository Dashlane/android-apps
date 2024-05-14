package com.dashlane.premium.offer.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.dashlane.premium.R
import com.dashlane.premium.offer.common.model.Offers
import com.dashlane.premium.offer.list.model.OfferOverview
import com.dashlane.premium.offer.list.view.OfferCardWidget
import com.dashlane.util.animation.fadeOut
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.skocken.presentation.viewproxy.BaseViewProxy

internal class OfferListViewProxy(view: View) :
    BaseViewProxy<OfferListContract.Presenter>(view),
    OfferListContract.ViewProxy {

    private val loader = view.findViewById(R.id.offer_list_progress) as ProgressBar
    private val idealStateView = view.findViewById(R.id.offer_list_ideal_state) as ViewGroup
    private val emptyStateView = view.findViewById(R.id.offer_list_empty_state) as ViewGroup

    private val periodicityTab = view.findViewById(R.id.offer_list_periodicity_tab_layout) as TabLayout
    private val periodicityPager = view.findViewById(R.id.offer_list_view_pager) as ViewPager2

    override fun showProgress() {
        loader.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        loader.fadeOut()
    }

    override fun showEmptyState() {
        idealStateView.visibility = View.GONE
        emptyStateView.visibility = View.VISIBLE
    }

    override fun showAvailableOffers(offers: Offers) {
        val monthlyOffersCards = buildOfferScreen(
            title = context.getString(R.string.plans_periodicity_toggle_monthly),
            offers = offers.monthlyOffers
        ) { presenterOrNull?.onMonthlyPeriodicityClicked() }
        val yearlyOffersCards = buildOfferScreen(
            title = context.getString(R.string.plans_periodicity_toggle_yearly),
            offers = offers.yearlyOffers
        ) { presenterOrNull?.onYearlyPeriodicityClicked() }

        val offersPerPeriodicity = listOfNotNull(monthlyOffersCards, yearlyOffersCards)
        periodicityPager.adapter = PeriodicityPagerAdapter(offersPerPeriodicity)
        periodicityPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    (periodicityPager.adapter as PeriodicityPagerAdapter).offersPerPeriodicity[position].onDisplay()
                }
            }
        )
        if (offersPerPeriodicity.size == 1) {
            periodicityTab.visibility = View.GONE
        } else {
            periodicityTab.visibility = View.VISIBLE
        }
        TabLayoutMediator(periodicityTab, periodicityPager) { tab, position ->
            val screen = (periodicityPager.adapter as PeriodicityPagerAdapter).offersPerPeriodicity[position]
            tab.setCustomView(R.layout.offer_list_tab_item)
            tab.customView?.findViewById<TextView>(R.id.tab_title)?.text = screen.title
        }.attach()
    }

    private fun buildOfferScreen(
        title: String,
        offers: List<OfferOverview>,
        onDisplay: () -> Unit
    ): PeriodicityPagerAdapter.OffersScreen? = if (offers.isNotEmpty()) {
        PeriodicityPagerAdapter.OffersScreen(
            title = title,
            offers = offers.map { buildOfferCardView(it) },
            onDisplay = onDisplay
        )
    } else {
        null
    }

    private fun buildOfferCardView(offer: OfferOverview) =
        OfferCardWidget(context).apply {
            title = context.getString(offer.title)
            description = context.getString(offer.description)
            if (offer is OfferOverview.IntroductoryOffer) {
                barredText = offer.pricing?.getBarredText(context)
                billedPrice = offer.pricing?.getPriceText(context)
                offerCallOut = offer.discountCallOut.formattedText(context)
                additionalInfo = offer.pricing?.getAdditionalInfoText(context)
            } else {
                billedPrice = offer.pricing?.getPriceText(context)
            }
            onGoingLabel = offer.onGoingRes?.let { context.getString(it) }
            setOnClickListener {
                presenterOrNull?.onOfferClicked(offer.type)
            }
        }

    class PeriodicityPagerAdapter(
        val offersPerPeriodicity: List<OffersScreen>
    ) : RecyclerView.Adapter<PeriodicityViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeriodicityViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.offer_list_content, parent, false)
            return PeriodicityViewHolder(view)
        }

        override fun onBindViewHolder(holder: PeriodicityViewHolder, position: Int) {
            offersPerPeriodicity[position].offers.forEach { offer ->
                holder.linearLayout.addView(offer)
            }
        }

        override fun getItemCount(): Int = offersPerPeriodicity.size

        data class OffersScreen(val title: String, val offers: List<OfferCardWidget>, val onDisplay: () -> Unit)
    }

    class PeriodicityViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {
        val linearLayout: LinearLayout = itemView.findViewById(R.id.offer_list_linear_layout)
    }
}
