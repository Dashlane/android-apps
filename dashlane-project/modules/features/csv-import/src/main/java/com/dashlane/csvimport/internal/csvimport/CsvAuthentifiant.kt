package com.dashlane.csvimport.internal.csvimport

import android.os.Parcelable
import com.dashlane.core.helpers.AppSignature
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class CsvAuthentifiant(
    val appSignatures: List<AppSignature>? = null,
    val deprecatedUrl: String? = null,
    val email: String? = "",
    val login: String? = "",
    val password: String? = null,
    val title: String? = null,
    val selected: Boolean = true
) : Parcelable {
    override fun toString() = "CsvAuthentifiant(██)" 
}