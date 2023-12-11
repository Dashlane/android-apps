package com.dashlane.m2w

import androidx.lifecycle.ViewModel
import com.dashlane.preference.UserPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
internal class M2wConnectViewModel @Inject constructor(
    val userPreferencesManager: UserPreferencesManager
) : ViewModel(), M2wConnectViewModelContract {

    override val showConfirmPopupFlow = MutableStateFlow(false)

    override val finishM2W = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)

    override fun onNext() {
        showConfirmPopupFlow.tryEmit(true)
    }

    override fun onConfirmSuccess() {
        finishM2w(true)
    }

    override fun onCancelConfirmPopup() = Unit

    override fun onConfirmationDialogDismissed() {
        showConfirmPopupFlow.tryEmit(false)
    }

    fun finishM2w(success: Boolean) {
        if (success) {
            userPreferencesManager.hasFinishedM2D = true
        }
        finishM2W.tryEmit(success)
    }
}