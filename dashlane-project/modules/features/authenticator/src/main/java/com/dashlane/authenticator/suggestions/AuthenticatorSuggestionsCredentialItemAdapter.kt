package com.dashlane.authenticator.suggestions

import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsUiState.HasLogins.CredentialItem
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.util.getThemeAttrDrawable
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

class AuthenticatorSuggestionsCredentialItemAdapter : DashlaneRecyclerAdapter<CredentialItem>() {

    override fun onBindViewHolder(viewHolder: EfficientViewHolder<CredentialItem>, position: Int) {
        val view = viewHolder.view
        view.foreground = view.context.getThemeAttrDrawable(android.R.attr.selectableItemBackground)
        super.onBindViewHolder(viewHolder, position)
    }
}
