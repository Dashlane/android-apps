package com.dashlane.csvimport.csvimport

import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject

interface CsvImportViewTypeProvider : DashlaneRecyclerAdapter.ViewTypeProvider {

    var selected: Boolean

    interface Factory {
        fun create(authentifiant: VaultItem<SyncObject.Authentifiant>, selected: Boolean): CsvImportViewTypeProvider
    }
}
