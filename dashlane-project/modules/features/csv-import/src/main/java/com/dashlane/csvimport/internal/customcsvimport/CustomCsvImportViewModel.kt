package com.dashlane.csvimport.internal.customcsvimport

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.csvimport.CustomCsvImportActivity
import com.dashlane.csvimport.CustomCsvImportActivity.Companion.CATEGORY_PASSWORD
import com.dashlane.csvimport.CustomCsvImportActivity.Companion.CATEGORY_URL
import com.dashlane.csvimport.CustomCsvImportActivity.Companion.CATEGORY_USERNAME
import com.dashlane.csvimport.R
import com.dashlane.csvimport.internal.ImportMultiplePasswordsLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class CustomCsvImportViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val logger: ImportMultiplePasswordsLogger
) : ViewModel(), CustomCsvImportViewModelContract {

    private val _showIntroPopupFlow = MutableSharedFlow<Int?>(replay = 1)
    override val showIntroPopupFlow: Flow<Int?>
        get() = _showIntroPopupFlow

    private var state = CustomCsvImportViewState(emptyList(), 0, false) 
        set(value) {
            field = value
            viewModelScope.launch { _stateFlow.emit(value) }
            savedStateHandle[KEY_ITEMS] = ArrayList(state.items)
            savedStateHandle[KEY_POSITION] = state.position
        }
    private val _stateFlow = MutableSharedFlow<CustomCsvImportViewState>(replay = 1)
    override val stateFlow: Flow<CustomCsvImportViewState>
        get() = _stateFlow

    private val _activityResultFlow = MutableSharedFlow<Intent>(replay = 1)
    override val activityResultFlow: Flow<Intent>
        get() = _activityResultFlow

    private val _activityCancelFlow = MutableSharedFlow<Int?>(replay = 1)
    override val activityCancelFlow: Flow<Int?>
        get() = _activityCancelFlow

    private val fields = savedStateHandle.get<List<String>>(CustomCsvImportActivity.EXTRA_FIELDS) ?: emptyList()

    init {
        if (fields.size < MINIMUM_FIELDS) {
            showNotEnoughField()
        } else {
            val items = savedStateHandle.get<List<CustomCsvImportItem>>(KEY_ITEMS)
            if (items == null) {
                state = CustomCsvImportViewState(
                    buildItems(),
                    0,
                    false
                )

                viewModelScope.launch { _showIntroPopupFlow.emit(state.items.size) }

                logger.logCustomCsvImportDisplayed(fields.size)
                logger.logCustomCsvImportItemSelected(0)
            } else {
                val position = savedStateHandle.get(KEY_POSITION) ?: 0
                val hasRequiredCategories = hasRequiredCategories(items)
                state = CustomCsvImportViewState(
                    items,
                    position,
                    hasRequiredCategories
                )
            }
        }
    }

    override fun onBackPressed(): Boolean {
        val position = state.position
        return if (position - 1 >= 0) {
            state = state.copy(position = position - 1)
            true
        } else {
            false
        }
    }

    override fun onCategorySelected(item: CustomCsvImportItem, category: Int) {
        val items = state.items.map {
            if (it.id == item.id) {
                if (it.category == category) {
                    
                    it.copy(category = null)
                } else {
                    
                    it.copy(category = category)
                }
            } else {
                it
            }
        }
        val hasRequiredCategories = hasRequiredCategories(items)
        state = state.copy(
            items = items,
            canValidate = hasRequiredCategories
        )
    }

    override fun onPositionChanged(newPosition: Int) {
        logger.logCustomCsvImportItemSelected(newPosition)
    }

    override fun onDismissIntroPopup() {
        viewModelScope.launch { _showIntroPopupFlow.emit(null) }
    }

    override fun onValidateClicked() {
        logger.logCustomCsvImportValidateClicked()

        val categories = state.items.map { it.category }
        val data = Intent()
            .putIntegerArrayListExtra(CustomCsvImportActivity.EXTRA_CATEGORIES, ArrayList(categories))
        viewModelScope.launch { _activityResultFlow.emit(data) }
    }

    override fun decrementPosition() {
        val position = state.position
        if (position - 1 >= 0) {
            state = state.copy(position = position - 1)
        }
    }

    override fun incrementPosition() {
        val position = state.position
        val itemsSize = state.items.size
        if (position + 1 < itemsSize) {
            state = state.copy(position = position + 1)
        }
    }

    override fun onCancelClicked() {
        logger.logCustomCsvImportCancelClicked()
        cancel()
    }

    private fun showNotEnoughField() {
        viewModelScope.launch { _activityCancelFlow.emit(R.string.csv_import_empty_state_title) }
    }

    private fun cancel() {
        viewModelScope.launch { _activityCancelFlow.emit(null) }
    }

    private fun buildItems() = fields.mapIndexed { index, field -> CustomCsvImportItem(index, field, null) }

    private fun hasRequiredCategories(items: List<CustomCsvImportItem>) =
        items.mapNotNull(CustomCsvImportItem::category).toSet().containsAll(requiredCategories)

    companion object {
        private val requiredCategories = setOf(CATEGORY_URL, CATEGORY_USERNAME, CATEGORY_PASSWORD)

        private const val MINIMUM_FIELDS = 3

        private const val KEY_ITEMS = "key_items"
        private const val KEY_POSITION = "key_position"
    }
}