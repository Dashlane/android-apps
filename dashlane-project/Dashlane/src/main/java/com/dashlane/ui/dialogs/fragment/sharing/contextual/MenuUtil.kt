package com.dashlane.ui.dialogs.fragment.sharing.contextual

import android.content.Context
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.iterator
import com.dashlane.R

internal fun PopupMenu.setupContextualMenuForCollections(
    context: Context,
    isItemInCollection: Boolean
) {
    val disabledColor = context.getColor(R.color.text_oddity_disabled)
    menu.iterator().forEach {
        it.isEnabled = !isItemInCollection
        if (!it.isEnabled) {
            it.title = SpannableString(it.title).apply {
                setSpan(ForegroundColorSpan(disabledColor), 0, length, 0)
            }
        }
    }
}