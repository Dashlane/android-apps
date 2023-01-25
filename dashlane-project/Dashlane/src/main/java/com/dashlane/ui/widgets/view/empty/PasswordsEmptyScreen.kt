package com.dashlane.ui.widgets.view.empty

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import com.dashlane.R

object PasswordsEmptyScreen {
    fun newInstance(context: Context, alignTop: Boolean): EmptyScreenViewProvider {
        return EmptyScreenViewProvider(
            EmptyScreenConfiguration.Builder()
                .setImage(AppCompatResources.getDrawable(context, R.drawable.ic_empty_password))
                .setLine1(context.getString(R.string.import_methods_empty_state_title))
                .setLine2(context.getString(R.string.import_methods_empty_state_description))
                .setAlignTop(alignTop)
                .build()
        )
    }
}