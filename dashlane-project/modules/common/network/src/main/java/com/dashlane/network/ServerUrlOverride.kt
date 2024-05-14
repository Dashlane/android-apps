package com.dashlane.network

interface ServerUrlOverride {
    val serverUrl: String?
    val apiUrl: String?
    val stagingEnabled: Boolean
    val cloudFlareClientId: String
    val cloudFlareSecret: String
}