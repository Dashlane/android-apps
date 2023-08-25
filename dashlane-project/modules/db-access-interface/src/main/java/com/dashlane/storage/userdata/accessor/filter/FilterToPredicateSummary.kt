package com.dashlane.storage.userdata.accessor.filter

import android.content.Context
import com.dashlane.core.helpers.PackageNameSignatureHelper
import com.dashlane.storage.userdata.accessor.filter.space.SpaceFilter
import com.dashlane.teamspaces.manager.DataIdentifierSpaceCategorization
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.matchPackageName
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class FilterToPredicateSummary @Inject constructor(
    @ApplicationContext private val context: Context,
    private val teamspaceAccessorProvider: OptionalProvider<TeamspaceAccessor>,
    private val packageNameSignatureHelper: PackageNameSignatureHelper
) {

    fun toPredicate(filter: BaseFilter): (SummaryObject) -> Boolean {
        return { accept(filter, it) }
    }

    private fun accept(filter: BaseFilter, vaultItem: SummaryObject): Boolean {
        return hasCorrectSpace(filter, vaultItem) &&
                ((filter !is CredentialFilter) || acceptCredentialFilter(filter, vaultItem)) &&
                ((filter !is CollectionFilter) || acceptCollectionFilter(filter, vaultItem))
    }

    private fun hasCorrectSpace(filter: BaseFilter, vaultItem: SummaryObject): Boolean {
        val teamspaceAccessor = teamspaceAccessorProvider.get()
            ?: return false 
        val spaceFilter = filter as? SpaceFilter
            ?: return true 
        val spaces = spaceFilter.getSpacesRestrictions(teamspaceAccessor)
            ?: return true 
        return spaces.any {
            DataIdentifierSpaceCategorization(teamspaceAccessor, it).canBeDisplay(vaultItem)
        }
    }

    private fun acceptCredentialFilter(filter: CredentialFilter, syncObject: SummaryObject): Boolean {
        return syncObject is SummaryObject.Authentifiant &&
                filter.email?.equals(syncObject.email, ignoreCase = true) ?: true && 
                hasCorrectDomain(filter, syncObject) &&
                hasCorrectPackageName(filter, syncObject)
    }

    private fun acceptCollectionFilter(filter: CollectionFilter, summaryObject: SummaryObject): Boolean {
        return summaryObject is SummaryObject.Collection &&
            filter.withVaultItem?.let { it in (summaryObject.vaultItems ?: emptyList()) } ?: true &&
            filter.withoutVaultItem?.let { it !in (summaryObject.vaultItems ?: emptyList()) } ?: true &&
            filter.withVaultItemId?.let { filterItemId ->
                filterItemId in (summaryObject.vaultItems?.map { it.id } ?: emptyList())
            } ?: true &&
            filter.withoutVaultItemId?.let { filterItemId ->
                filterItemId !in (summaryObject.vaultItems?.map { it.id } ?: emptyList())
            } ?: true &&
            filter.name?.equals(summaryObject.name) ?: true
    }

    private fun hasCorrectDomain(filter: CredentialFilter, authentifiant: SummaryObject.Authentifiant): Boolean =
        hasCorrectDomain(
            filter,
            authentifiant.url,
            authentifiant.userSelectedUrl,
            authentifiant.title,
            authentifiant.linkedServices?.associatedDomains?.map { it.domain }
        )

    private fun hasCorrectPackageName(filter: CredentialFilter, authentifiant: SummaryObject.Authentifiant) =
        filter.packageName?.let { authentifiant.matchPackageName(packageNameSignatureHelper, context, it) }
            ?: true 
}