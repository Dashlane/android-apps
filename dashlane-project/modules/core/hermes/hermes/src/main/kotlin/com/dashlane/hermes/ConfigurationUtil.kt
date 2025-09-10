package com.dashlane.hermes

object DefaultConfigurationUtil : ConfigurationUtil

interface ConfigurationUtil {
    val isVerboseEnabled: Boolean
        get() = false

    val isAutoFlushEnabled: Boolean
        get() = false

    val allowSendLogs: Boolean
        get() = true
}