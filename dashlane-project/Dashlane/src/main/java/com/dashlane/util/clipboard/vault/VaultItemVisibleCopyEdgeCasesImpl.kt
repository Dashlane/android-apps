package com.dashlane.util.clipboard.vault

import com.dashlane.item.subview.provider.payment.getBankName
import com.dashlane.ui.adapters.text.factory.toIdentityFormat
import com.dashlane.util.BankDataProvider
import com.dashlane.util.isNotSemanticallyNull
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class VaultItemVisibleCopyEdgeCasesImpl @Inject constructor() : VaultItemVisibleCopyEdgeCases {
    override fun shouldCopyDifferentContent(copyField: CopyField): Boolean {
        return when (copyField) {
            CopyField.BankAccountBank,
            CopyField.PassportExpirationDate,
            CopyField.PassportIssueDate,
            CopyField.IdsIssueDate,
            CopyField.IdsExpirationDate,
            CopyField.DriverLicenseIssueDate,
            CopyField.DriverLicenseExpirationDate,
            CopyField.PaymentsExpirationDate -> true
            else -> false
        }
    }

    override fun mapEnumeration(enumerationContent: String, copyField: CopyField): String? {
        if (copyField == CopyField.BankAccountBank) {
            try {
                val bankDescriptor = enumerationContent.takeIf { it.isNotSemanticallyNull() } ?: return null
                return BankDataProvider.instance.getBankName(bankDescriptor, "")
                    .takeIf { it.isNotSemanticallyNull() }
            } catch (e: Exception) {
                return null
            }
        }

        return null
    }

    override fun mapLocalDate(localDate: LocalDate, copyField: CopyField): String? {
        return when (copyField) {
            CopyField.PassportExpirationDate,
            CopyField.PassportIssueDate,
            CopyField.IdsIssueDate,
            CopyField.IdsExpirationDate,
            CopyField.DriverLicenseIssueDate,
            CopyField.DriverLicenseExpirationDate -> localDate.toIdentityFormat()
            else -> null
        }
    }

    override fun mapYearMonth(yearMonth: YearMonth, copyField: CopyField): String? {
        return when (copyField) {
            CopyField.PaymentsExpirationDate -> String.format("%02d - %02d", yearMonth.monthValue, yearMonth.year % 100)
            else -> null
        }
    }
}