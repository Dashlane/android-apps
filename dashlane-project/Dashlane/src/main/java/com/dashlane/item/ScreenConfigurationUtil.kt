@file:JvmName("ScreenConfigurationUtil")

package com.dashlane.item

import android.os.Bundle
import com.dashlane.authenticator.Otp
import com.dashlane.item.subview.ItemSubViewWithActionWrapper
import com.dashlane.item.subview.edit.ItemEditSpaceSubView
import com.dashlane.item.subview.edit.ItemEditValueBooleanSubView
import com.dashlane.item.subview.edit.ItemEditValueDateSubView
import com.dashlane.item.subview.edit.ItemEditValueListSubView
import com.dashlane.item.subview.edit.ItemEditValueNumberSubView
import com.dashlane.item.subview.edit.ItemEditValueTextSubView
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.util.isValueNull
import java.time.LocalDate

fun ScreenConfiguration.restoreState(bundle: Bundle, teamSpaceAccessor: TeamSpaceAccessor) {
    bundle.getStringArray("itemSubviews")?.let { values ->
        restoreSubViews(values, teamSpaceAccessor)
    }
}

fun ScreenConfiguration.toBundle(): Bundle {
    val bundle = Bundle()
    val subviewValues = itemSubViews.map {
        val value = if (it is ItemSubViewWithActionWrapper) {
            it.itemSubView.value
        } else {
            it.value
        }
        when (value) {
            is TeamSpace -> value.teamId
            is LocalDate -> value.toString()
            is Otp -> value.url ?: value.secret
            else -> value?.toString() ?: "null"
        }
    }.toTypedArray()

    bundle.putStringArray("itemSubviews", subviewValues)
    return bundle
}

private fun ScreenConfiguration.restoreSubViews(
    valuesToRestore: Array<String>,
    teamSpaceAccessor: TeamSpaceAccessor,
) {
    if (valuesToRestore.size != itemSubViews.size) {
        
        return
    }
    for (i in 0..valuesToRestore.lastIndex) {
        val value = valuesToRestore[i]
        if (value.isValueNull()) continue
        
        val subview = if (itemSubViews[i] is ItemSubViewWithActionWrapper) {
            (itemSubViews[i] as ItemSubViewWithActionWrapper).itemSubView
        } else {
            itemSubViews[i]
        }
        when (subview) {
            is ItemEditValueDateSubView -> subview.notifyValueChanged(LocalDate.parse(value))
            is ItemEditValueBooleanSubView -> subview.notifyValueChanged(value.toBoolean())
            is ItemEditValueListSubView -> subview.notifyValueChanged(value)
            is ItemEditValueTextSubView -> subview.notifyValueChanged(value)
            is ItemEditValueNumberSubView -> subview.notifyValueChanged(value)
            is ItemEditSpaceSubView -> {
                teamSpaceAccessor.availableSpaces.firstOrNull { it.teamId == value }?.let {
                    subview.notifyValueChanged(it)
                }
            }

            else -> {
            }
        }
    }
}