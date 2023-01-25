package com.dashlane.storage.userdata.accessor.filter

import android.content.Context
import com.dashlane.storage.userdata.accessor.filter.space.SpaceFilter
import com.dashlane.teamspaces.manager.DataIdentifierSpaceCategorization
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.matchPackageName
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject



class FilterToPredicateSummary @Inject constructor(
    @ApplicationContext private val context: Context,
    private val teamspaceAccessorProvider: OptionalProvider<TeamspaceAccessor>,
    private val userFeaturesChecker: UserFeaturesChecker
) {

    

    fun toPredicate(filter: BaseFilter): (SummaryObject) -> Boolean {
        return { accept(filter, it) }
    }

    private fun accept(filter: BaseFilter, vaultItem: SummaryObject): Boolean {
        return hasCorrectSpace(filter, vaultItem) &&
                ((filter !is CredentialFilter) || acceptCredentialFilter(filter, vaultItem))
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

    private fun hasCorrectDomain(filter: CredentialFilter, authentifiant: SummaryObject.Authentifiant): Boolean =
        hasCorrectDomain(
            filter,
            userFeaturesChecker,
            authentifiant.url,
            authentifiant.userSelectedUrl,
            authentifiant.title,
            authentifiant.linkedServices?.associatedDomains?.map { it.domain })

    private fun hasCorrectPackageName(filter: CredentialFilter, authentifiant: SummaryObject.Authentifiant) =
        filter.packageName?.let { authentifiant.matchPackageName(context, it) }
            ?: true 
}