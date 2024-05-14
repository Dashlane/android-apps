package com.dashlane.labs

import androidx.lifecycle.ViewModel
import com.dashlane.userfeatures.FeatureFlip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DashlaneLabsViewModels @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<DashlaneLabsState>(
        DashlaneLabsState.Loading(
            ViewData(features = emptyList(), helpClicked = false)
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        getLabsAvailableFeatureFlip()
            .map { it.name }
            .let {
                _uiState.value = DashlaneLabsState.Loaded(_uiState.value.viewData.copy(features = it))
            }
    }

    fun onHelpClicked() {
        _uiState.value = DashlaneLabsState.Loaded(_uiState.value.viewData.copy(helpClicked = true))
    }

    fun onHelpOpened() {
        _uiState.value = DashlaneLabsState.Loaded(_uiState.value.viewData.copy(helpClicked = false))
    }

    private fun getLabsAvailableFeatureFlip(): List<FeatureFlip> {
        return FeatureFlip.values()
            .toList()
            .filterNot { it == FeatureFlip.DASHLANE_LABS }
    }
}