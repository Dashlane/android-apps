package com.dashlane.csvimport.internal.customcsvimport

internal data class CustomCsvImportViewState(
    val items: List<CustomCsvImportItem>,
    val position: Int,
    val canValidate: Boolean
)