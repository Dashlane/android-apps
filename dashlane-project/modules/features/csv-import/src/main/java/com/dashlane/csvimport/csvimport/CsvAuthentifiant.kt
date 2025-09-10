package com.dashlane.csvimport.csvimport

import com.dashlane.core.helpers.AppSignature

data class CsvAuthentifiant(
    val appSignatures: List<AppSignature>? = null,
    val deprecatedUrl: String? = null,
    val email: String? = "",
    val login: String? = "",
    val password: String? = null,
    val title: String? = null,
    val selected: Boolean = true,
) {
    override fun toString() = "CsvAuthentifiant(██)" 
}