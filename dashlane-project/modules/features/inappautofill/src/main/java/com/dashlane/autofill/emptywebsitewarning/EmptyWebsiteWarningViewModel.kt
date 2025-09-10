package com.dashlane.autofill.emptywebsitewarning

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.autofill.api.R
import com.dashlane.autofill.emptywebsitewarning.presenter.EmptyWebsiteWarningPresenter
import com.dashlane.autofill.model.toItemToFill
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.util.tryOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmptyWebsiteWarningViewModel @Inject constructor(
    private val presenter: EmptyWebsiteWarningPresenter,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _stateFlow: MutableViewStateFlow<EmptyWebsiteWarningState.View, EmptyWebsiteWarningState.SideEffect> =
        MutableViewStateFlow(EmptyWebsiteWarningState.View.Initial)
    val stateFlow: ViewStateFlow<EmptyWebsiteWarningState.View, EmptyWebsiteWarningState.SideEffect> = _stateFlow

    private var websiteToAdd: String? = null
    private var itemId: String? = null

    fun load(websiteToAdd: String, itemId: String) {
        this.websiteToAdd = websiteToAdd
        this.itemId = itemId
        viewModelScope.launch {
            val result = presenter.getAccountWrapper(itemId, websiteToAdd) ?: return@launch
            _stateFlow.update { _ ->
                EmptyWebsiteWarningState.View.Loaded(
                    viewData = EmptyWebsiteWarningData(
                        credentialTitle = result.title,
                        credentialLogin = result.login,
                        websiteToAdd = result.websiteSuggestion,
                    ),
                )
            }
        }
    }

    fun onAddWebsiteClick() {
        viewModelScope.launch {
            val itemId = itemId ?: return@launch
            val websiteToAdd = websiteToAdd ?: return@launch
            val resultData = tryOrNull { presenter.updateAccountWithNewUrl(itemId, websiteToAdd) }
            val result = if (resultData == null) {
                EmptyWebsiteWarningState.SideEffect.OnError
            } else {
                val message = context.getString(R.string.autofill_empty_website_toast, resultData.syncObject.title)
                EmptyWebsiteWarningState.SideEffect.OnSuccess(
                    message = message,
                    itemToFill = resultData.toItemToFill(),
                )
            }
            _stateFlow.update { _ -> EmptyWebsiteWarningState.View.Hidden }
            _stateFlow.send(result)
        }
    }

    fun onDismissOrCancel() {
        viewModelScope.launch {
            _stateFlow.update { _ -> EmptyWebsiteWarningState.View.Hidden }
            _stateFlow.send(EmptyWebsiteWarningState.SideEffect.OnCancel)
        }
    }
}