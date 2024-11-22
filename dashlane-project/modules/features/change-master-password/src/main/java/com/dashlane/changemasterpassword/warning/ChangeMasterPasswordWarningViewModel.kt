package com.dashlane.changemasterpassword.warning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.changemasterpassword.R
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.server.api.endpoints.premium.PremiumStatus.PremiumCapability.Capability
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ChangeMasterPasswordWarningViewModel @Inject constructor(
    private val userFeatureChecker: UserFeaturesChecker
) : ViewModel() {

    private val _stateFlow =
        MutableViewStateFlow<ChangeMasterPasswordWarningState.View, ChangeMasterPasswordWarningState.SideEffect>(ChangeMasterPasswordWarningState.View())
    val stateFlow: ViewStateFlow<ChangeMasterPasswordWarningState.View, ChangeMasterPasswordWarningState.SideEffect> = _stateFlow

    fun viewStarted() {
        viewModelScope.launch {
            _stateFlow.update { state ->
                when {
                    userFeatureChecker.has(Capability.SYNC) -> state.copy(
                        title = R.string.change_mp_warning_desktop_title,
                        description = R.string.change_mp_warning_desktop_description
                    )
                    else -> state.copy(
                        title = R.string.change_mp_warning_desktop_nosync_title,
                        description = R.string.change_mp_warning_desktop_nosync_description
                    )
                }
            }
        }
    }

    fun next() {
        viewModelScope.launch {
            _stateFlow.send(ChangeMasterPasswordWarningState.SideEffect.GoToChangeMP)
        }
    }

    fun cancel() {
        viewModelScope.launch {
            _stateFlow.send(ChangeMasterPasswordWarningState.SideEffect.Cancel)
        }
    }
}