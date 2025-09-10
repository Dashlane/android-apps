package com.dashlane.notification.badge

import com.dashlane.notificationcenter.NotificationCenterRepository
import com.dashlane.util.inject.OptionalProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationBadgeFlow @Inject constructor(
    private val sessionCoroutineScopeProvider: OptionalProvider<CoroutineScope>,
    private val sharingInvitationRepository: SharingInvitationRepository,
    private val actionItemsRepository: NotificationCenterRepository,
) {
    private val _resultFlow = MutableSharedFlow<Result>(replay = 1)
    val resultFlow: SharedFlow<Result> = _resultFlow.asSharedFlow()

    private var lastResult: Result = Result()

    fun refresh() {
        sessionCoroutineScopeProvider.get()?.launch {
            val hasSharingDeferred = async { sharingInvitationRepository.hasInvitations() }
            val hasUnReadActionItemsDeferred = async { actionItemsRepository.hasAtLeastOneUnRead() }
            val newResult = Result(
                hasSharing = hasSharingDeferred.await(),
                hasUnReadActionItems = hasUnReadActionItemsDeferred.await(),
            )
            if (newResult != _resultFlow.replayCache.firstOrNull()) {
                _resultFlow.emit(newResult)
                lastResult = newResult
            }
        }
    }
}

data class Result(
    val hasSharing: Boolean = false,
    val hasActionItems: Boolean = false,
    val hasUnReadActionItems: Boolean = false,
)