package com.dashlane.autofill.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.dashlane.autofill.onboarding.OnboardingInAppLoginDone.Companion.ARGS_ONBOARDING_TYPE
import com.dashlane.notification.creator.AutoFillNotificationCreator
import com.dashlane.preference.GlobalPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingInAppLoginViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val autoFillNotificationCreator: AutoFillNotificationCreator
) : ViewModel() {
    val onboardingType: OnboardingType = savedStateHandle[ARGS_ONBOARDING_TYPE] ?: OnboardingType.AUTO_FILL_API

    fun onDoneClicked() {
        if (onboardingType == OnboardingType.AUTO_FILL_API) {
            autoFillNotificationCreator.cancelAutofillNotificationWorkers()
            globalPreferencesManager.saveActivatedAutofillOnce()
        }
    }
}