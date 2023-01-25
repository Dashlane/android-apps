package com.dashlane.authenticator.dashboard

import android.net.Uri
import android.os.Parcelable
import com.dashlane.authenticator.AuthenticatorBaseViewModelContract.Companion.DEFAULT_ITEMS_SHOWN
import com.dashlane.authenticator.Otp
import com.dashlane.authenticator.R
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import kotlinx.parcelize.Parcelize

sealed class AuthenticatorDashboardUiState {
    

    object Progress : AuthenticatorDashboardUiState()

    

    object NoOtp : AuthenticatorDashboardUiState()

    

    data class HasLogins(
        val logins: List<CredentialItem>,
        val nbItemsShown: Int = DEFAULT_ITEMS_SHOWN
    ) : AuthenticatorDashboardUiState() {
        @Parcelize
        data class CredentialItem(
            val id: String,
            val title: String,
            val domain: String?,
            val username: String?,
            val professional: Boolean,
            val otp: Otp,
            var expanded: Boolean = false,
            var editMode: Boolean = false
        ) : DashlaneRecyclerAdapter.ViewTypeProvider, Parcelable {
            override fun getViewType() = DashlaneRecyclerAdapter.ViewType(
                R.layout.authenticator_credential_item_otp,
                AuthenticatorDashboardCredentialItemViewHolder::class.java
            )
        }
    }

    

    data class HandleUri(val otpUri: Uri) : AuthenticatorDashboardUiState()
}