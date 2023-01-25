package com.dashlane.item.linkedwebsites

import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject

fun VaultItem<*>.getUpdatedLinkedWebsites(
    oldItem: VaultItem<*>
): Pair<List<String>, List<String>>? {
    val syncObjectToSave = syncObject
    val syncObjectSaved = oldItem.syncObject
    if (syncObjectToSave is SyncObject.Authentifiant && syncObjectSaved is SyncObject.Authentifiant) {
        return getUpdatedLinkedWebsites(
            syncObjectToSave.linkedServices?.associatedDomains?.map { it.domain ?: "" },
            syncObjectSaved.linkedServices?.associatedDomains?.map { it.domain ?: "" })
    }
    return null
}

fun VaultItem<*>.getRemovedLinkedApps(oldItem: VaultItem<*>): List<String>? {
    val syncObjectToSave = syncObject
    val syncObjectSaved = oldItem.syncObject
    if (syncObjectToSave is SyncObject.Authentifiant && syncObjectSaved is SyncObject.Authentifiant) {
        val oldApps = syncObjectSaved.linkedServices?.associatedAndroidApps?.map { it.packageName ?: "" } ?: listOf()
        val newApps = syncObjectToSave.linkedServices?.associatedAndroidApps?.map { it.packageName ?: "" } ?: listOf()
        return oldApps - newApps
    }
    return null
}



fun getUpdatedLinkedWebsites(websites: List<String>?, oldWebsites: List<String>?): Pair<List<String>, List<String>> {
    val addedWebsites = (websites ?: listOf()) - (oldWebsites ?: listOf())
    val removedWebsites = (oldWebsites ?: listOf()) - (websites ?: listOf())
    return Pair(addedWebsites, removedWebsites)
}