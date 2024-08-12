package com.dashlane.authenticator.suggestions

import android.content.Context
import android.view.View
import android.widget.TextView
import com.dashlane.authenticator.R
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsUiState.HasLogins.CredentialItem
import com.dashlane.ui.thumbnail.ThumbnailDomainIconView
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

class AuthenticatorSuggestionsCredentialItemViewHolder(v: View) :
    EfficientViewHolder<CredentialItem>(v) {
    private val name = findViewByIdEfficient<TextView>(R.id.authenticator_credential_item_name)!!
    private val login = findViewByIdEfficient<TextView>(R.id.authenticator_credential_item_login)!!
    private val logo = findViewByIdEfficient<ThumbnailDomainIconView>(R.id.authenticator_credential_item_icon)!!

    override fun updateView(context: Context, item: CredentialItem?) {
        item ?: return
        name.text = item.title
        login.text = item.username
        logo.domainUrl = item.domain
    }
}