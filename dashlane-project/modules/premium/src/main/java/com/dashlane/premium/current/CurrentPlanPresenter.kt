package com.dashlane.premium.current

import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.FragmentActivity
import com.dashlane.navigation.Navigator
import com.dashlane.premium.R
import com.dashlane.premium.current.model.CurrentPlan
import com.dashlane.premium.current.model.CurrentPlan.Action.Type.ALL_PLANS
import com.dashlane.premium.current.model.CurrentPlan.Action.Type.CLOSE
import com.dashlane.premium.current.model.CurrentPlan.Action.Type.FAMILY
import com.dashlane.premium.current.model.CurrentPlan.Action.Type.PREMIUM
import com.dashlane.premium.current.model.CurrentPlanType
import com.dashlane.premium.current.model.CurrentPlanType.B2B
import com.dashlane.premium.current.model.CurrentPlanType.FAMILY_ADMIN
import com.dashlane.premium.current.model.CurrentPlanType.FAMILY_INVITEE
import com.dashlane.premium.current.model.CurrentPlanType.FAMILY_PLUS_ADMIN
import com.dashlane.premium.current.model.CurrentPlanType.FAMILY_PLUS_INVITEE
import com.dashlane.premium.current.model.CurrentPlanType.FREE
import com.dashlane.premium.current.model.CurrentPlanType.LEGACY
import com.dashlane.premium.current.model.CurrentPlanType.PREMIUM_FREE_FOR_LIFE
import com.dashlane.premium.current.model.CurrentPlanType.PREMIUM_FREE_OF_CHARGE
import com.dashlane.premium.current.model.CurrentPlanType.PREMIUM_PLUS
import com.dashlane.premium.current.model.CurrentPlanType.TRIAL
import com.dashlane.premium.current.model.CurrentPlanType.UNKNOWN
import com.dashlane.premium.current.ui.CurrentBenefitItem
import com.dashlane.premium.current.ui.DarkWebMonitoringBottomSheetDialogFragment
import com.dashlane.premium.offer.common.model.OfferType
import com.dashlane.ui.model.TextResource
import com.skocken.presentation.presenter.BasePresenter

