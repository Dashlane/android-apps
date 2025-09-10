package com.dashlane.autofill.smsotp

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.autofill.api.R
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.google.android.gms.auth.api.phone.SmsCodeRetriever
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class SmsOtpAutofillViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _stateFlow: MutableViewStateFlow<SmsOtpState.View, SmsOtpState.SideEffect> =
        MutableViewStateFlow(SmsOtpState.View.Initial)
    val stateFlow: ViewStateFlow<SmsOtpState.View, SmsOtpState.SideEffect> = _stateFlow

    fun onFetchSmsCode() {
        viewModelScope.launch {
            _stateFlow.update { previousState ->
                when (previousState) {
                    SmsOtpState.View.Initial -> SmsOtpState.View.Shown.Loading
                    is SmsOtpState.View.Shown -> previousState
                    SmsOtpState.View.Hidden -> previousState
                }
            }
        }
    }

    fun onFetchSmsCodeFailure(exception: Exception) {
        viewModelScope.launch {
            _stateFlow.update {
                SmsOtpState.View.Shown.Error(message = exception.message ?: "?")
            }
        }
    }

    fun onSmsCodeResult(extras: Bundle?) {
        val smsCode = extras?.getString(SmsCodeRetriever.EXTRA_SMS_CODE)?.takeUnless { it.isBlank() }
        if (smsCode == null) {
            viewModelScope.launch {
                _stateFlow.update {
                    SmsOtpState.View.Shown.Error(message = context.getString(R.string.autofill_sms_otp_dialog_error_detail_timeout))
                }
            }
        } else {
            viewModelScope.launch {
                _stateFlow.update {
                    SmsOtpState.View.Shown.DisplayCode(smsCode = smsCode)
                }
            }
        }
    }

    fun onAutofillClick(smsCode: String) {
        viewModelScope.launch {
            _stateFlow.update {
                SmsOtpState.View.Hidden
            }
            _stateFlow.send(SmsOtpState.SideEffect.OnSuccess(smsCode = smsCode))
        }
    }

    fun onDismiss() {
        viewModelScope.launch {
            _stateFlow.update {
                SmsOtpState.View.Hidden
            }
            _stateFlow.send(SmsOtpState.SideEffect.OnCancel)
        }
    }
}