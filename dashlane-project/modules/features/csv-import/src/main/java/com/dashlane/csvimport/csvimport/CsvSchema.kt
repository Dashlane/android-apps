package com.dashlane.csvimport.csvimport

data class CsvSchema(
    val hasHeader: Boolean,
    val separator: Char,
    val fieldTypes: List<FieldType?>
) {
    enum class FieldType {
        URL,
        USERNAME,
        PASSWORD
    }
}