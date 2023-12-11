package com.dashlane.csvimport.matchcsvfields

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.csvimport.R
import com.dashlane.csvimport.matchcsvfields.MatchCsvFieldsActivity.Companion.CATEGORY_PASSWORD
import com.dashlane.csvimport.matchcsvfields.MatchCsvFieldsActivity.Companion.CATEGORY_URL
import com.dashlane.csvimport.matchcsvfields.MatchCsvFieldsActivity.Companion.CATEGORY_USERNAME
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
internal class MatchCsvFieldsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _showIntroPopupFlow = MutableSharedFlow<Int?>(replay = 1)
    val showIntroPopupFlow: Flow<Int?>
        get() = _showIntroPopupFlow

    private var state = MatchCsvFieldsViewState(
        emptyList(),
        0,
        false
    ) 
        set(value) {
            field = value
            viewModelScope.launch { _stateFlow.emit(value) }
            savedStateHandle[KEY_ITEMS] = ArrayList(state.items)
            savedStateHandle[KEY_POSITION] = state.position
        }
    private val _stateFlow = MutableSharedFlow<MatchCsvFieldsViewState>(replay = 1)
    val stateFlow: Flow<MatchCsvFieldsViewState>
        get() = _stateFlow

    private val _activityResultFlow = MutableSharedFlow<Intent>(replay = 1)
    val activityResultFlow: Flow<Intent>
        get() = _activityResultFlow

    private val _activityCancelFlow = MutableSharedFlow<Int?>(replay = 1)
    val activityCancelFlow: Flow<Int?>
        get() = _activityCancelFlow

    private val fields: List<String> = savedStateHandle[MatchCsvFieldsActivity.EXTRA_FIELDS] ?: emptyList()

    init {
        if (fields.size < MINIMUM_FIELDS) {
            showNotEnoughField()
        } else {
            val items: List<MatchCsvFieldsItem>? = savedStateHandle[KEY_ITEMS]
            if (items == null) {
                state = MatchCsvFieldsViewState(
                    buildItems(),
                    0,
                    false
                )

                viewModelScope.launch { _showIntroPopupFlow.emit(state.items.size) }
            } else {
                val position = savedStateHandle[KEY_POSITION] ?: 0
                val hasRequiredCategories = hasRequiredCategories(items)
                state = MatchCsvFieldsViewState(
                    items,
                    position,
                    hasRequiredCategories
                )
            }
        }
    }

    fun onBackPressed(): Boolean {
        val position = state.position
        return if (position - 1 >= 0) {
            state = state.copy(position = position - 1)
            true
        } else {
            false
        }
    }

    fun onCategorySelected(item: MatchCsvFieldsItem, category: Int) {
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

    fun onDismissIntroPopup() {
        viewModelScope.launch { _showIntroPopupFlow.emit(null) }
    }

    fun onValidateClicked() {
        val categories = state.items.map { it.category }
        val data = Intent()
            .putIntegerArrayListExtra(MatchCsvFieldsActivity.EXTRA_CATEGORIES, ArrayList(categories))
        viewModelScope.launch { _activityResultFlow.emit(data) }
    }

    fun decrementPosition() {
        val position = state.position
        if (position - 1 >= 0) {
            state = state.copy(position = position - 1)
        }
    }

    fun incrementPosition() {
        val position = state.position
        val itemsSize = state.items.size
        if (position + 1 < itemsSize) {
            state = state.copy(position = position + 1)
        }
    }

    fun goToPosition(position: Int) {
        state = state.copy(position = position)
    }

    fun onCancelClicked() {
        cancel()
    }

    private fun showNotEnoughField() {
        viewModelScope.launch { _activityCancelFlow.emit(R.string.csv_import_empty_state_title) }
    }

    private fun cancel() {
        viewModelScope.launch { _activityCancelFlow.emit(null) }
    }

    private fun buildItems() = fields.mapIndexed { index, field -> MatchCsvFieldsItem(index, field, null) }

    private fun hasRequiredCategories(items: List<MatchCsvFieldsItem>) =
        items.mapNotNull(MatchCsvFieldsItem::category).toSet().containsAll(requiredCategories)

    companion object {
        private val requiredCategories = setOf(CATEGORY_URL, CATEGORY_USERNAME, CATEGORY_PASSWORD)

        private const val MINIMUM_FIELDS = 3

        private const val KEY_ITEMS = "key_items"
        private const val KEY_POSITION = "key_position"
    }
}