package com.dashlane.autofill.viewallaccounts.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.autofill.viewallaccounts.AutofillSearch
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@HiltViewModel
class ViewAllItemsViewModel @Inject constructor(
    private val autofillSearch: AutofillSearch,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val stateFlow = MutableStateFlow<ViewAllItemsState>(ViewAllItemsState.Initial)
    val uiState = stateFlow.asStateFlow()
    private var filterJob: Job? = null

    fun filterCredentials(query: String) {
        filterJob?.cancel()
        filterJob = flow {
            val credentials = autofillSearch.loadAuthentifiants()
            val result = autofillSearch.matchAuthentifiantsFromQuery(query, credentials)
            emit(result)
        }
            .flowOn(ioDispatcher)
            .catch {
                stateFlow.emit(ViewAllItemsState.Error(it.message ?: "Unknown error"))
            }
            .onStart { stateFlow.emit(ViewAllItemsState.Loading) }
            .onEach { stateFlow.emit(ViewAllItemsState.Loaded(it, query)) }
            .launchIn(viewModelScope)
    }

    fun selectedCredential(
        wrapperItem: AuthentifiantSearchViewTypeProviderFactory.AuthentifiantWrapperItem
    ) {
        viewModelScope.launch(ioDispatcher) {
            val result = autofillSearch.loadAuthentifiant(wrapperItem.getAuthentifiant().id)
            if (result == null) {
                stateFlow.emit(ViewAllItemsState.Error("Selected credential not found"))
            } else {
                stateFlow.emit(ViewAllItemsState.Selected(result, wrapperItem.itemListContext))
            }
        }
    }
}
