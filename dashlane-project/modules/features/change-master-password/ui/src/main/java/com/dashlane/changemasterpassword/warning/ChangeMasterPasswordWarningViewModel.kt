package com.dashlane.changemasterpassword.warning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.changemasterpassword.ChangeMasterPasswordOrigin
import com.dashlane.changemasterpassword.ui.R
import com.dashlane.changemasterpassword.warning.ChangeMasterPasswordWarningState.SideEffect
import com.dashlane.changemasterpassword.warning.ChangeMasterPasswordWarningState.View
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.server.api.endpoints.premium.PremiumStatus.PremiumCapability.Capability
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeMasterPasswordWarningViewModel @Inject constructor(
    private val userFeatureChecker: UserFeaturesChecker,
) : ViewModel() {
    private val _stateFlow = MutableViewStateFlow<View, SideEffect>(View())
    val stateFlow: ViewStateFlow<View, SideEffect> = _stateFlow

    fun viewStarted(origin: ChangeMasterPasswordOrigin) {
        viewModelScope.launch {
            _stateFlow.update { state ->
                when {
                    origin is ChangeMasterPasswordOrigin.Migration -> {
                        state.copy(
                            title = R.string.master_password_user_migration_warning_title,
                            description = R.string.master_password_user_migration_warning_message,
                            primaryButtonText = R.string.master_password_user_migration_warning_cta_positive,
                            secondaryButtonText = R.string.master_password_user_migration_warning_cta_negative,
                            isMigrationToMP = true,
                        )
                    }
                    userFeatureChecker.has(Capability.SYNC) -> state.copy(
                        title = R.string.change_mp_warning_desktop_title,
                        description = R.string.change_mp_warning_desktop_description,
                    )
                    else -> state.copy(
                        title = R.string.change_mp_warning_desktop_nosync_title,
                        description = R.string.change_mp_warning_desktop_nosync_description,
                    )
                }
            }
        }
    }

    fun next() {
        viewModelScope.launch {
            _stateFlow.send(SideEffect.GoToChangeMP)
        }
    }

    fun cancel() {
        viewModelScope.launch {
            if (stateFlow.viewState.value.isMigrationToMP) {
                _stateFlow.send(SideEffect.Logout)
            } else {
                _stateFlow.send(SideEffect.Cancel)
            }
        }
    }
}