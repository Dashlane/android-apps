package com.dashlane.autofill.formdetector.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class AutoFillFormSource : Parcelable

@Parcelize
data class ApplicationFormSource(
    val packageName: String,
) : AutoFillFormSource()

@Parcelize
data class WebDomainFormSource(
    val packageName: String,
    val webDomain: String,
) : AutoFillFormSource() {
    fun sameDomain(webDomainFormSource: WebDomainFormSource): Boolean {
        return webDomain == webDomainFormSource.webDomain
    }
}