package com.dashlane.item.subview.provider

import android.content.Context
import com.dashlane.R
import com.dashlane.item.subview.ItemSubView
import com.dashlane.item.subview.ValueChangeManager
import com.dashlane.item.subview.edit.ItemEditValueListSubView
import com.dashlane.item.subview.readonly.ItemReadValueListSubView
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.getLabel
import com.dashlane.vault.model.labels
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.copy
import com.dashlane.xml.domain.utils.Country

fun createCountryField(
    context: Context,
    item: VaultItem<*>,
    editMode: Boolean,
    valueChangedListener: ValueChangeManager.Listener<String> = object : ValueChangeManager.Listener<String> {
        override fun onValueChanged(origin: Any, newValue: String) {
            
        }
    }
): ItemSubView<*> {
    val countryHeader = context.getString(R.string.country)
    val allCountryList = Country.labels(context)
    val selectedCountry = item.syncObject.localeFormat?.getLabel(context) ?: ""
    return when {
        editMode -> ItemEditValueListSubView(
            countryHeader,
            selectedCountry,
            allCountryList
        ) { it, value -> it.copyForUpdatedCountry(context, value) }.apply {
            addValueChangedListener(valueChangedListener)
        }
        else -> ItemReadValueListSubView(countryHeader, selectedCountry, allCountryList)
    }
}

private fun VaultItem<SyncObject>.copyForUpdatedCountry(context: Context, value: String): VaultItem<*> {
    val lang = getCountryIsoCode(context, value)
    return if (lang == syncObject.localeFormat) {
        this
    } else {
        this.copy(syncObject = syncObject.copy { localeFormat = lang })
    }
}

fun getCountryIsoCode(context: Context, label: String?): Country =
    Country.values().firstOrNull { it.getLabel(context) == label } ?: Country.UnitedStates