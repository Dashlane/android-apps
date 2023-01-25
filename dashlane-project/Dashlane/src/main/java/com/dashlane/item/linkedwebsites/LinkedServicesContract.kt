package com.dashlane.item.linkedwebsites

import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject

interface LinkedServicesContract {
    interface ViewModel {
        fun save(linkedWebsites: List<String>, linkedApps: List<String>)
        fun switchEditMode()

        

        fun hasOtherItemsDuplicate(linkedWebsites: List<String>): Pair<String, String>?

        

        fun hasWebsitesToSave(linkedWebsites: List<String>): Boolean

        

        fun hasAppsToSave(linkedApps: List<String>): Boolean

        

        fun canEdit(): Boolean

        

        fun doneButtonActivated(): Boolean
    }

    interface DataProvider {
        suspend fun getItem(itemId: String): VaultItem<SyncObject.Authentifiant>?
        suspend fun save(
            vaultItem: VaultItem<SyncObject.Authentifiant>,
            linkedWebsites: List<String>,
            linkedApps: List<String>
        ): Boolean

        

        fun getDuplicateWebsitesItem(
            vaultItem: VaultItem<SyncObject.Authentifiant>?,
            websites: List<String>
        ): Pair<String, String>?
    }
}