internal class CurrentPlanPresenter(
    private val navigator: Navigator,
    private val logger: CurrentPlanLogger
) : CurrentPlanContract.Presenter,
    BasePresenter<CurrentPlanContract.DataProvider, CurrentPlanContract.ViewProxy>() {

    private val fragmentActivity
        get() = activity as FragmentActivity

    private var currentPlan: CurrentPlan? = null
        set(value) {
            if (field != value) {
                field = value
                field?.let {
                    view.showCurrentPlan(it)
                    logger.showCurrentPlan(primaryAction = it.primaryAction, secondaryAction = it.secondaryAction)
                }
            }
        }

    init {
        logger.initPresenter(this)
    }

    override fun refresh() {
        currentPlan = fetchCurrentPlan()
    }

    @VisibleForTesting
    fun fetchCurrentPlan(): CurrentPlan {
        val benefits = provider.getBenefits()
        val type = provider.getType()
        val title = getTitle(type)
        val (suggestion, cta) = getSuggestedTextAndAction(type, provider.isVpnAllowed())
        return CurrentPlan(
            title = title,
            benefits = benefits,
            suggestion = suggestion,
            primaryAction = cta.first,
            secondaryAction = cta.second
        )
    }

    override fun onActionClicked(actionType: CurrentPlan.Action.Type) = when (actionType) {
        ALL_PLANS -> openOffer(null)
        PREMIUM -> openOffer(OfferType.PREMIUM)
        FAMILY -> openOffer(OfferType.FAMILY)
        CLOSE -> onCloseClicked()
    }.also { logger.onActionClicked(actionType) }

    override fun onItemClicked(item: CurrentBenefitItem) {
        item.benefit.action?.let { it(this) }
    }

    override fun openDarkWebMonitoringInfo() {
        val supportFragmentManager = fragmentActivity.supportFragmentManager
        var dialog = supportFragmentManager.findFragmentByTag(DarkWebMonitoringBottomSheetDialogFragment.DIALOG_TAG)
        if (dialog == null) {
            dialog = DarkWebMonitoringBottomSheetDialogFragment.newInstance()
            dialog.logger = logger
            dialog.show(supportFragmentManager, DarkWebMonitoringBottomSheetDialogFragment.DIALOG_TAG)
        }
    }

    override fun onDestroy() {
        if (activity?.isFinishing == true) {
            logger.onCurrentPlanFinishing()
        }
    }

    private fun openOffer(offerType: OfferType?) {
        navigator.goToOffers(offerType = offerType?.toString())
    }

    private fun onCloseClicked() {
        activity?.finish()
    }

    private fun getTitle(type: CurrentPlanType) = when (type) {
        LEGACY -> buildTitle(R.string.current_plan_title_legacy_1, R.string.current_plan_title_legacy_2)
        FREE -> buildTitle(R.string.current_plan_title_free)
        TRIAL -> TextResource.StringText(R.string.current_plan_title_trial)
        CurrentPlanType.ADVANCED -> buildTitle(R.string.plans_advanced_title)
        CurrentPlanType.ESSENTIALS -> buildTitle(R.string.current_plan_title_essentials)
        PREMIUM_FREE_FOR_LIFE ->
            buildTitle(R.string.current_plan_title_premium, R.string.current_plan_title_free_for_life)
        PREMIUM_FREE_OF_CHARGE ->
            buildTitle(R.string.current_plan_title_premium, R.string.current_plan_title_free_of_charge)
        CurrentPlanType.PREMIUM -> buildTitle(R.string.current_plan_title_premium)
        PREMIUM_PLUS -> buildTitle(R.string.current_plan_title_premium_plus)
        FAMILY_ADMIN,
        FAMILY_INVITEE -> buildTitle(R.string.current_plan_title_family)
        FAMILY_PLUS_ADMIN,
        FAMILY_PLUS_INVITEE -> buildTitle(R.string.current_plan_title_family_plus)
        B2B,
        UNKNOWN -> TextResource.StringText(R.string.current_plan_title_fallback)
    }

    private fun buildTitle(@StringRes typeRes: Int, @StringRes extraRes: Int? = null) =
        TextResource.StringText(
            R.string.current_plan_title_with_args,
            TextResource.Arg.StringResArg(typeRes),
            extraRes?.let { TextResource.Arg.StringResArg(it) } ?: TextResource.Arg.StringArg("")
        )

    private fun getSuggestedTextAndAction(
        type: CurrentPlanType,
        vpnAllowed: Boolean
    ): Pair<CurrentPlan.Suggestion?, Pair<CurrentPlan.Action, CurrentPlan.Action?>> = when (type) {
        TRIAL -> {
            CurrentPlan.Suggestion(
                title = TextResource.StringText(R.string.current_plan_suggestion_trial_title),
                text = TextResource.StringText(R.string.current_plan_suggestion_trial_text)
            ) to buildCta(primaryCta = ALL_PLANS, secondaryCta = CLOSE)
        }
        FREE -> {
            CurrentPlan.Suggestion(
                title = TextResource.StringText(R.string.current_plan_suggestion_upgrade_to_premium_from_free_title),
                text = TextResource.StringText(R.string.current_plan_suggestion_upgrade_to_premium_from_free_text)
            ) to buildCta(secondaryCta = PREMIUM)
        }
        CurrentPlanType.ADVANCED,
        CurrentPlanType.ESSENTIALS -> {
            CurrentPlan.Suggestion(
                title = TextResource.StringText(R.string.current_plan_suggestion_upgrade_to_premium_title),
                text = TextResource.StringText(R.string.current_plan_suggestion_upgrade_to_premium_text)
                    .takeIf { vpnAllowed }
                    ?: TextResource.StringText(R.string.current_plan_suggestion_upgrade_to_premium_text_with_no_vpn_mention)
            ) to buildCta(secondaryCta = PREMIUM)
        }
        LEGACY -> {
            (CurrentPlan.Suggestion(
                title = TextResource.StringText(R.string.current_plan_suggestion_upgrade_to_premium_title),
                text = TextResource.StringText(R.string.current_plan_suggestion_upgrade_to_premium_from_legacy_text)
            ) to buildCta(secondaryCta = PREMIUM)).takeIf { vpnAllowed } ?: (null to buildCta(null))
        }
        PREMIUM_FREE_OF_CHARGE -> {
            (CurrentPlan.Suggestion(
                title = TextResource.StringText(R.string.current_plan_suggestion_upgrade_to_paid_premium_title),
                text = TextResource.StringText(R.string.current_plan_suggestion_upgrade_to_paid_premium_text)
            ) to buildCta(secondaryCta = PREMIUM)).takeIf { vpnAllowed } ?: (null to buildCta(null))
        }
        CurrentPlanType.PREMIUM -> {
            CurrentPlan.Suggestion(
                title = TextResource.StringText(R.string.current_plan_suggestion_upgrade_to_family_title),
                text = TextResource.StringText(R.string.current_plan_suggestion_upgrade_to_family_text)
            ) to buildCta(secondaryCta = FAMILY)
        }
        FAMILY_ADMIN,
        FAMILY_PLUS_ADMIN -> {
            CurrentPlan.Suggestion(
                title = null,
                text = TextResource.StringText(R.string.current_plan_suggestion_family_dashboard_text)
            ) to buildCta(secondaryCta = null)
        }
        B2B,
        PREMIUM_FREE_FOR_LIFE,
        FAMILY_PLUS_INVITEE,
        FAMILY_INVITEE,
        PREMIUM_PLUS -> null to buildCta(secondaryCta = null)
        UNKNOWN -> null to buildCta(secondaryCta = ALL_PLANS)
    }

    private fun buildCta(
        secondaryCta: CurrentPlan.Action.Type?,
        primaryCta: CurrentPlan.Action.Type = CLOSE
    ) = primaryCta.toAction() to secondaryCta?.toAction()

    private fun CurrentPlan.Action.Type.toAction() = when (this) {
        ALL_PLANS -> CurrentPlan.Action(type = this, label = R.string.current_plan_cta_all_plans)
        PREMIUM -> CurrentPlan.Action(type = this, label = R.string.current_plan_cta_premium)
        FAMILY -> CurrentPlan.Action(type = this, label = R.string.current_plan_cta_family)
        CLOSE -> CurrentPlan.Action(type = this, label = R.string.current_plan_cta_close)
    }
}