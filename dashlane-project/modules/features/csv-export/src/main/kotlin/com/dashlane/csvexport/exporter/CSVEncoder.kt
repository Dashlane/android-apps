package com.dashlane.csvexport.exporter

class CSVEncoder(delimiter: Char = ',') {
    private val delimiterCharSequence = delimiter.toString()
    private val escapingCharacters = listOf(delimiter, '\t', '\b', '\n', '\r', '"', '\'', '\\', '$')

    fun encodeRaw(data: List<String?>): String {
        return data.joinToString(separator = delimiterCharSequence) { escapeValue(it) }
    }

    private fun escapeValue(value: String?): String {
        value ?: return ""
        val needsEscaping = value.any { it in escapingCharacters }
        return if (needsEscaping) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}