package com.dashlane.notificationcenter

import com.dashlane.notificationcenter.view.ActionItemSection
import com.dashlane.notificationcenter.view.NotificationItem
import com.dashlane.premium.offer.common.model.OfferType
import com.dashlane.security.identitydashboard.breach.BreachWrapper
import com.dashlane.session.Session
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.skocken.presentation.definition.Base
import java.time.Instant

interface NotificationCenterDef {

    interface DataProvider : Base.IDataProvider {
        val session: Session?

        suspend fun load(section: ActionItemSection): List<NotificationItem>

        suspend fun loadAll(): List<NotificationItem>

        fun markAsRead(list: List<NotificationItem>)

        fun dismiss(item: NotificationItem)

        fun undoDismiss(item: NotificationItem)

        fun getCreationDate(item: NotificationItem): Instant

        fun refreshNotificationBadge()

        suspend fun getBreachAlertCount(): Int
        fun listenForChanges()
        fun unlistenForChanges()
    }

    interface View : Base.IView {
        var items: List<NotificationItem>

        fun getDisplayedItems(): List<NotificationItem>

        fun setLoading(isLoading: Boolean)

        fun updateBreachAlertHeader(breachAlertCount: Int?)
    }

    interface Presenter : Base.IPresenter {
        fun refresh()

        fun markAsRead()

        fun dismiss(item: NotificationItem)

        fun undoDismiss(item: NotificationItem)

        fun click(item: DashlaneRecyclerAdapter.ViewTypeProvider)

        fun groupActionItemsBySection(
            list: List<NotificationItem>,
            displaySectionHeader: Boolean,
            breachAlertCount: Int?
        ): List<DashlaneRecyclerAdapter.MultiColumnViewTypeProvider>

        fun startOnboardingInAppLogin()

        fun startAddOnePassword()

        fun startSectionDetails(section: ActionItemSection)

        fun startAlertDetails(breachWrapper: BreachWrapper)

        fun startSharingRedirection()

        fun startPinCodeSetup()

        fun startBiometricSetup()

        fun startBiometricRecoverySetup(hasBiometricLockType: Boolean)

        fun startGoEssentials()

        fun startCurrentPlan()

        fun startUpgrade(offerType: OfferType? = null)

        fun startAuthenticator()
    }
}
