package com.dashlane.notificationcenter.view

import android.content.Context
import android.view.View
import com.dashlane.R
import com.dashlane.notificationcenter.NotificationCenterDef
import com.dashlane.notificationcenter.NotificationCenterRepository
import com.dashlane.premium.offer.common.model.OfferType
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.util.DiffUtilComparator
import com.dashlane.ui.drawable.CircleDrawable

interface ActionItem : NotificationItem, DiffUtilComparator<ActionItem> {
    val title: Int
    val titleFormatArgs: List<Any>
        get() = emptyList()
    val descriptionFormatArgs: List<Any>
        get() = emptyList()
    val description: Int
    val icon: Int
    override val trackingKey: String
        get() = type.trackingKey

    override fun getSpanSize(spanCount: Int): Int = spanCount

    override fun getViewType() = VIEW_TYPE

    override fun isItemTheSame(item: ActionItem) = this.type == item.type

    override fun isContentTheSame(item: ActionItem) = this == item

    companion object {
        val VIEW_TYPE = DashlaneRecyclerAdapter.ViewType<ActionItem>(
            R.layout.item_actionitem,
            ViewHolder::class.java
        )
    }

    data class PinCodeActionItem(
        override val actionItemsRepository: NotificationCenterRepository
    ) : ActionItem {
        override val type: ActionItemType = ActionItemType.PIN_CODE
        override val section: ActionItemSection = ActionItemSection.GETTING_STARTED
        override val title: Int = R.string.action_item_activate_pin_title
        override val description: Int = R.string.action_item_activate_pin_description
        override val icon: Int = R.drawable.ic_action_item_pin
        override val action: NotificationCenterDef.Presenter.() -> Unit = { startPinCodeSetup() }
    }

    data class BiometricActionItem(
        override val actionItemsRepository: NotificationCenterRepository
    ) : ActionItem {
        override val type: ActionItemType = ActionItemType.BIOMETRIC
        override val section: ActionItemSection = ActionItemSection.GETTING_STARTED
        override val title: Int = R.string.action_item_activate_fingerprint_title
        override val description: Int = R.string.action_item_activate_fingerprint_description
        override val icon: Int = R.drawable.ic_action_item_fingerprint
        override val action: NotificationCenterDef.Presenter.() -> Unit = { startBiometricSetup() }
    }

    data class AutoFillActionItem(
        override val actionItemsRepository: NotificationCenterRepository
    ) : ActionItem {
        override val type: ActionItemType = ActionItemType.AUTO_FILL
        override val section: ActionItemSection = ActionItemSection.GETTING_STARTED
        override val title: Int = R.string.action_item_auto_fill_title
        override val description: Int = R.string.action_item_auto_fill_description
        override val icon: Int = R.drawable.ic_action_item_auto_fill
        override val action: NotificationCenterDef.Presenter.() -> Unit = { startOnboardingInAppLogin() }
    }

    data class ZeroPasswordActionItem(
        override val actionItemsRepository: NotificationCenterRepository
    ) : ActionItem {
        override val type: ActionItemType = ActionItemType.ZERO_PASSWORD
        override val section: ActionItemSection = ActionItemSection.GETTING_STARTED
        override val title: Int = R.string.action_item_zero_password_title
        override val description: Int = R.string.action_item_zero_password_description
        override val icon: Int = R.drawable.ic_action_item_zero_password
        override val action: NotificationCenterDef.Presenter.() -> Unit = { startAddOnePassword() }
    }

    data class BiometricRecoveryActionItem(
        override val actionItemsRepository: NotificationCenterRepository,
        private val hasBiometricLockType: Boolean
    ) : ActionItem {
        override val type: ActionItemType = ActionItemType.ACCOUNT_RECOVERY
        override val section: ActionItemSection = ActionItemSection.GETTING_STARTED
        override val title: Int = R.string.action_item_account_recovery_title
        override val description: Int = R.string.action_item_account_recovery_description
        override val icon: Int = R.drawable.ic_action_item_account_recovery
        override val action: NotificationCenterDef.Presenter.() -> Unit =
            { startBiometricRecoverySetup(hasBiometricLockType) }
    }

