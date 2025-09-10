package com.dashlane.importlogins

import androidx.lifecycle.SavedStateHandle
import com.dashlane.importlogins.ImportLoginsState.SideEffect
import com.dashlane.importlogins.ImportLoginsState.View
import com.dashlane.udf.StateMachine
import com.dashlane.udf.UDFViewModel
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class ImportLoginsViewModel @Inject constructor(
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle,
) : UDFViewModel<View, SideEffect, ImportLoginsEvent>(
        useCases = mutableListOf(),
        defaultDispatcher = defaultDispatcher,
    ) {
    init {
        val navArgs = ImportLoginsFragmentArgs.fromSavedStateHandle(savedStateHandle)
        start(createStateMachine(showAddLoginManually = navArgs.showAddLoginManually))
    }

    internal fun createStateMachine(showAddLoginManually: Boolean) =
        StateMachine.create<View, ImportLoginsEvent, SideEffect> {
            initialState(View(showAddLoginManually = showAddLoginManually))
            state<View> {
                on<ImportLoginsEvent.GoToCsvFileImportClick> {
                    sideEffect(SideEffect.GoToCsvFileImportIntro)
                }
                on<ImportLoginsEvent.GoToDashFileImportClick> {
                    sideEffect(SideEffect.GoToDashFileImportIntro)
                }
                on<ImportLoginsEvent.GoToAddSingleLoginClick> {
                    sideEffect(SideEffect.GoToAddSingleLogin)
                }
            }
        }
}