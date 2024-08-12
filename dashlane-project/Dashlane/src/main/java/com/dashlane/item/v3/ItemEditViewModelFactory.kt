package com.dashlane.item.v3

import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.dashlane.item.v3.viewmodels.CredentialItemEditViewModel
import com.dashlane.item.v3.viewmodels.ItemEditViewModel
import com.dashlane.xml.domain.SyncObjectXmlName.AUTHENTIFIANT

class ItemEditViewModelFactory(private val fragment: ItemEditFragment) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val navArgs = ItemEditFragmentArgs.fromSavedStateHandle(extras.createSavedStateHandle())
        val dataType = navArgs.dataType
        return when (dataType) {
            AUTHENTIFIANT -> fragment.viewModels<CredentialItemEditViewModel>()
            else -> throw IllegalArgumentException("No ViewModel exists for $dataType")
        }.value as T
    }
}