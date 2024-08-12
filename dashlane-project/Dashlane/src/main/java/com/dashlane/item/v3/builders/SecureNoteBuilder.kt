package com.dashlane.item.v3.builders

import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.data.toSecureNoteFormData
import com.dashlane.item.v3.viewmodels.State
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject

class SecureNoteBuilder @Inject constructor() : FormData.Builder() {
    override fun build(
        initialSummaryObject: SummaryObject,
        state: State
    ): FormData = (initialSummaryObject as SummaryObject.SecureNote).toSecureNoteFormData(
        isEditable = isEditable,
        canDelete = canDelete,
        sharingCount = sharingCount,
        teamSpace = teamSpace,
        availableSpaces = availableSpaces
    )
}