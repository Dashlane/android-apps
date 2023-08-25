package com.dashlane.authentication

interface DeviceRegistrationInfo {
    val appVersion: String
    val deviceName: String
    val country: String
    val osCountry: String
    val language: String
    val osLanguage: String
    val installOrigin: String?
}