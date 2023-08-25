package com.dashlane.item.subview.view

import android.content.Context
import androidx.appcompat.widget.SwitchCompat
import com.dashlane.R
import com.dashlane.util.getThemeAttrResourceId

object SwitchInputProvider {

    fun create(
        context: Context,
        header: String,
        description: String?,
        value: Boolean,
        editable: Boolean = false,
        switchAction: (Boolean) -> Unit = { _ -> }
    ): SwitchCompat {
        return SwitchCompat(context).apply {
            isFocusable = editable
            isActivated = editable
            isClickable = editable
            text = header
            setSwitchTextAppearance(context, context.getThemeAttrResourceId(R.attr.textAppearanceSubtitle1))
            description?.let {
                hint = it
            }
            isChecked = value

            if (editable) {
                setOnCheckedChangeListener { _, value ->
                    switchAction.invoke(value)
                }
            } else {
                setOnCheckedChangeListener(null)
            }
        }
    }
}