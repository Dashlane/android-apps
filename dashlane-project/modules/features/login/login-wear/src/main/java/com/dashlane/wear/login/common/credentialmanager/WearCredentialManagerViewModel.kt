package com.dashlane.wear.login.common.credentialmanager

import androidx.lifecycle.ViewModel
import com.dashlane.credentialmanager.WearCredentialManagerUtil
import com.dashlane.preference.GlobalPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WearCredentialManagerViewModel @Inject constructor(
    private val credentialManagerUtil: WearCredentialManagerUtil,
    private val globalPreferencesManager: GlobalPreferencesManager,
) : ViewModel() {
    fun enableCredentialProvider() {
        credentialManagerUtil.enableCredentialProvider()
        globalPreferencesManager.saveActivatedAutofillOnce()
    }
}