package com.dashlane.login.controller

import android.app.Activity
import android.os.Bundle
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.ui.AbstractActivityLifecycleListener
import javax.inject.Inject

class NumberOfRunsActivityListener @Inject constructor(
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager,
) :
    AbstractActivityLifecycleListener() {
    override fun onFirstLoggedInActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onFirstLoggedInActivityCreated(activity, savedInstanceState)
        val userPreferencesManager = preferencesManager[sessionManager.session?.username]
        val runs = userPreferencesManager.getInt(ConstantsPrefs.RUNS)
        userPreferencesManager.putInt(ConstantsPrefs.RUNS, runs + 1)
    }
}