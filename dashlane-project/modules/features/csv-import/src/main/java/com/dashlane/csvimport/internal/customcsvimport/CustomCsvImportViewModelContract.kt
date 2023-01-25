package com.dashlane.csvimport.internal.customcsvimport

import android.content.Intent
import com.dashlane.csvimport.CustomCsvImportActivity
import kotlinx.coroutines.flow.Flow

internal interface CustomCsvImportViewModelContract {
    val showIntroPopupFlow: Flow<Int?>
    val stateFlow: Flow<CustomCsvImportViewState>
    val activityResultFlow: Flow<Intent>
    val activityCancelFlow: Flow<Int?>

    fun onBackPressed(): Boolean

    fun onCategorySelected(item: CustomCsvImportItem, @CustomCsvImportActivity.Category category: Int)
    fun onPositionChanged(newPosition: Int)
    fun onDismissIntroPopup()
    fun onValidateClicked()
    fun onCancelClicked()
    fun decrementPosition()
    fun incrementPosition()
}