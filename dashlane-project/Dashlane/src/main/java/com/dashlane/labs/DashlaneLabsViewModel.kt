package com.dashlane.labs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.featureflipping.FeatureFlipManager
import com.dashlane.preference.PreferencesManager
import com.dashlane.server.api.endpoints.features.ListAvailableLabsService
import com.dashlane.featureflipping.FeatureFlip
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashlaneLabsViewModel @Inject constructor(
    private val listAvailableLabsService: ListAvailableLabsService,
    private val featureFlipManager: FeatureFlipManager,
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val userPreferencesManager: UserPreferencesManager
        get() = preferencesManager[sessionManager.session?.username]

    private val _uiState = MutableStateFlow<DashlaneLabsState>(
        DashlaneLabsState.Loading(
            ViewData(labFeatures = emptyList(), helpClicked = false)
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        loadAvailableLab()
    }

    fun onHelpClicked() {
        _uiState.value = DashlaneLabsState.Loaded(_uiState.value.viewData.copy(helpClicked = true))
    }

    fun onHelpOpened() {
        _uiState.value = DashlaneLabsState.Loaded(_uiState.value.viewData.copy(helpClicked = false))
    }

    private fun loadAvailableLab() = viewModelScope.launch {
        val activatedLabs = userPreferencesManager.getLabsActivated()
        listAvailableLabsService.execute().data
            .labs
            .filter {
                featureFlipManager.featureFlips?.contains(it.featureName) != true
            }
            .filter { lab ->
                FeatureFlip.entries.map { it.value }.contains(lab.featureName)
            }
            .map {
                ViewData.Lab(
                    displayDescription = it.displayDescription,
                    featureName = it.featureName,
                    displayName = it.displayName,
                    enabled = activatedLabs.contains(it.featureName)
                )
            }.let {
                _uiState.value = DashlaneLabsState.Loaded(_uiState.value.viewData.copy(labFeatures = it))
            }
    }

    fun toggleLabFeature(featureName: String, enable: Boolean) = viewModelScope.launch {
        val activatedLabs = userPreferencesManager.getLabsActivated().toMutableSet()
        if (enable) {
            activatedLabs.add(featureName)
            userPreferencesManager.setLabsActivated(activatedLabs.toList())
        } else {
            activatedLabs.remove(featureName)
            userPreferencesManager.setLabsActivated(activatedLabs.toList())
        }
        loadAvailableLab()
    }
}