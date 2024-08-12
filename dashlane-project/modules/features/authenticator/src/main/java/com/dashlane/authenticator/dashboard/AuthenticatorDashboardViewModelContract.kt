package com.dashlane.authenticator.dashboard

import android.net.Uri
import com.dashlane.authenticator.AuthenticatorBaseViewModelContract
import com.dashlane.authenticator.Otp
import com.dashlane.authenticator.util.SetUpAuthenticatorResultContract
import kotlinx.coroutines.flow.Flow

interface AuthenticatorDashboardViewModelContract : AuthenticatorBaseViewModelContract {
    val uiState: Flow<AuthenticatorDashboardUiState>
    val editState: Flow<AuthenticatorDashboardEditState>
    fun onOtpRemoved(itemId: String)
    fun onOtpCounterUpdate(itemId: String, otp: Otp)
    fun onEditClicked()
    fun onBackToViewMode()
    fun onOtpCodeCopy(itemId: String, domain: String?): Boolean
    fun onSetupAuthenticatorFromUri(
        otpUri: Uri,
        setUpAuthenticatorResultContract: SetUpAuthenticatorResultContract
    )
}