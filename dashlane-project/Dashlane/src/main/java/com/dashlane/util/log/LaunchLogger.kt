package com.dashlane.util.log

import android.content.Context
import android.content.pm.PackageManager
import com.dashlane.authenticator.AuthenticatorAppConnection
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.events.user.FirstLaunch
import com.dashlane.hermes.generated.events.user.PasswordManagerLaunch
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.util.getPackageInfoCompat
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@Reusable
class LaunchLogger @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationCoroutineScope private val coroutineScope: CoroutineScope,
    @IoCoroutineDispatcher private val dispatcher: CoroutineDispatcher,
    private val logRepository: LogRepository,
    private val preferencesManager: GlobalPreferencesManager,
    private val authenticatorAppConnection: AuthenticatorAppConnection,
    private val attributionsLogDataProvider: AttributionsLogDataProvider
) {
    fun logLaunched() {
        val isFirstInstall = try {
            val info = context.packageManager.getPackageInfoCompat("com.dashlane", 0)
            info.firstInstallTime == info.lastUpdateTime
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }

        if (!isFirstInstall || preferencesManager.isFirstPasswordManagerLogSent) return

        preferencesManager.isFirstPasswordManagerLogSent = true

        coroutineScope.launch(dispatcher) {
            awaitAll(
                async { logFirstLaunch() },
                async { logPasswordManagerLaunched() }
            )
        }
    }

    private suspend fun logPasswordManagerLaunched() {
        logRepository.queueEvent(
            PasswordManagerLaunch(
                isFirstLaunch = true, 
                hasAuthenticatorInstalled = authenticatorAppConnection.hasAuthenticatorInstalled,
                authenticatorOtpCodesCount = authenticatorAppConnection.run {
                    if (hasAuthenticatorInstalled) {
                        getOtpForBackupCountAsync().await()
                    } else {
                        
                        null
                    }
                } ?: 0
            )
        )
    }

    private suspend fun logFirstLaunch() {
        val attributionLogData = attributionsLogDataProvider.getAttributionLogData()

        logRepository.queueEvent(
            FirstLaunch(
                isMarketingOptIn = attributionLogData.isMarketingOptIn,
                android = attributionLogData.androidAttribution
            )
        )
    }
}
