package com.dashlane.network

interface NitroUrlOverride {
    val nitroUrl: String?
    val nitroStagingEnabled: Boolean
    val cloudFlareNitroClientId: String
    val cloudFlareNitroSecret: String
}