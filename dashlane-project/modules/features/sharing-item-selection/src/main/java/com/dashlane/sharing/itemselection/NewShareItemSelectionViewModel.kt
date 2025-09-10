package com.dashlane.sharing.itemselection

import com.dashlane.sharing.itemselection.NewShareItemSelectionEvent.ItemClicked
import com.dashlane.sharing.itemselection.NewShareItemSelectionEvent.NewShareClicked
import com.dashlane.sharing.itemselection.NewShareItemSelectionEvent.QueryChanged
import com.dashlane.sharing.itemselection.NewShareItemSelectionEvent.ShareableItemsLoaded
import com.dashlane.sharing.itemselection.NewShareItemSelectionEvent.UnlockCancelled
import com.dashlane.sharing.itemselection.NewShareItemSelectionEvent.UnlockRequired
import com.dashlane.sharing.itemselection.NewShareItemSelectionEvent.ViewStarted
import com.dashlane.sharing.itemselection.NewShareItemSelectionState.SideEffect
import com.dashlane.sharing.itemselection.NewShareItemSelectionState.SideEffect.GoToNewShare
import com.dashlane.sharing.itemselection.NewShareItemSelectionState.SideEffect.MPLock
import com.dashlane.sharing.itemselection.NewShareItemSelectionState.View
import com.dashlane.sharing.itemselection.NewShareItemSelectionState.View.DisplayItemList
import com.dashlane.sharing.itemselection.NewShareItemSelectionState.View.Initial
import com.dashlane.sharing.itemselection.NewShareItemSelectionState.View.Loading
import com.dashlane.sharing.itemselection.NewShareItemSelectionState.View.ToggleItem
import com.dashlane.sharing.itemselection.NewShareItemSelectionState.View.UnlockProtectedItems
import com.dashlane.udf.StateMachine
import com.dashlane.udf.UDFViewModel
import com.dashlane.udf.UseCase
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class NewShareItemSelectionViewModel @Inject constructor(
    useCases: MutableList<UseCase<View, NewShareItemSelectionEvent>>,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : UDFViewModel<View, SideEffect, NewShareItemSelectionEvent>(
        useCases = useCases,
        defaultDispatcher = defaultDispatcher,
    ) {
    init {
        start(
            createStateMachine(
                initialState = Initial(
                    NewShareItemSelectionData(query = ""),
                ),
            ),
        )
    }

    @Suppress("LongMethod")
    private fun createStateMachine(initialState: View) =
        StateMachine.create<View, NewShareItemSelectionEvent, SideEffect> {
            initialState(initialState)
            state<Initial> {
                on<ViewStarted> {
                    transitionTo(Loading(data))
                }
                on<QueryChanged> { event ->
                    transitionTo(Loading(data.copy(query = event.query)))
                }
            }
            state<Loading> {
                on<ShareableItemsLoaded> { event ->
                    transitionTo(
                        DisplayItemList(
                            data.copy(
                                displayList = event.shareableItems,
                                selectedItemCounts = event.selectedItemCount,
                                showFooter = event.selectedItemCount > 0,
                            ),
                        ),
                    )
                }
                on<QueryChanged> { event ->
                    transitionTo(Loading(data.copy(query = event.query)))
                }
            }
            state<ToggleItem> {
                on<ShareableItemsLoaded> { event ->
                    transitionTo(
                        DisplayItemList(
                            data.copy(
                                displayList = event.shareableItems,
                                selectedItemCounts = event.selectedItemCount,
                                showFooter = event.selectedItemCount > 0,
                            ),
                        ),
                    )
                }
            }
            state<DisplayItemList> {
                on<QueryChanged> { event ->
                    transitionTo(Loading(data.copy(query = event.query)))
                }
                on<ItemClicked> { event ->
                    transitionTo(
                        ToggleItem(event.item.id, data),
                    )
                }
                on<NewShareClicked> {
                    transitionTo(
                        UnlockProtectedItems(data),
                    )
                }
            }
            state<UnlockProtectedItems> {
                on<QueryChanged> { event ->
                    transitionTo(Loading(data.copy(query = event.query)))
                }
                on<UnlockRequired> {
                    sideEffect(MPLock)
                }
                on<NewShareItemSelectionEvent.Proceed> { event ->
                    transitionTo(
                        DisplayItemList(data),
                        GoToNewShare(
                            selectedPasswords = event.selectedPasswords,
                            selectedNotes = event.selectedNotes,
                            selectedSecrets = event.selectedSecrets,
                        ),
                    )
                }
                on<UnlockCancelled> {
                    transitionTo(DisplayItemList(data))
                }
            }
        }

    companion object {
        const val UNLOCK_EVENT_CODE = 84271
    }
}