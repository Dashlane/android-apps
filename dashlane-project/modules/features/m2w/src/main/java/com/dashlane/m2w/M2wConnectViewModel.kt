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

    override val finishM2W = MutableSharedFlow<M2WResult>(extraBufferCapacity = 1)

    override fun onNext() {
        showConfirmPopupFlow.tryEmit(true)
    }

    override fun onSkip() = finishM2w(M2WResult.SKIPPED)

    override fun onConfirmSuccess() = finishM2w(M2WResult.COMPLETED)

    override fun onCancelConfirmPopup() = Unit

    override fun onConfirmationDialogDismissed() {
        showConfirmPopupFlow.tryEmit(false)
    }

    fun finishM2w(result: M2WResult) {
        if (result == M2WResult.COMPLETED || result == M2WResult.SKIPPED) {
            userPreferencesManager.hasFinishedM2D = true
        }
        finishM2W.tryEmit(result)
    }
}