package com.dashlane.csvimport

import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.UsageLogCode75



class ImportFromChromeLogger(
    private val usageLogRepository: UsageLogRepository?,
    private val origin: UsageLogCode75.Origin? = null,
    private val originStr: String? = null
) {

    fun logOnboardingDisplayed() = log(
        subtype = "chrome_android_csv",
        action = "show_onboarding_screen"
    )

    fun logOnboardingOpenChromeClicked() = log(
        subtype = "chrome_android_csv",
        action = "click_open_chrome_on_onboarding_screen"
    )

    fun logOnboardingSkipClicked() = log(
        subtype = "chrome_android_csv",
        action = "click_skip_on_onboarding_screen"
    )

    fun logOnboardingErrorDisplayed() = log(
        subtype = "chrome_android_csv",
        action = "show_error_onboarding_screen"
    )

    fun logOnboardingErrorSkipClicked() = log(
        subtype = "chrome_android_csv",
        action = "click_skip_after_error_message_onboarding_screen"
    )

    fun logOnboardingErrorRetryClicked() = log(
        subtype = "chrome_android_csv",
        action = "click_retry_after_error_message_onboarding_screen"
    )

    fun logCsvImportDisplayed() = log(
        subtype = "chrome_android_csv",
        action = "show_importable_credentials"
    )

    fun logCsvImportParseResult(newPasswords: Int = -1) = log(
        subtype = "chrome_android_csv",
        action = "import_results_details",
        subaction = newPasswords.toString()
    )

    fun logCsvImportImportAllClicked() = log(
        subtype = "chrome_android_csv",
        action = "click_import_all_on_importable_credentials_screen"
    )

    fun logCsvImportCancelClicked() = log(
        subtype = "chrome_android_csv",
        action = "click_cancel_on_importable_credentials_screen"
    )

    fun logCsvImportAddManuallyClicked() = log(
        subtype = "chrome_android_csv",
        action = "click_add_manually_after_error_message_onboarding_screen"
    )

    private fun log(
        subtype: String,
        action: String? = null,
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