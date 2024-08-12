package com.dashlane.autofill.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.autofill.onboarding.OnboardingInAppLoginDone.Companion.ARGS_ONBOARDING_TYPE
import com.dashlane.autofill.phishing.AntiPhishingFilesDownloader
import com.dashlane.notification.creator.AutoFillNotificationCreator
import com.dashlane.preference.GlobalPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingInAppLoginViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val autoFillNotificationCreator: AutoFillNotificationCreator,
    private val antiPhishingFilesDownloader: AntiPhishingFilesDownloader
) : ViewModel() {
    val onboardingType: OnboardingType = savedStateHandle[ARGS_ONBOARDING_TYPE] ?: OnboardingType.AUTO_FILL_API

    fun onDoneClicked() {
        viewModelScope.launch {
            if (onboardingType == OnboardingType.AUTO_FILL_API) {
                autoFillNotificationCreator.cancelAutofillNotificationWorkers()
                globalPreferencesManager.saveActivatedAutofillOnce()
            }
            antiPhishingFilesDownloader.downloadFilePhishingFiles()
        }
    }
}