    data class FreeTrialStartedActionItem(
        override val actionItemsRepository: NotificationCenterRepository
    ) : ActionItem {
        override val section: ActionItemSection = ActionItemSection.YOUR_ACCOUNT
        override val type: ActionItemType = ActionItemType.FREE_TRIAL_STARTED
        override val title: Int = R.string.action_item_free_trial_started_title
        override val description: Int = R.string.action_item_free_trial_started_description
        override val icon: Int = R.drawable.ic_action_item_premium_related
        override val action: NotificationCenterDef.Presenter.() -> Unit = { startCurrentPlan() }
    }

    data class TrialUpgradeRecommendationActionItem(
        override val actionItemsRepository: NotificationCenterRepository,
    ) : ActionItem {
        override val section: ActionItemSection = ActionItemSection.YOUR_ACCOUNT
        override val type: ActionItemType = ActionItemType.TRIAL_UPGRADE_RECOMMENDATION
        override val title: Int = R.string.action_item_trial_upgrade_recommendation_title
        override val description: Int
            get() = R.string.action_item_trial_upgrade_recommendation_description_premium
        override val icon: Int = R.drawable.ic_action_item_premium_related
        override val action: NotificationCenterDef.Presenter.() -> Unit = { startUpgrade(offerType = null) }
    }

    data class PasswordLimitReachedActionItem(
        override val actionItemsRepository: NotificationCenterRepository,
        private val passwordLimit: Int
    ) : ActionItem {
        override val type: ActionItemType = ActionItemType.PASSWORD_LIMIT_REACHED
        override val section: ActionItemSection = ActionItemSection.YOUR_ACCOUNT
        override val title: Int = R.string.action_item_password_limit_reached_title
        override val description: Int = R.string.action_item_password_limit_description
        override val icon: Int = R.drawable.ic_action_item_premium_related
        override val action: NotificationCenterDef.Presenter.() -> Unit =
            { startUpgrade(offerType = OfferType.PREMIUM) }
        override val titleFormatArgs: List<Any>
            get() = listOf(passwordLimit)
    }

    data class PasswordLimitWarningActionItem(
        override val actionItemsRepository: NotificationCenterRepository,
        private val passwordCount: Int,
        private val passwordLimit: Int
    ) : ActionItem {
        override val type: ActionItemType = ActionItemType.PASSWORD_LIMIT_WARNING
        override val section: ActionItemSection = ActionItemSection.YOUR_ACCOUNT
        override val title: Int = R.string.action_item_password_limit_warning_title
        override val description: Int = R.string.action_item_password_limit_description
        override val icon: Int = R.drawable.ic_action_item_premium_related
        override val action: NotificationCenterDef.Presenter.() -> Unit =
            { startUpgrade(offerType = OfferType.PREMIUM) }
        override val titleFormatArgs: List<Any>
            get() = listOf(passwordCount, passwordLimit)
    }

    data class PasswordLimitExceededActionItem(
        override val actionItemsRepository: NotificationCenterRepository,
        private val passwordLimit: Int
    ) : ActionItem {
        override val type: ActionItemType = ActionItemType.PASSWORD_LIMIT_EXCEEDED
        override val section: ActionItemSection = ActionItemSection.YOUR_ACCOUNT
        override val title: Int = R.string.action_item_frozen_account_title
        override val description: Int = R.string.action_item_frozen_account_description
        override val icon: Int = R.drawable.ic_action_item_premium_related
        override val action: NotificationCenterDef.Presenter.() -> Unit =
            { openFrozenStatePaywall() }
        override val descriptionFormatArgs: List<Any>
            get() = listOf(passwordLimit)
    }

    class ViewHolder(view: View) : ReadStateViewHolder<ActionItem>(view) {

        @Suppress("SpreadOperator")
        override fun updateView(context: Context, item: ActionItem?) {
            super.updateView(context, item)
            item ?: return
            setText(R.id.title, context.getString(item.title, *item.titleFormatArgs.toTypedArray()))
            setText(R.id.description, context.getString(item.description, *item.descriptionFormatArgs.toTypedArray()))
            setImageDrawable(
                R.id.icon,
                CircleDrawable.with(
                    context = context,
                    backgroundColorRes = R.color.container_expressive_brand_quiet_idle,
                    drawableRes = item.icon,
                    drawableTintColorRes = R.color.text_brand_standard
                )
            )
        }
    }
}