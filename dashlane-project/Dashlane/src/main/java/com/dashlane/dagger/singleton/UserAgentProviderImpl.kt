package com.dashlane.dagger.singleton

import android.os.Build
import com.dashlane.BuildConfig
import com.dashlane.debug.DaDaDa
import com.dashlane.server.api.UserAgent.Companion.PARTNER
import com.dashlane.server.api.UserAgent.Companion.PLATFORM
import com.dashlane.server.api.UserAgentProvider
import com.dashlane.util.Constants
import javax.inject.Inject

class UserAgentProviderImpl @Inject constructor(private val dadada: DaDaDa) : UserAgentProvider {
    override val appVersionName: String
        get() = dadada.appVersionName ?: BuildConfig.VERSION_NAME
    override val osVersion: String
        get() = Build.VERSION.RELEASE
    override val partner: String
        get() = PARTNER
    override val platform: String
        get() = PLATFORM
    override val language: String
        get() = Constants.getOSLang()
}