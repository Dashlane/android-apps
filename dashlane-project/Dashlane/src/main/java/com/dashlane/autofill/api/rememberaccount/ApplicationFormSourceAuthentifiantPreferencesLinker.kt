package com.dashlane.autofill.api.rememberaccount

import androidx.annotation.VisibleForTesting
import com.dashlane.autofill.api.rememberaccount.services.FormSourceAuthentifiantLinker
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.GlobalPreferencesManager
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class ApplicationFormSourceAuthentifiantPreferencesLinker @VisibleForTesting constructor(
    val globalPreferencesManager: GlobalPreferencesManager,
    val coroutineContext: CoroutineContext = Dispatchers.IO
) : FormSourceAuthentifiantLinker by FormSourceAuthentifiantPreferencesLinker(
    ConstantsPrefs.AUTOFILL_REMEMBER_ACCOUNT_FOR_APP_SOURCES_LIST,
    globalPreferencesManager,
    Dispatchers.IO
) {
    @Inject
    constructor(globalPreferencesManager: GlobalPreferencesManager) : this(
        globalPreferencesManager,
        Dispatchers.IO
    )
}