package com.dashlane.logger.utils

import com.dashlane.debug.services.DaDaDaHermes
import com.dashlane.hermes.ConfigurationUtil
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.util.BuildConfig

class HermesDebugUtil(
    private val dadadaHermes: DaDaDaHermes,
    private val preferencesManager: GlobalPreferencesManager
) : ConfigurationUtil {
    override val allowSendLogs: Boolean
        get() = preferencesManager.allowSendLogs

    override val isAutoFlushEnabled: Boolean
        get() {
            
            return if (BuildConfig.DEBUG) {
                if (!dadadaHermes.isEnabled) {
                    true
                } else {
                    dadadaHermes.hasHermesAutoFlushEnabled()
                }
            } else {
                dadadaHermes.isEnabled && dadadaHermes.hasHermesAutoFlushEnabled()
            }
        }

    override val isVerboseEnabled
        get() = dadadaHermes.isEnabled && dadadaHermes.hasHermesVerboseEnabled()
}