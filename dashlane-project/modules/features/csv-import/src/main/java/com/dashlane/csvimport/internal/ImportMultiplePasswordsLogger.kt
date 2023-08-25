package com.dashlane.csvimport.internal

import androidx.lifecycle.SavedStateHandle
import com.dashlane.csvimport.CustomCsvImportActivity
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject

class ImportMultiplePasswordsLogger(
    private val usageLogRepository: UsageLogRepository?,
    private val origin: UsageLogCode75.Origin? = null,
    private val originStr: String? = null
) {
    @Inject
    constructor(usageLogRepository: UsageLogRepository?, savedStateHandle: SavedStateHandle) : this(
        usageLogRepository,
        originStr = savedStateHandle.get<String>(CustomCsvImportActivity.EXTRA_ORIGIN) ?: ""
    )

    fun logImportMethodsDisplayed() = log(
        subtype = "android_import_multiple_passwords",
        action = "show_import_methods"
    )

    fun logImportMethodsFromChromeClicked() = log(
        subtype = "android_import_multiple_passwords",
        action = "click_import_method_chrome"
    )

    fun logImportMethodsFromM2dClicked() = log(
        subtype = "android_import_multiple_passwords",
        action = "click_import_method_m2d"
    )

    fun logImportMethodsFromCsvClicked() = log(
        subtype = "android_import_multiple_passwords",
        action = "click_import_method_csv"
    )

    fun logImportMethodsFromCompetitorClicked() = log(
        subtype = "android_import_multiple_passwords",
        action = "click_import_method_competitor"
    )

    fun logImportMethodsManuallyClicked() = log(
        subtype = "android_import_multiple_passwords",
        action = "click_import_method_manual"
    )

    fun logCsvFileImportDisplayed() = log(
        subtype = "android_import_multiple_passwords",
        action = "show_csv_file_import"
    )

    fun logCsvFileImportCancelClicked() = log(
        subtype = "android_import_multiple_passwords",
        action = "click_cancel_csv_file_import"
    )

    fun logCsvFileImportFileSelected() = log(
        subtype = "android_import_multiple_passwords",
        action = "pick_file_csv_file_import"
    )

    fun logCustomCsvImportDisplayed(fieldCount: Int) = log(
        subtype = "android_import_multiple_passwords",
        action = "show_custom_csv_import",
        subaction = fieldCount.toString()
    )

    fun logCustomCsvImportItemSelected(index: Int) = log(
        subtype = "android_import_multiple_passwords",
        action = "select_question_custom_csv_import",
        subaction = index.toString()
    )

    fun logCustomCsvImportCancelClicked() = log(
        subtype = "android_import_multiple_passwords",
        action = "click_cancel_custom_csv_import"
    )

    fun logCustomCsvImportValidateClicked() = log(
        subtype = "android_import_multiple_passwords",
        action = "click_validate_custom_csv_import"
    )

    fun logCsvImportDisplayed() = log(
        subtype = "android_import_multiple_passwords",
        action = "show_csv_import"
    )

    fun logCsvImportAllClicked() = log(
        subtype = "android_import_multiple_passwords",
        action = "click_import_csv_import"
    )

    fun logCsvImporCancelClicked() = log(
        subtype = "android_import_multiple_passwords",
        action = "click_cancel_csv_import"
    )

    fun logCsvImportResults(foundPasswordCount: Int = -1) = log(
        subtype = "android_import_multiple_passwords",
        action = "import_results_details",
        subaction = foundPasswordCount.toString()
    )

    private fun log(
        subtype: String,
        action: String,
        subaction: String? = null
    ) {
        usageLogRepository?.enqueue(
            UsageLogCode75(
                origin = origin,
                originStr = originStr,
                type = "import_credential",
                subtype = subtype,
                action = action,
                subaction = subaction
            )
        )
    }
}