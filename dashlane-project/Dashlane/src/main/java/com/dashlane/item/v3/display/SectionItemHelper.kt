package com.dashlane.item.v3.display

import androidx.compose.foundation.lazy.LazyListScope
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.display.sections.SharedAccessSection
import com.dashlane.item.v3.viewmodels.FormState

fun LazyListScope.sharedAccessSectionItem(
    uiState: FormState<out FormData>,
    onSharedClick: () -> Unit
) {
    item(key = "SharedAccessSection") {
        SharedAccessSection(
            commonData = uiState.datas.current.commonData,
            editMode = uiState.isEditMode,
            onSharedClick = onSharedClick
        )
    }
}