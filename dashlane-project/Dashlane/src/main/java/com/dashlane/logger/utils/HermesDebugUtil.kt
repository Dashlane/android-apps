package com.dashlane.logger.utils

import com.dashlane.debug.DaDaDa
import com.dashlane.hermes.ConfigurationUtil
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.util.BuildConfig



class HermesDebugUtil(
    private val daDaDa: DaDaDa,
    private val preferencesManager: GlobalPreferencesManager
) : ConfigurationUtil {
    override val allowSendLogs: Boolean
        get() = preferencesManager.allowSendLogs

    override val isAutoFlushEnabled: Boolean
        get() {
            
            return if (BuildConfig.DEBUG) {
                if (!daDaDa.isEnabled) {
                    true
                } else {
                    daDaDa.hasHermesAutoFlushEnabled()
                }
            } else {
                daDaDa.isEnabled && daDaDa.hasHermesAutoFlushEnabled()
            }
        }

    override val isVerboseEnabled
        get() = daDaDa.isEnabled && daDaDa.hasHermesVerboseEnabled()
}