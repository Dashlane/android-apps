package com.dashlane.hermes

import com.dashlane.hermes.generated.definitions.Os
import com.dashlane.hermes.generated.definitions.OsType
import java.util.Locale

data class OsInfo(
    val locale: String,
    val version: String,
) {
    constructor(locale: Locale, version: String) : this(locale.stripExtensions().toLanguageTag(), version)
}

fun OsInfo.toOsDefinition() =
    Os(
        type = OsType.ANDROID,
        locale = locale,
        version = version,
    )