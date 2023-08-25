package com.dashlane.authenticator.suggestions

import android.os.Parcelable
import com.dashlane.authenticator.AuthenticatorBaseViewModelContract.Companion.DEFAULT_ITEMS_SHOWN
import com.dashlane.authenticator.R
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

sealed class AuthenticatorSuggestionsUiState {
    object Progress : AuthenticatorSuggestionsUiState()

    object NoLogins : AuthenticatorSuggestionsUiState()

    object AllSetup : AuthenticatorSuggestionsUiState()

    object SetupComplete : AuthenticatorSuggestionsUiState()

    data class HasLogins(
        val logins: List<CredentialItem>,
        val nbItemsShown: Int = DEFAULT_ITEMS_SHOWN
    ) : AuthenticatorSuggestionsUiState() {
        @Parcelize
        data class CredentialItem(
            val id: String,
            val title: String?,
            val domain: String?,
            val username: String?,
            val packageName: String? = null,
            val professional: Boolean = false
        ) : DashlaneRecyclerAdapter.ViewTypeProvider, Parcelable {
            @IgnoredOnParcel
            var layout = R.layout.authenticator_credential_item
            override fun getViewType() = DashlaneRecyclerAdapter.ViewType(
                layout,
                AuthenticatorSuggestionsCredentialItemViewHolder::class.java
            )
        }
    }
}