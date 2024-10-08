package com.dashlane.notification.badge

import com.dashlane.notificationcenter.NotificationCenterRepository
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.MainCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationBadgeActor @Inject constructor(
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    @MainCoroutineDispatcher
    private val mainCoroutineDispatcher: CoroutineDispatcher,
    private val sharingInvitationRepository: SharingInvitationRepository,
    private val actionItemsRepository: NotificationCenterRepository
) {
    @Suppress("EXPERIMENTAL_API_USAGE")
    private val resultBroadcastChannel = BroadcastChannel<Result>(capacity = Channel.CONFLATED)

    @Suppress("EXPERIMENTAL_API_USAGE")
    private val lastResult: Result
        get() = openStateSubscription().consume { tryReceive().getOrNull() } ?: Result()

    val hasUnread: Boolean
        get() = hasSharing || hasUnReadActionItems

    val hasSharing: Boolean
        get() = lastResult.hasSharing

    val hasUnReadActionItems: Boolean
        get() = lastResult.hasUnReadActionItems

    @Suppress("EXPERIMENTAL_API_USAGE")
    private val actor =
        applicationCoroutineScope.actor<Unit>(context = defaultCoroutineDispatcher, capacity = Channel.CONFLATED) {
            consumeEach {
                val hasSharingDeferred = async { sharingInvitationRepository.hasInvitations() }
                val hasUnReadActionItemsDeferred = async { actionItemsRepository.hasAtLeastOneUnRead() }
                val newResult = Result(
                    hasSharing = hasSharingDeferred.await(),
                    hasUnReadActionItems = hasUnReadActionItemsDeferred.await()
                )
                broadcastIfNeeded(newResult)
            }
        }

    fun refresh() {
        actor.trySend(Unit)
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    fun subscribe(coroutineScope: CoroutineScope, listener: NotificationBadgeListener) {
        coroutineScope.launch(mainCoroutineDispatcher) {
            openStateSubscription().consumeEach {
                listener.onNotificationBadgeUpdated()
            }
        }
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    private fun broadcastIfNeeded(result: Result) {
        if (result != lastResult) {
            resultBroadcastChannel.trySend(result)
        }
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    private fun openStateSubscription() = resultBroadcastChannel.openSubscription()
}

private data class Result(
    val hasSharing: Boolean = false,
    val hasActionItems: Boolean = false,
    val hasUnReadActionItems: Boolean = false
)
