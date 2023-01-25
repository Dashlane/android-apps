package com.dashlane.ui.screens.activities.onboarding.inapplogin

import androidx.annotation.Keep
import com.dashlane.useractivity.log.usage.UsageLogCode95

@Keep
enum class OnboardingType(val usageLog95Type: UsageLogCode95.Type) {
    ACCESSIBILITY(UsageLogCode95.Type.DASHLANE),
    AUTO_FILL_API(UsageLogCode95.Type.AUTOFILL_API);
}