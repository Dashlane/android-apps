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
    val titleFormatArgs: Array<Any>
        get() = emptyArray()
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
        private val offerType: OfferType
    ) : ActionItem {
        override val section: ActionItemSection = ActionItemSection.YOUR_ACCOUNT
        override val type: ActionItemType = ActionItemType.TRIAL_UPGRADE_RECOMMENDATION
        override val title: Int = R.string.action_item_trial_upgrade_recommendation_title
        override val description: Int
            get() = when (offerType) {
                OfferType.ADVANCED -> R.string.action_item_trial_upgrade_recommendation_description_essentials
                OfferType.PREMIUM -> R.string.action_item_trial_upgrade_recommendation_description_premium
                OfferType.FAMILY -> R.string.action_item_trial_upgrade_recommendation_description_family
            }
        override val icon: Int = R.drawable.ic_action_item_premium_related
        override val action: NotificationCenterDef.Presenter.() -> Unit = { startUpgrade(offerType = null) }
    }

    data class AuthenticatorAnnouncementItem(
        override val actionItemsRepository: NotificationCenterRepository
    ) : ActionItem {
        override val section = ActionItemSection.WHATS_NEW
        override val type = ActionItemType.AUTHENTICATOR_ANNOUNCEMENT
        override val title = R.string.authenticator_notification_center_title
        override val description = R.string.authenticator_notification_center_body
        override val icon = R.drawable.ic_action_item_authenticator
        override val action: NotificationCenterDef.Presenter.() -> Unit = { startAuthenticator() }
    }

    class ViewHolder(view: View) : ReadStateViewHolder<ActionItem>(view) {

        @Suppress("SpreadOperator")
        override fun updateView(context: Context, item: ActionItem?) {
            super.updateView(context, item)
            item ?: return
            setText(R.id.title, context.getString(item.title, *item.titleFormatArgs))
            setText(R.id.description, context.getString(item.description))
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