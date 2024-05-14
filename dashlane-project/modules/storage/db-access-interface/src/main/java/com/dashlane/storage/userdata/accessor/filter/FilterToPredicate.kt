package com.dashlane.storage.userdata.accessor.filter

import android.content.Context
import com.dashlane.core.helpers.PackageNameSignatureHelper
import com.dashlane.storage.userdata.accessor.filter.sharing.SharingFilter
import com.dashlane.storage.userdata.accessor.filter.space.SpaceFilter
import com.dashlane.storage.userdata.accessor.filter.uid.UidFilter
import com.dashlane.teamspaces.manager.DataIdentifierSpaceCategorization
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.model.UserPermission
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.vault.util.matchPackageName
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class FilterToPredicate @Inject constructor(
    @ApplicationContext private val context: Context,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
    private val currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter,
    private val packageNameSignatureHelper: PackageNameSignatureHelper
) {

    fun toPredicate(filter: BaseFilter): (VaultItem<*>) -> Boolean {
        return { accept(filter, it) }
    }

    private fun accept(filter: BaseFilter, vaultItem: VaultItem<*>): Boolean {
        return hasCorrectDataType(filter, vaultItem) &&
            hasCorrectUid(filter, vaultItem) &&
            hasCorrectSpace(filter, vaultItem) &&
            hasCorrectSharing(filter, vaultItem) &&
            ((filter !is CredentialFilter) || acceptCredentialFilter(filter, vaultItem.syncObject)) &&
            ((filter !is CollectionFilter) || acceptCollectionFilter(filter, vaultItem))
    }

    private fun hasCorrectSharing(filter: BaseFilter, vaultItem: VaultItem<*>): Boolean {
        return (filter as? SharingFilter)?.let { sharingFilter ->
            
            sharingFilter.sharingPermissions?.any {
                
                (it == UserPermission.UNDEFINED && vaultItem.sharingPermission == null) ||
                    (vaultItem.sharingPermission == it)
            } ?: true
        } ?: true 
    }

    private fun hasCorrectUid(filter: BaseFilter, vaultItem: VaultItem<*>) =
        (filter as? UidFilter)?.onlyOnUids?.contains(vaultItem.uid) ?: true

    private fun hasCorrectDataType(filter: BaseFilter, vaultItem: VaultItem<*>) =
        filter.dataTypes.contains(vaultItem.syncObjectType)

    private fun hasCorrectSpace(filter: BaseFilter, vaultItem: VaultItem<*>): Boolean {
        val teamspaceAccessor = teamSpaceAccessorProvider.get()
            ?: return false 
        val spaceFilter = filter as? SpaceFilter
            ?: return true 
        val spaces = spaceFilter.getSpacesRestrictions(currentTeamSpaceUiFilter)
            ?: return true 
        return spaces.any {
            DataIdentifierSpaceCategorization(teamspaceAccessor, currentTeamSpaceUiFilter, it).canBeDisplay(vaultItem)
        }
    }

    private fun acceptCredentialFilter(filter: CredentialFilter, syncObject: SyncObject): Boolean {
        return syncObject is SyncObject.Authentifiant &&
            filter.email?.equals(syncObject.email, ignoreCase = true) ?: true && 
            hasCorrectDomain(filter, syncObject) &&
            hasCorrectPackageName(filter, syncObject)
    }

    private fun hasCorrectDomain(filter: CredentialFilter, authentifiant: SyncObject.Authentifiant): Boolean =
        hasCorrectDomain(
            filter,
            authentifiant.url,
            authentifiant.userSelectedUrl,
            authentifiant.title,
            authentifiant.linkedServices?.associatedDomains?.map { it.domain }
        )

    private fun hasCorrectPackageName(filter: CredentialFilter, authentifiant: SyncObject.Authentifiant) =
        filter.packageName?.let { authentifiant.matchPackageName(packageNameSignatureHelper, context, it) }
            ?: true 

    private fun acceptCollectionFilter(filter: CollectionFilter, vaultItem: VaultItem<*>): Boolean {
        val syncObject = vaultItem.syncObject
        return syncObject is SyncObject.Collection &&
            filter.withVaultItem?.let { filterVaultItem ->
                filterVaultItem in (vaultItem.toSummary<SummaryObject.Collection>().vaultItems ?: emptyList())
            } ?: true &&
            filter.withoutVaultItem?.let { filterVaultItem ->
                filterVaultItem !in (vaultItem.toSummary<SummaryObject.Collection>().vaultItems ?: emptyList())
            } ?: true &&
            filter.withVaultItemId?.let { filterItemId ->
                filterItemId in (
                    vaultItem.toSummary<SummaryObject.Collection>().vaultItems?.map { it.id }
                        ?: emptyList()
                    )
            } ?: true &&
            filter.withoutVaultItemId?.let { filterItemId ->
                filterItemId !in (
                    vaultItem.toSummary<SummaryObject.Collection>().vaultItems?.map { it.id }
                        ?: emptyList()
                    )
            } ?: true &&
            filter.name?.equals(syncObject.name) ?: true
    }
}