package com.dashlane.csvimport.utils

import androidx.annotation.StringDef

object Intents {
    internal const val ACTION_CSV_IMPORT = "com.dashlane.chromeimport.action.CSV_IMPORT"
    internal const val EXTRA_CSV_IMPORT_RESULT = "result"

    internal const val CSV_IMPORT_RESULT_SUCCESS = "success"
    internal const val CSV_IMPORT_RESULT_FAILURE = "failure"
    internal const val CSV_IMPORT_RESULT_CANCEL = "cancel"
    internal const val CSV_IMPORT_RESULT_ADD_INDIVIDUALLY = "add_individually"

    @StringDef(
        CSV_IMPORT_RESULT_SUCCESS,
        CSV_IMPORT_RESULT_FAILURE,
        CSV_IMPORT_RESULT_CANCEL,
        CSV_IMPORT_RESULT_ADD_INDIVIDUALLY
    )
    internal annotation class CsvImportResult
}