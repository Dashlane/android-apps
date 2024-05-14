package com.dashlane.util.date

import java.time.Instant

interface RelativeDateFormatter {
    fun format(instant: Instant): String
}