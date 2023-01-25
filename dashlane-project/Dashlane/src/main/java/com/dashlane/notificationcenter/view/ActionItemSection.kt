package com.dashlane.notificationcenter.view

import androidx.annotation.StringRes
import com.dashlane.R

enum class ActionItemSection(@StringRes val titleRes: Int) {
    BREACH_ALERT(R.string.section_header_breach_alert_title_no_count),
    SHARING(R.string.section_header_sharing_title_no_count),
    PROMOTIONS(R.string.section_header_promotions_title_no_count),
    GETTING_STARTED(R.string.section_header_getting_started_title_no_count),
    YOUR_ACCOUNT(R.string.section_header_your_account_title_no_count),
    WHATS_NEW(R.string.section_header_whats_new_title_no_count)
}