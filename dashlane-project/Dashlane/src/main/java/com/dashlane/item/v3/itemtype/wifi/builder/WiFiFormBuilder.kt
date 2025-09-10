package com.dashlane.item.v3.itemtype.wifi.builder

import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.item.v3.data.CommonData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.itemtype.wifi.WiFiFormData
import com.dashlane.item.v3.repositories.ItemEditRepository
import com.dashlane.item.v3.util.Field
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.teamspaces.manager.TeamSpaceAccessorProvider
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject

class WiFiFormBuilder @Inject constructor(
    private val frozenStateManager: FrozenStateManager,
    private val teamSpaceAccessorProvider: TeamSpaceAccessorProvider,
    private val itemEditRepository: ItemEditRepository,
    private val wifiHiddenFieldsBuilder: WiFiHiddenFieldsBuilder,
) : FormData.Builder<WiFiFormData>() {
    override suspend fun build(
        initialSummaryObject: SummaryObject,
        isNew: Boolean,
    ): Data<WiFiFormData> {
        val summaryObject = (initialSummaryObject as SummaryObject.WiFi)
        isCopyActionAllowed = !frozenStateManager.isAccountFrozen

        teamSpace = itemEditRepository.getTeamspace(initialSummaryObject.spaceId)
        availableSpaces = teamSpaceAccessorProvider.get()
            ?.availableSpaces?.minus(TeamSpace.Combined) ?: emptyList()
        if (isNew && availableSpaces.isNotEmpty()) {
            
            teamSpace = availableSpaces.firstOrNull {
                it != TeamSpace.Personal
            } ?: TeamSpace.Personal
        }

        return Data(
            commonData = summaryObject.buildCreditCardCommonData(
                isNew = isNew,
            ),
            formData = WiFiFormData(
                ssid = summaryObject.ssid.takeIf { it.isNotSemanticallyNull() } ?: "",
                isPassphraseEmpty = summaryObject.isPassphraseEmpty,
                note = summaryObject.note.takeIf { it.isNotSemanticallyNull() } ?: "",
            ),
        )
    }

    override fun buildHiddenFields(data: WiFiFormData, commonData: CommonData): Set<Field> =
        wifiHiddenFieldsBuilder(data = data)

    private fun SummaryObject.WiFi.buildCreditCardCommonData(isNew: Boolean): CommonData {
        return CommonData(
            id = this.id,
            name = name.takeIf { it.isNotSemanticallyNull() },
            isCopyActionAllowed = isCopyActionAllowed,
            created = this.creationDatetime,
            updated = this.userModificationDatetime,
            canDelete = isNew.not(),
            isEditable = true,
            space = teamSpace,
            availableSpaces = availableSpaces,
        )
    }
}