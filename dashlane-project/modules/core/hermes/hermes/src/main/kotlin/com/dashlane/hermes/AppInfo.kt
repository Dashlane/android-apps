package com.dashlane.hermes

import com.dashlane.hermes.generated.definitions.App
import com.dashlane.hermes.generated.definitions.BuildType
import com.dashlane.hermes.generated.definitions.Platform

data class AppInfo(val version: String, val buildType: Build, val app: App = App.MAIN) {
    enum class Build {
        PRODUCTION,
        QA,
        DEV,
    }

    enum class App {
        MAIN,
        AUTHENTICATOR,
    }
}

fun AppInfo.toAppDefinition() =
    App(
        version = version,
        buildType =
            when (buildType) {
                AppInfo.Build.PRODUCTION -> BuildType.PRODUCTION
                AppInfo.Build.QA -> BuildType.QA
                else -> BuildType.DEV
            },
        platform =
            when (app) {
                AppInfo.App.MAIN -> Platform.ANDROID
                AppInfo.App.AUTHENTICATOR -> Platform.AUTHENTICATOR_ANDROID
            },
    )