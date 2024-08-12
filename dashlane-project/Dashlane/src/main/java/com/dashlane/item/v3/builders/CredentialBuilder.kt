package com.dashlane.item.v3.builders

import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.data.toCredentialFormData
import com.dashlane.item.v3.repositories.ItemEditRepository
import com.dashlane.item.v3.viewmodels.State
import com.dashlane.teamspaces.manager.TeamSpaceAccessorProvider
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.ui.screens.fragments.SharingPolicyDataProvider
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject

class CredentialBuilder @Inject constructor(
    private val teamSpaceAccessorProvider: TeamSpaceAccessorProvider,
    private val itemEditRepository: ItemEditRepository,
    private val sharingPolicyDataProvider: SharingPolicyDataProvider,
    private val frozenStateManager: FrozenStateManager,
) : FormData.Builder() {
    override fun build(
        initialSummaryObject: SummaryObject,
        state: State,
    ): FormData {
        val teamSpaceAccessor = teamSpaceAccessorProvider.get()
        val sharingAllowsEdit = sharingPolicyDataProvider.canEditItem(initialSummaryObject, state.isNew)
        teamSpace = itemEditRepository.getTeamspace(initialSummaryObject.spaceId)
        availableSpaces =
            teamSpaceAccessor?.availableSpaces?.minus(TeamSpace.Combined) ?: emptyList()
        isEditable = sharingAllowsEdit && !frozenStateManager.isAccountFrozen
        isCopyActionAllowed = !frozenStateManager.isAccountFrozen
        return (initialSummaryObject as SummaryObject.Authentifiant).toCredentialFormData(
            isEditable = isEditable,
            canDelete = canDelete,
            sharingCount = sharingCount,
            teamSpace = teamSpace,
            availableSpaces = availableSpaces,
            isCopyActionAllowed = isCopyActionAllowed,
            isSharedWithLimitedRight = !sharingAllowsEdit,
        )
    }
}