package com.dashlane.item.v3

import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.dashlane.item.v3.itemtype.creditcard.CreditCardItemEditViewModel
import com.dashlane.item.v3.itemtype.wifi.WiFiItemEditViewModel
import com.dashlane.item.v3.viewmodels.CredentialItemEditViewModel
import com.dashlane.item.v3.viewmodels.ItemEditViewModel
import com.dashlane.item.v3.viewmodels.SecretItemEditViewModel
import com.dashlane.item.v3.viewmodels.SecureNoteItemEditViewModel
import com.dashlane.xml.domain.SyncObjectXmlName.AUTHENTIFIANT
import com.dashlane.xml.domain.SyncObjectXmlName.PAYMENT_CREDIT_CARD
import com.dashlane.xml.domain.SyncObjectXmlName.SECRET
import com.dashlane.xml.domain.SyncObjectXmlName.SECURE_NOTE
import com.dashlane.xml.domain.SyncObjectXmlName.WI_FI

class ItemEditViewModelFactory(private val fragment: ItemEditFragment) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val navArgs = ItemEditFragmentArgs.fromSavedStateHandle(extras.createSavedStateHandle())
        val dataType = navArgs.dataType
        return when (dataType) {
            AUTHENTIFIANT -> fragment.viewModels<CredentialItemEditViewModel>()
            SECURE_NOTE -> fragment.viewModels<SecureNoteItemEditViewModel>()
            SECRET -> fragment.viewModels<SecretItemEditViewModel>()
            PAYMENT_CREDIT_CARD -> fragment.viewModels<CreditCardItemEditViewModel>()
            WI_FI -> fragment.viewModels<WiFiItemEditViewModel>()
            else -> throw IllegalArgumentException("No ViewModel exists for $dataType")
        }.value as T
    }
}