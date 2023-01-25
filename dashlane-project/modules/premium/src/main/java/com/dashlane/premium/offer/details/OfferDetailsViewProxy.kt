package com.dashlane.premium.offer.details

import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.toSpanned
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dashlane.Legal
import com.dashlane.premium.R
import com.dashlane.premium.offer.common.model.OfferDetails
import com.dashlane.premium.offer.details.view.BenefitItem
import com.dashlane.premium.offer.details.view.MiddleDivider
import com.dashlane.premium.offer.details.view.WarningItem
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.model.TextResource
import com.dashlane.util.OnClickSpan
import com.dashlane.util.animation.fadeOut
import com.dashlane.util.launchUrl
import com.dashlane.util.setCurrentPageView
import com.dashlane.util.setOnFirst
import kotlinx.coroutines.launch

internal class OfferDetailsViewProxy(
    val fragment: Fragment,
    val viewModel: OfferDetailsViewModelContract,
    view: View
) {
    private val loader = view.findViewById(R.id.offer_details_progress) as ProgressBar

    private val adapter = DashlaneRecyclerAdapter<DashlaneRecyclerAdapter.ViewTypeProvider>()
    private val bottomSheet = view.findViewById(R.id.offer_details_bottom_sheet) as ConstraintLayout
    private val monthlyCta = view.findViewById(R.id.offer_details_monthly_cta) as Button
    private val yearlyCta = view.findViewById(R.id.offer_details_yearly_cta) as Button
    private val monthlyFlow = view.findViewById(R.id.offer_details_monthly_group) as Flow
    private val yearlyFlow = view.findViewById(R.id.offer_details_yearly_group) as Flow
    private val monthlyPricingInfo = view.findViewById(R.id.offer_details_monthly_cta_additional_info) as TextView
    private val yearlyPricingInfo = view.findViewById(R.id.offer_details_yearly_cta_additional_info) as TextView
    private val disclaimerView = view.findViewById(R.id.offer_details_disclaimer) as TextView

    private val context get() = fragment.requireContext()
    private val activity = fragment.requireActivity()
    private val navController = NavHostFragment.findNavController(fragment)

    init {
        val benefitsRecyclerView = view.findViewById(R.id.offer_details_benefits_recyclerview) as RecyclerView
        benefitsRecyclerView.apply {
            layoutManager = LinearLayoutManager(benefitsRecyclerView.context)
            adapter = this@OfferDetailsViewProxy.adapter
            addItemDecoration(MiddleDivider(resources, R.dimen.spacing_extra_small))
        }

        setupToolbar(activity as AppCompatActivity, viewModel.titleResId)

        fragment.lifecycleScope.launch {
            viewModel.currentPageViewFlow.collect { (page, fromAutofill) ->
                activity.setCurrentPageView(page, fromAutofill)
            }
        }
        fragment.lifecycleScope.launch {
            viewModel.showProgressFlow.collect {
                if (it) showProgress() else hideProgress()
            }
        }
        fragment.lifecycleScope.launch {
            viewModel.offerDetailsFlow.collect {
                if (it == null) {
                    
                    navController.popBackStack()
                } else {
                    showOfferDetails(it)
                }
            }
        }
    }

    private fun showProgress() {
        loader.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        if (loader.isVisible) loader.fadeOut()
    }

    private fun setupToolbar(activity: AppCompatActivity, titleRes: Int?) {
        activity.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            titleRes?.let {
                title = context.getString(titleRes)
            }
        }
    }

    

    private fun showOfferDetails(offerDetails: OfferDetails) {
        setItems(warning = offerDetails.warning, benefits = offerDetails.benefits)
        setProducts(offerDetails)
    }

    private fun setItems(warning: TextResource?, benefits: List<TextResource>) {
        val items = sequence {
            warning?.let { yield(WarningItem(content = it)) }
            yieldAll(benefits.map { BenefitItem(it) })
        }.toList()
        adapter.populateItems(items)
    }

    private fun setProducts(offerDetails: OfferDetails) {
        if (offerDetails.monthlyProduct == null && offerDetails.yearlyProduct == null) {
            bottomSheet.visibility = View.GONE
        } else {
            setCTA(
                monthlyFlow,
                monthlyCta,
                monthlyPricingInfo,
                offerDetails.monthlyProduct,
                offerDetails.monthlyProduct?.priceInfo?.getMonthlyInfoString(context)
            )
            setCTA(
                yearlyFlow,
                yearlyCta,
                yearlyPricingInfo,
                offerDetails.yearlyProduct,
                offerDetails.yearlyProduct?.priceInfo?.getYearlyInfoString(context)
            )

            setDisclaimer()
            bottomSheet.visibility = View.VISIBLE
        }
    }

    private fun setCTA(
        flow: Flow,
        button: Button,
        pricingInfoView: TextView,
        product: OfferDetails.Product?,
        pricingInfo: String?
    ) {
        if (product == null) {
            flow.visibility = View.GONE
            button.visibility = View.GONE
            pricingInfoView.visibility = View.GONE
        } else {
            val priceInfo = product.priceInfo
            button.apply {
                text = priceInfo.getCtaString(context)
                isEnabled = product.enabled
                setOnClickListener {
                    viewModel.onInAppPurchaseStarted(product)
                    fragment.lifecycleScope.launch {
                        val serviceConnection = viewModel.getBillingServiceConnection() ?: return@launch
                        val serviceResult = serviceConnection.startPurchaseFlow(
                            activity = activity,
                            productDetails = product.productDetails.originalProductDetails,
                            offerToken = product.priceInfo.subscriptionOfferToken,
                            updateReference = product.update
                        )
                        val billingServiceResult =
                            viewModel.onBillingServiceResult(serviceResult, product)
                        if (billingServiceResult != null) {
                            
                            val (purchase, userLockedOut) = billingServiceResult
                            viewModel.purchaseCheckingCoordinator.openPlayStorePurchaseChecking(
                                context = context,
                                sku = product.productId,
                                currencyCode = priceInfo.currencyCode,
                                price = priceInfo.baseOfferPriceValue,
                                purchaseOriginalJson = purchase.originalJson,
                                signature = purchase.signature,
                                userLockedOut = userLockedOut
                            )
                            activity.finish()
                        }
                    }
                }
            }
            flow.visibility = View.VISIBLE
            button.visibility = View.VISIBLE
            pricingInfo?.let {
                pricingInfoView.text = it
                pricingInfoView.visibility = View.VISIBLE
            }
        }
    }

    private fun setDisclaimer() {
        val privacyPolicy = context.getString(R.string.plan_disclaimer_legal_arg_privacy_policy)
        val termsOfService = context.getString(R.string.plan_disclaimer_legal_arg_terms_of_service)

        val disclaimer = context.getString(R.string.plan_disclaimer_default, privacyPolicy, termsOfService)

        val spanned = SpannableStringBuilder(disclaimer).apply {
            setOnFirst(privacyPolicy, OnClickSpan { context.launchUrl(Legal.URL_PRIVACY_POLICY) })
            setOnFirst(termsOfService, OnClickSpan { context.launchUrl(Legal.URL_TERMS_OF_SERVICE) })
        }.toSpanned()
        disclaimerView.text = spanned
        disclaimerView.movementMethod = LinkMovementMethod.getInstance()
    }
}