package com.dashlane.util.clipboard.vault

import java.time.LocalDate
import java.time.YearMonth

interface VaultItemVisibleCopyEdgeCases {
    fun shouldCopyDifferentContent(copyField: CopyField): Boolean
    fun mapEnumeration(enumerationContent: String, copyField: CopyField): String?
    fun mapLocalDate(localDate: LocalDate, copyField: CopyField): String?
    fun mapYearMonth(yearMonth: YearMonth, copyField: CopyField): String?
}