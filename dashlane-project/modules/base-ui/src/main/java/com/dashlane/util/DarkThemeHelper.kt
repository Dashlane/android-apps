package com.dashlane.util

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.useractivity.log.inject.UserActivityComponent
import com.dashlane.useractivity.log.install.InstallLogCode17
import javax.inject.Inject



class DarkThemeHelper @Inject constructor(
    private val prefs: GlobalPreferencesManager
) {
    private val isAtLeastQ: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    

    val isSettingAvailable: Boolean
        get() = !isAtLeastQ

    var isSettingEnabled: Boolean
        get() = prefs.getBoolean(ConstantsPrefs.IS_DARK_THEME_ENABLED)
        set(value) {
            prefs.putBoolean(ConstantsPrefs.IS_DARK_THEME_ENABLED, value)
            forceDayNight(value)
        }

    

    fun onApplicationCreate(application: Application) {

        
        val logType = if (isDarkTheme(application)) "darkThemeEnabled" else "darkThemeDisabled"
        UserActivityComponent(application)
            .installLogRepository
            .enqueue(InstallLogCode17(type = logType, subStep = "17.34.1"))

        
        if (!isAtLeastQ) {
            forceDayNight(isSettingEnabled)
        }
    }

    private fun forceDayNight(night: Boolean) {
        val mode = if (night) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    

    fun isDarkTheme(context: Context) = if (isAtLeastQ) {
        
        context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    } else {
        
        isSettingEnabled
    }
}