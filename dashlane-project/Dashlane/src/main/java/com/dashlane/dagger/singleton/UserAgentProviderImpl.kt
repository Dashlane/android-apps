package com.dashlane.dagger.singleton

import android.os.Build
import com.dashlane.BuildConfig
import com.dashlane.debug.services.DaDaDaVersion
import com.dashlane.server.api.UserAgent.Companion.PARTNER
import com.dashlane.server.api.UserAgent.Companion.PLATFORM
import com.dashlane.server.api.UserAgentProvider
import com.dashlane.util.getOsLang
import javax.inject.Inject

class UserAgentProviderImpl @Inject constructor(private val dadadaVersion: DaDaDaVersion) : UserAgentProvider {
    override val appVersionName: String
        get() = dadadaVersion.appVersionName ?: BuildConfig.VERSION_NAME
    override val osVersion: String
        get() = Build.VERSION.RELEASE
    override val partner: String
        get() = PARTNER
    override val platform: String
        get() = PLATFORM
    override val language: String
        get() = getOsLang()
}