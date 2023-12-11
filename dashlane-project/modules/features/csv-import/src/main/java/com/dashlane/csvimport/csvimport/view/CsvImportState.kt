package com.dashlane.csvimport.csvimport.view

import com.dashlane.csvimport.csvimport.CsvAuthentifiant

sealed class CsvImportState {
    abstract val data: CsvImportStateData

    data class Initial(override val data: CsvImportStateData) : CsvImportState()
    data class OpenMatchActivity(
        override val data: CsvImportStateData,
        val fields: List<String>,
        val separator: Char
    ) : CsvImportState()

    data class Matching(override val data: CsvImportStateData, val separator: Char) : CsvImportState()

    data class Loading(override val data: CsvImportStateData) : CsvImportState()
    data class Loaded(
        override val data: CsvImportStateData,
        val saving: Boolean
    ) : CsvImportState()

    data class Error(override val data: CsvImportStateData, val result: String) : CsvImportState()
    data class Saved(override val data: CsvImportStateData, val count: Int) : CsvImportState()
}

data class CsvImportStateData(
    val selectedCredentials: List<CsvAuthentifiant>
)