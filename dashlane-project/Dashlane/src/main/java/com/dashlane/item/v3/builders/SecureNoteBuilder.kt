package com.dashlane.item.v3.builders

import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.data.SecureNoteFormData
import com.dashlane.item.v3.data.toSecureNoteFormData
import com.dashlane.item.v3.repositories.ItemEditRepository
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.sharingpolicy.SharingPolicyDataProvider
import com.dashlane.teamspaces.manager.TeamSpaceAccessorProvider
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject

class SecureNoteBuilder @Inject constructor(
    private val teamSpaceAccessorProvider: TeamSpaceAccessorProvider,
    private val itemEditRepository: ItemEditRepository,
    private val sharingPolicyDataProvider: SharingPolicyDataProvider,
) : FormData.Builder<SecureNoteFormData>() {
    override suspend fun build(
        initialSummaryObject: SummaryObject,
        isNew: Boolean,
    ): Data<SecureNoteFormData> {
        val teamSpaceAccessor = teamSpaceAccessorProvider.get()
        
        val sharingAllowsEdit = sharingPolicyDataProvider.canEditItem(initialSummaryObject, isNew)
        teamSpace = itemEditRepository.getTeamspace(initialSummaryObject.spaceId)
        availableSpaces =
            teamSpaceAccessor?.availableSpaces?.minus(TeamSpace.Combined) ?: emptyList()
        if (isNew && availableSpaces.isNotEmpty()) {
            
            teamSpace = availableSpaces.firstOrNull { it != TeamSpace.Personal } ?: TeamSpace.Personal
        }
        isEditable = sharingPolicyDataProvider.canEditItem(initialSummaryObject, isNew)
        return (initialSummaryObject as SummaryObject.SecureNote).toSecureNoteFormData(
            isEditable = isEditable,
            canDelete = canDelete,
            sharingCount = sharingCount,
            teamSpace = teamSpace,
            availableSpaces = availableSpaces,
            isSharedWithLimitedRight = !sharingAllowsEdit,
            secureSettingAvailable = teamSpaceAccessor?.isSsoUser == false,
        )
    }
}