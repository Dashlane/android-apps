package com.dashlane.csvimport.matchcsvfields

internal data class MatchCsvFieldsViewState(
    val items: List<MatchCsvFieldsItem>,
    val position: Int,
    val canValidate: Boolean
)