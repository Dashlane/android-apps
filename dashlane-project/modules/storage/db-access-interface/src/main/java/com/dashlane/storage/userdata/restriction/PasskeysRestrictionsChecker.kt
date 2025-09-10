package com.dashlane.storage.userdata.restriction

import com.dashlane.storage.userdata.accessor.filter.PasskeyFilter
import com.dashlane.vault.summary.SummaryObject

interface PasskeysRestrictionsChecker {
    fun canUseCloudPasskeys(): Boolean

    fun canUsePasskey(passkeySummary: SummaryObject.Passkey, filter: PasskeyFilter): Boolean
}