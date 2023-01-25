package com.dashlane.ui.screens.fragments.search

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.events.user.SearchVaultItem
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.UsageLogCode32
import com.dashlane.vault.model.DataIdentifierId
import com.dashlane.vault.model.getTableName
import com.dashlane.vault.util.desktopId
import com.dashlane.xml.domain.SyncObjectType

class SearchLogger(private val usageLogRepository: UsageLogRepository?, private val hermesLogRepository: LogRepository) {

    fun logClose(keywords: Int, typedCharCount: Int, resultCount: Int) {
        logUsageLog32(keywords, resultCount, false)

        hermesLogRepository.queueEvent(
            SearchVaultItem(
                totalCount = resultCount,
                hasInteracted = false,
                charactersTypedCount = typedCharCount
            )
        )
    }

    fun logClick(keywordsCount: Int, typedCharCount: Int, resultCount: Int, itemType: SyncObjectType) {
        logUsageLog32(keywordsCount, resultCount, true, itemType.toCategoryNameForLogs(), itemType.getTableName())

        hermesLogRepository.queueEvent(
            SearchVaultItem(
                totalCount = resultCount,
                hasInteracted = true,
                charactersTypedCount = typedCharCount
            )
        )
    }

    private fun logUsageLog32(
        keywords: Int,
        resultCount: Int,
        click: Boolean,
        categoryName: String? = null,
        tableName: String? = null
    ) {
        usageLogRepository?.enqueue(
            UsageLogCode32(
                keywords = keywords,
                results = resultCount,
                click = click,
                service = categoryName,
                subsection = tableName
            )
        )
    }

    private fun SyncObjectType.toCategoryNameForLogs(): String? = when (this.desktopId) {
        DataIdentifierId.AUTH_CATEGORY,
        DataIdentifierId.AUTHENTIFIANT -> "Authentifiant"
        DataIdentifierId.ADDRESS,
        DataIdentifierId.COMPANY,
        DataIdentifierId.DRIVER_LICENCE,
        DataIdentifierId.EMAIL,
        DataIdentifierId.FISCAL_STATEMENT,
        DataIdentifierId.GENERATED_PASSWORD,
        DataIdentifierId.ID_CARD,
        DataIdentifierId.IDENTITY,
        DataIdentifierId.MERCHANT,
        DataIdentifierId.PASSPORT,
        DataIdentifierId.PAYMENT_PAYPAL,
        DataIdentifierId.PAYMENT_CREDIT_CARD,
        DataIdentifierId.PERSONAL_DATA_DEFAULT,
        DataIdentifierId.PERSONAL_WEBSITE,
        DataIdentifierId.PHONE,
        DataIdentifierId.PURCHASE_ARTICLE,
        DataIdentifierId.PURCHASE_BASKET,
        DataIdentifierId.PURCHASE_CATEGORY,
        DataIdentifierId.PURCHASE_CONFIRMATION,
        DataIdentifierId.PURCHASE_PAID_BASKET,
        DataIdentifierId.SOCIAL_SECURITY_STATEMENT,
        DataIdentifierId.SECURE_NOTE,
        DataIdentifierId.SECURE_NOTE_CATEGORY,
        DataIdentifierId.BANK_STATEMENT,
        DataIdentifierId.REACTIVATION_OBJECT -> "Personal data"
        else -> null
    }
}