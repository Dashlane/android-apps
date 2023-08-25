package com.dashlane.m2w

import androidx.lifecycle.ViewModel
import com.dashlane.preference.UserPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
internal class M2wConnectViewModel @Inject constructor(
    val logger: M2wConnectLogger,
    val userPreferencesManager: UserPreferencesManager
) : ViewModel(), M2wConnectViewModelContract {

    override val showConfirmPopupFlow = MutableStateFlow(false)

    override val finishM2W = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)

    override fun onNext() {
        logger.logDone()
        showConfirmPopupFlow.tryEmit(true)
    }

    override fun onConfirmSuccess() {
        logger.logConfirmPopupYes()
        finishM2w(true)
    }

    override fun onCancelConfirmPopup() {
        logger.logConfirmPopupNo()
    }

    override fun onConfirmationDialogDismissed() {
        showConfirmPopupFlow.tryEmit(false)
    }

    fun finishM2w(success: Boolean) {
        if (!success) {
            logger.logExit()
        } else {
            userPreferencesManager.hasFinishedM2D = true
        }
        finishM2W.tryEmit(success)
    }
}