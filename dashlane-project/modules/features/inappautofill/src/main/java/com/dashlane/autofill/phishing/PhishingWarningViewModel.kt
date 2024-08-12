package com.dashlane.autofill.phishing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.autofill.rememberaccount.model.FormSourcesDataProvider
import com.dashlane.vault.model.getUrlDisplayName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhishingWarningViewModel @Inject constructor(
    private val phishingWarningDataProvider: PhishingWarningDataProvider,
    private val formSourceDataProvider: FormSourcesDataProvider,
    private val logger: AutofillPhishingLogger,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val hintSummary: AutoFillHintSummary? = savedStateHandle[EXTRA_PHISHING_HINT_SUMMARY]
    private val itemId: String? = savedStateHandle[EXTRA_PHISHING_ITEM_ID]
    private val phishingAttemptLevel: PhishingAttemptLevel = savedStateHandle[EXTRA_PHISHING_LEVEL]
        ?: PhishingAttemptLevel.NONE
    private val website = hintSummary?.webDomain?.getUrlDisplayName
    private val isNativeApp = hintSummary?.formSource is ApplicationFormSource
    private val packageName = hintSummary?.packageName

    private val _uiState = MutableStateFlow<UiState>(
        UiState.Initial(
            Data(
                rememberChecked = false,
                website = website,
                itemWebsite = savedStateHandle[EXTRA_PHISHING_ITEM_WEBSITE],
                isAlreadyRemembered = phishingWarningDataProvider.isWebsiteIgnored(website),
                phishingAttemptLevel = phishingAttemptLevel
            )
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun toggleRemember() {
        _uiState.tryEmit(UiState.Initial(_uiState.value.data.copy(rememberChecked = _uiState.value.data.rememberChecked.not())))
    }

    fun dismiss(origin: PhishingWarningOrigin) {
        when (origin) {
            PhishingWarningOrigin.PHISHING_WARNING -> {
                if (_uiState.value.data.rememberChecked && packageName != null) {
                    logger.onSuggestAutoFillRiskToNone(
                        isNativeApp = isNativeApp,
                        packageName = packageName,
                    )
                    logger.onDismissAutoFill(
                        isNativeApp = isNativeApp,
                        packageName = packageName,
                        trust = true,
                    )
                }
                saveRememberWebsite(linkWebsite = false)
            }
            PhishingWarningOrigin.AUTOFILL_PHISHING_WARNING -> {
                if (packageName != null) {
                    logger.onDismissAutoFill(
                        isNativeApp = isNativeApp,
                        packageName = packageName,
                        trust = false,
                    )
                }
            }
        }
    }

    fun trustWebsite() {
        if (packageName != null) {
            logger.onAcceptAutoFillRisk(
                isNativeApp = isNativeApp,
                packageName = packageName,
                phishingAttemptLevel = phishingAttemptLevel,
            )
            if (_uiState.value.data.rememberChecked) {
                logger.onDismissAutoFill(
                    isNativeApp = isNativeApp,
                    packageName = packageName,
                    trust = true,
                )
            }
        }

        saveRememberWebsite(linkWebsite = true)
    }

    private fun saveRememberWebsite(linkWebsite: Boolean) = viewModelScope.launch {
        if (_uiState.value.data.rememberChecked) {
            phishingWarningDataProvider.addPhishingWebsiteIgnored(website)
            if (linkWebsite && itemId != null && website != null) {
                
                formSourceDataProvider.link(WebDomainFormSource("", website), itemId)
            }
        }
        _uiState.tryEmit(UiState.DataSaved(_uiState.value.data))
    }

    sealed class UiState {
        abstract val data: Data

        data class Initial(override val data: Data) : UiState()
        data class DataSaved(override val data: Data) : UiState()
    }

    data class Data(
        val rememberChecked: Boolean,
        val website: String?,
        val itemWebsite: String?,
        val isAlreadyRemembered: Boolean,
        val phishingAttemptLevel: PhishingAttemptLevel
    )

    enum class PhishingWarningOrigin {
        PHISHING_WARNING,
        AUTOFILL_PHISHING_WARNING,
    }

    companion object {
        const val EXTRA_PHISHING_HINT_SUMMARY = "EXTRA_PHISHING_HINT_SUMMARY"
        const val EXTRA_PHISHING_ITEM_WEBSITE = "EXTRA_PHISHING_ITEM_WEBSITE"
        const val EXTRA_PHISHING_ITEM_ID = "EXTRA_PHISHING_ITEM_ID"
        const val EXTRA_PHISHING_LEVEL = "EXTRA_PHISHING_LEVEL"
    }
}