package com.dashlane.notificationcenter

import com.dashlane.events.AppEvents
import com.dashlane.events.BreachStatusChangedEvent
import com.dashlane.events.clearLastEvent
import com.dashlane.events.register
import com.dashlane.events.unregister
import com.dashlane.notification.badge.NotificationBadgeActor
import com.dashlane.notificationcenter.view.ActionItemSection
import com.dashlane.notificationcenter.view.NotificationItem
import com.dashlane.security.identitydashboard.breach.BreachLoader
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.skocken.presentation.provider.BaseDataProvider
import java.lang.ref.WeakReference
import javax.inject.Inject

class NotificationCenterDataProvider @Inject constructor(
    private val sessionManager: SessionManager,
    private val repository: NotificationCenterRepository,
    private val notificationBadgeActor: NotificationBadgeActor,
    private val breachLoader: BreachLoader,
    private val appEvents: AppEvents
) : NotificationCenterDef.DataProvider,
    BaseDataProvider<NotificationCenterDef.Presenter>() {

    private val appEventListener = AppEventsListener(appEvents, this)

    override val session: Session?
        get() = sessionManager.session

    override suspend fun loadAll(): List<NotificationItem> = repository.loadAll()

    override suspend fun load(section: ActionItemSection): List<NotificationItem> =
        repository.load(section)

    override fun markAsRead(list: List<NotificationItem>) {
        list.forEach { item ->
            repository.markAsRead(item)
        }
        refreshNotificationBadge()
    }

    override fun dismiss(item: NotificationItem) {
        repository.markDismissed(item, true)
        refreshNotificationBadge()
    }

    override fun undoDismiss(item: NotificationItem) {
        repository.markDismissed(item, false)
        refreshNotificationBadge()
    }

    override fun getCreationDate(item: NotificationItem) = repository.getOrInitCreationDate(item)

    override fun refreshNotificationBadge() {
        notificationBadgeActor.refresh()
    }

    override suspend fun getBreachAlertCount(): Int {
        return breachLoader.getBreachesWrapper().count()
    }

    override fun listenForChanges() {
        appEventListener.listen()
    }

    override fun unlistenForChanges() {
        appEventListener.unlisten()
    }

    private fun refreshUI() {
        presenter.refresh()
    }

    class AppEventsListener(
        private val appEvents: AppEvents,
        dataProvider: NotificationCenterDataProvider
    ) {
        private val dataProviderRef = WeakReference(dataProvider)

        fun listen() {
            appEvents.register<BreachStatusChangedEvent>(this, true) {
                dataProviderRef.get()?.apply {
                    appEvents.clearLastEvent<BreachStatusChangedEvent>()
                    refreshUI()
                } ?: appEvents.unregister<BreachStatusChangedEvent>(this)
            }
        }

        fun unlisten() {
            runCatching {
                appEvents.unregister<BreachStatusChangedEvent>(this)
            }
        }
    }
}