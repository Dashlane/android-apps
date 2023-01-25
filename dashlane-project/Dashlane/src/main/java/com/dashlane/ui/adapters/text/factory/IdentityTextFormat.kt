package com.dashlane.ui.adapters.text.factory

import com.dashlane.R
import com.dashlane.util.TextUtil
import java.time.LocalDate

fun LocalDate.toIdentityFormat(): String? =
    TextUtil.formatDate(this, R.string.date_format_identity)
