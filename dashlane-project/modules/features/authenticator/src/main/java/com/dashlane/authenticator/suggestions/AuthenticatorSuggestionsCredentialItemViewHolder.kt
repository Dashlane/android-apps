package com.dashlane.authenticator.suggestions

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dashlane.authenticator.R
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsUiState.HasLogins.CredentialItem
import com.dashlane.util.graphics.RemoteImageRoundRectDrawable
import com.dashlane.util.graphics.RoundRectDrawable
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

class AuthenticatorSuggestionsCredentialItemViewHolder(v: View) :
    EfficientViewHolder<CredentialItem>(v) {
    private val name = findViewByIdEfficient<TextView>(R.id.authenticator_credential_item_name)!!
    private val login = findViewByIdEfficient<TextView>(R.id.authenticator_credential_item_login)!!
    private val logo = findViewByIdEfficient<ImageView>(R.id.authenticator_credential_item_icon)!!
    private val drawable = RemoteImageRoundRectDrawable(v.context, Color.WHITE).also {
        it.setPreferImageBackgroundColor(true)
    }

    override fun updateView(context: Context, item: CredentialItem?) {
        item ?: return
        name.text = item.title
        login.text = item.username
        logo.setImageDrawable(drawable)
        try {
            drawable.loadImage(item.domain, RoundRectDrawable(context, Color.WHITE))
        } catch (e: IllegalArgumentException) {
            
        }
    }
}