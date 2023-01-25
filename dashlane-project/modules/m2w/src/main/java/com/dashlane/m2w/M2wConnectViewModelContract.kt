package com.dashlane.m2w

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

interface M2wConnectViewModelContract {
    val showConfirmPopupFlow: MutableStateFlow<Boolean>
    val finishM2W: MutableSharedFlow<Boolean>
    fun onConfirmSuccess()
    fun onCancelConfirmPopup()
    fun onNext()
    fun onConfirmationDialogDismissed()
}