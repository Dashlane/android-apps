package com.dashlane.createaccount.passwordless.termsandconditions

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.createaccount.passwordless.MplessAccountCreationRepository
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TermsAndConditionsViewModel @Inject constructor(
    private val mplessAccountCreationRepository: MplessAccountCreationRepository,
) : ViewModel() {
    private val _stateFlow =
        MutableViewStateFlow<TermsAndConditionsState.View, TermsAndConditionsState.SideEffect>(TermsAndConditionsState.View())
    val stateFlow: ViewStateFlow<TermsAndConditionsState.View, TermsAndConditionsState.SideEffect> = _stateFlow

    fun updateTos(isTosChecked: Boolean) {
        viewModelScope.launch {
            mplessAccountCreationRepository.updateTermsOfServicesAccepted(isTosChecked)
            _stateFlow.update { it.copy(isTosChecked = isTosChecked) }
        }
    }

    fun updateOffers(isOffersChecked: Boolean) {
        viewModelScope.launch {
            mplessAccountCreationRepository.updateOffersAccepted(isOffersChecked)
            _stateFlow.update { it.copy(isOffersChecked = isOffersChecked) }
        }
    }

    fun onCreateAccount() {
        viewModelScope.launch {
            _stateFlow.send(TermsAndConditionsState.SideEffect.Success)
        }
    }

    fun onHelp(uri: Uri) {
        viewModelScope.launch {
            _stateFlow.send(TermsAndConditionsState.SideEffect.GoToHelp(uri))
        }
    }
}