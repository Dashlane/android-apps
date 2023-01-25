package com.dashlane.ui.activities.fragments.checklist

import kotlinx.coroutines.flow.Flow

interface ChecklistViewModelContract {
    val checkListDataFlow: Flow<ChecklistData>
    fun onDismissChecklistClicked()
}