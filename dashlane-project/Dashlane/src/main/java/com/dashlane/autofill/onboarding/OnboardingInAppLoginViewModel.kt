package com.dashlane.autofill.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.autofill.phishing.AntiPhishingFilesDownloader
import com.dashlane.notification.creator.AutoFillNotificationCreator
import com.dashlane.preference.GlobalPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingInAppLoginViewModel @Inject constructor(
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val autoFillNotificationCreator: AutoFillNotificationCreator,
    private val antiPhishingFilesDownloader: AntiPhishingFilesDownloader
) : ViewModel() {

    fun onDoneClicked() {
        viewModelScope.launch {
            autoFillNotificationCreator.cancelAutofillNotificationWorkers()
            globalPreferencesManager.saveActivatedAutofillOnce()
            antiPhishingFilesDownloader.downloadFilePhishingFiles()
        }
    }
}