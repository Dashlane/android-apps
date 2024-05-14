package com.dashlane.darkweb.ui.setup

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow

internal interface DarkWebSetupMailViewModelContract {
    val suggestions: Deferred<List<String>>

    val state: Flow<DarkWebSetupMailState>

    fun onOptInClicked(mail: String)

    fun onMailChanged(mail: String)

    fun onCancel()
}