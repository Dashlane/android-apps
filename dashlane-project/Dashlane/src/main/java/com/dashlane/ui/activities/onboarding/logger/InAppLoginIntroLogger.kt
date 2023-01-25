package com.dashlane.ui.activities.onboarding.logger

import com.dashlane.useractivity.log.usage.UsageLogCode131



interface InAppLoginIntroLogger {

    fun logSkip(type: UsageLogCode131.Type)

    fun logBack(type: UsageLogCode131.Type)

    fun logActivateAutofill()

    fun logShowInAppLoginScreen()
}