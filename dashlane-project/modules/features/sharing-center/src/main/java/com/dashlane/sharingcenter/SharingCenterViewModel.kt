package com.dashlane.sharingcenter

import com.dashlane.sharingcenter.SharingCenterEvent.Loaded
import com.dashlane.sharingcenter.SharingCenterEvent.ViewStarted
import com.dashlane.sharingcenter.SharingCenterState.SideEffect
import com.dashlane.sharingcenter.SharingCenterState.View
import com.dashlane.sharingcenter.SharingCenterState.View.Initial
import com.dashlane.sharingcenter.SharingCenterState.View.Loading
import com.dashlane.udf.StateMachine
import com.dashlane.udf.UDFViewModel
import com.dashlane.udf.UseCase
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class SharingCenterViewModel @Inject constructor(
    useCases: MutableList<UseCase<View, SharingCenterEvent>>,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : UDFViewModel<View, SideEffect, SharingCenterEvent>(
        useCases = useCases,
        defaultDispatcher = defaultDispatcher,
    ) {
    init {
        start(
            createStateMachine(
                initialState = Initial(
                    peopleData = SharingCenterPeopleData(),
                    pendingRequestData = SharingCenterPendingRequestData(),
                    contentData = SharingCenterContentData(),
                ),
            ),
        )
    }

    @Suppress("LongMethod")
    private fun createStateMachine(initialState: View) = StateMachine.create<View, SharingCenterEvent, SideEffect> {
        initialState(initialState)
        state<Initial> {
            on<ViewStarted> {
                transitionTo(Loading(peopleData, pendingRequestData, contentData))
            }
        }
        state<Loading> {
            on<Loaded> { event ->
                transitionTo(
                    View.Display(
                        peopleData = event.peopleData,
                        pendingRequestData = event.pendingRequestData,
                        contentData = event.contentData,
                    ),
                )
            }
        }
        state<View.Display> {
            on<Loaded> { event ->
                transitionTo(
                    View.Display(
                        peopleData = event.peopleData,
                        pendingRequestData = event.pendingRequestData,
                        contentData = event.contentData,
                    ),
                )
            }
            on<SharingCenterEvent.PendingRequestEvent.AcceptCollectionInviteEvent> {
                transitionTo(
                    View.ProcessPendingInviteResponse.ProcessPendingCollectionInviteAccept(
                        peopleData = peopleData,
                        pendingRequestData = pendingRequestData,
                        contentData = contentData,
                        collectionInvite = it.invite,
                    ),
                )
            }

            on<SharingCenterEvent.PendingRequestEvent.AcceptGroupInviteEvent> {
                transitionTo(
                    View.ProcessPendingInviteResponse.ProcessPendingGroupInviteAccept(
                        peopleData = peopleData,
                        pendingRequestData = pendingRequestData,
                        contentData = contentData,
                        groupInvite = it.invite,
                    ),
                )
            }
            on<SharingCenterEvent.PendingRequestEvent.AcceptItemInviteEvent> {
                transitionTo(
                    View.ProcessPendingInviteResponse.ProcessPendingItemInviteAccept(
                        peopleData = peopleData,
                        pendingRequestData = pendingRequestData,
                        contentData = contentData,
                        itemInvite = it.invite,
                        title = it.title,
                    ),
                )
            }
            on<SharingCenterEvent.PendingRequestEvent.DeclineCollectionInviteEvent> {
                transitionTo(
                    View.ProcessPendingInviteResponse.ProcessPendingCollectionInviteDecline(
                        peopleData = peopleData,
                        pendingRequestData = pendingRequestData,
                        contentData = contentData,
                        collectionInvite = it.invite,
                    ),
                )
            }
            on<SharingCenterEvent.PendingRequestEvent.DeclineGroupInviteEvent> {
                transitionTo(
                    View.ProcessPendingInviteResponse.ProcessPendingGroupInviteDecline(
                        peopleData = peopleData,
                        pendingRequestData = pendingRequestData,
                        contentData = contentData,
                        groupInvite = it.invite,
                    ),
                )
            }
            on<SharingCenterEvent.PendingRequestEvent.DeclineItemInviteEvent> {
                transitionTo(
                    View.ProcessPendingInviteResponse.ProcessPendingItemInviteDecline(
                        peopleData = peopleData,
                        pendingRequestData = pendingRequestData,
                        contentData = contentData,
                        itemInvite = it.invite,
                        title = it.title,
                    ),
                )
            }
        }
        state<View.ProcessPendingInviteResponse> {
            on<SharingCenterEvent.PendingRequestResponseFailure> {
                transitionTo(
                    View.Display(
                        peopleData = peopleData,
                        pendingRequestData = pendingRequestData,
                        contentData = contentData,
                    ),
                )
            }
            on<SharingCenterEvent.PendingRequestResponseSuccess> {
                transitionTo(
                    Loading(
                        peopleData = peopleData,
                        pendingRequestData = pendingRequestData,
                        contentData = contentData,
                    ),
                    SideEffect.ShowToast(it.message),
                )
            }
        }
    }
}