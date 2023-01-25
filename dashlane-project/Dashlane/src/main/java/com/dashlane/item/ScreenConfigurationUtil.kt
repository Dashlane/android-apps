@file:JvmName("ScreenConfigurationUtil")

package com.dashlane.item

import android.os.Bundle
import androidx.core.net.toUri
import com.dashlane.authenticator.Otp
import com.dashlane.authenticator.Totp
import com.dashlane.authenticator.UriParser
import com.dashlane.item.subview.ItemSubViewWithActionWrapper
import com.dashlane.item.subview.action.ItemEditMenuAction
import com.dashlane.item.subview.action.note.SecureNoteCategoryMenuAction
import com.dashlane.item.subview.action.note.SecureNoteColorMenuAction
import com.dashlane.item.subview.action.payment.CreditCardColorMenuAction
import com.dashlane.item.subview.edit.ItemAuthenticatorEditSubView
import com.dashlane.item.subview.edit.ItemEditPasswordWithStrengthSubView
import com.dashlane.item.subview.edit.ItemEditSpaceSubView
import com.dashlane.item.subview.edit.ItemEditValueBooleanSubView
import com.dashlane.item.subview.edit.ItemEditValueDateSubView
import com.dashlane.item.subview.edit.ItemEditValueListSubView
import com.dashlane.item.subview.edit.ItemEditValueNumberSubView
import com.dashlane.item.subview.edit.ItemEditValueRawSubView
import com.dashlane.item.subview.edit.ItemEditValueTextSubView
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.util.isValueNull
import com.dashlane.util.logE
import com.dashlane.util.tryOrNull
import com.dashlane.xml.SyncObjectEnum
import com.dashlane.xml.domain.SyncObject
import java.time.LocalDate



fun ScreenConfiguration.restoreState(bundle: Bundle, teamspaceAccessor: TeamspaceAccessor) {
    bundle.getStringArray("itemMenuActions")?.let {
        restoreMenuActions(it)
    }
    bundle.getStringArray("itemSubviews")?.let {
        restoreSubViews(it, teamspaceAccessor)
    }
}



fun ScreenConfiguration.toBundle(): Bundle {

    val bundle = Bundle()
    itemHeader?.menuActions?.filterIsInstance<ItemEditMenuAction>()?.let { list ->
        bundle.putStringArray("itemMenuActions", list.map {
            when (it) {
                is SecureNoteCategoryMenuAction -> it.selectedCategory ?: "null"
                is SecureNoteColorMenuAction -> it.selectedType.name
                is CreditCardColorMenuAction -> it.selectedColor.value
                else -> "null"
            }
        }.toTypedArray())
    }

    val subviewValues = itemSubViews.map {
        val value = if (it is ItemSubViewWithActionWrapper) {
            it.itemSubView.value
        } else {
            it.value
        }
        when (value) {
            is Teamspace -> value.teamId
            is LocalDate -> value.toString()
            is Otp -> value.url ?: value.secret
            else -> value?.toString() ?: "null"
        }
    }.toTypedArray()

    bundle.putStringArray("itemSubviews", subviewValues)

    return bundle
}

private fun ScreenConfiguration.restoreSubViews(valuesToRestore: Array<String>, teamspaceAccessor: TeamspaceAccessor) {
    if (valuesToRestore.size != itemSubViews.size) {
        
        logE { "Saved ItemSubViews values can't be restored, subViews count has changed" }
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
            is ItemEditPasswordWithStrengthSubView -> subview.notifyValueChanged(value)
            is ItemEditValueBooleanSubView -> subview.notifyValueChanged(value.toBoolean())
            is ItemEditValueListSubView -> subview.notifyValueChanged(value)
            is ItemEditValueTextSubView -> subview.notifyValueChanged(value)
            is ItemEditValueNumberSubView -> subview.notifyValueChanged(value)
            is ItemEditSpaceSubView -> {
                teamspaceAccessor.all.firstOrNull { it.teamId == value }?.let {
                    subview.notifyValueChanged(it)
                }
            }
            is ItemEditValueRawSubView -> subview.notifyValueChanged(value)
            is ItemAuthenticatorEditSubView -> {
                val otp = tryOrNull { UriParser.parse(value.toUri()) }
                if (otp != null) {
                    subview.notifyValueChanged(otp)
                } else {
                    subview.notifyValueChanged(Totp(secret = value))
                }
            }
            else -> logE { "Can't restore item type: ${subview::class.java}" }
        }
    }
}

private fun ScreenConfiguration.restoreMenuActions(menuActionsValuesToRestore: Array<String>) {
    val menuActions = itemHeader?.menuActions?.filterIsInstance<ItemEditMenuAction>()
    if (menuActionsValuesToRestore.size == menuActions?.size) {
        for (i in 0..menuActionsValuesToRestore.lastIndex) {
            val value = menuActionsValuesToRestore[i]
            if (value.isValueNull()) continue
            when (val menuAction = menuActions[i]) {
                is SecureNoteCategoryMenuAction -> menuAction.selectedCategory = value
                is SecureNoteColorMenuAction -> menuAction.selectedType =
                    SyncObjectEnum.getEnumForValue(value) ?: SyncObject.SecureNoteType.NO_TYPE
                is CreditCardColorMenuAction -> menuAction.selectedColor =
                    SyncObjectEnum.getEnumForValue(value) ?: SyncObject.PaymentCreditCard.Color.NO_TYPE
                else -> logE { "Can't restore ItemEditMenuAction type: ${menuAction::class.java}" }
            }
        }
    } else {
        
        logE { "Saved ItemEditMenuAction values can't be restored, ItemEditMenuAction count has changed" }
    }
}