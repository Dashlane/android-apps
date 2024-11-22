package com.dashlane.autofill.request.autofill.database

import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.model.AuthentifiantItemToFill
import com.dashlane.autofill.model.CreditCardItemToFill
import com.dashlane.autofill.model.EmailItemToFill
import com.dashlane.autofill.model.ItemToFill
import com.dashlane.autofill.model.toItemToFill
import com.dashlane.autofill.formdetector.AutoFillFormType
import com.dashlane.hermes.generated.definitions.MatchType
import com.dashlane.search.Query
import com.dashlane.url.UrlDomain
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.util.matchDomain
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import java.time.Instant
import javax.inject.Inject

internal interface ItemLoader {
    fun loadSuggestions(
        @AutoFillFormType.FormType formType: Int,
        packageName: String,
        url: String?,
        username: String? = null
    ): List<ItemToFill>?

    fun loadSyncObject(itemId: String): VaultItem<SyncObject>?
}

internal class ItemLoaderImpl @Inject constructor(private val databaseAccess: AutofillAnalyzerDef.DatabaseAccess) :
    ItemLoader {

    override fun loadSuggestions(
        @AutoFillFormType.FormType
        formType: Int,
        packageName: String,
        url: String?,
        username: String?
    ): List<ItemToFill>? {
        return when (formType) {
            AutoFillFormType.CREDIT_CARD -> loadCreditCardSummary()
            AutoFillFormType.CREDENTIAL -> loadCredentialSummary(packageName, url, username)
            AutoFillFormType.EMAIL_ONLY, AutoFillFormType.USERNAME_ONLY -> {
                
                val credentials = loadCredentialSummary(packageName, url, username)
                if (credentials.isNullOrEmpty()) {
                    loadEmailSummary()
                } else {
                    credentials
                }
            }
            AutoFillFormType.USERNAME_OR_EMAIL -> concat(
                loadCredentialSummary(packageName, url, username),
                loadEmailSummary()
            )
            else -> listOf()
        }?.sort()?.take(10)
    }

    override fun loadSyncObject(itemId: String): VaultItem<SyncObject>? {
        return databaseAccess.loadSyncObject(itemId)
    }

    private fun List<ItemToFill>.sort(): List<ItemToFill> {
        return this.sortedWith(
            compareByDescending<ItemToFill> { it.lastUsedDate ?: Instant.now() }.thenBy { it.itemId }
        )
    }

    private fun loadEmailSummary(): List<EmailItemToFill>? =
        databaseAccess.loadSummaries<SummaryObject.Email>(SyncObjectType.EMAIL)?.map {
            it.toItemToFill()
        }

    private fun loadCredentialSummary(
        packageName: String,
        url: String?,
        username: String?
    ): List<AuthentifiantItemToFill>? {
        databaseAccess.clearCache() 
        val result = if (url.isNullOrBlank()) {
            
            databaseAccess.loadAuthentifiantsByPackageName(packageName)
        } else {
            
            databaseAccess.loadAuthentifiantsByUrl(url)
        }?.filter {
            if (username == null) {
                true
            } else {
                it.email == username || it.login == username
            }
        }?.map { summaryObject ->
            
            val matchType = when {
                url?.toUrlDomainOrNull()
                    .isMatchFromLinkedDomain(summaryObject.url?.toUrlDomainOrNull()) -> MatchType.ASSOCIATED_WEBSITE
                summaryObject.linkedServices?.associatedDomains?.let { it.any { url?.matchDomain(it.domain) == true } } == true -> MatchType.USER_ASSOCIATED_WEBSITE
                else -> MatchType.REGULAR
            }
            summaryObject.toItemToFill(matchType)
        }
        return result
    }

    private fun UrlDomain?.isMatchFromLinkedDomain(other: UrlDomain?): Boolean {
        if (this == null || other == null) return false

        val thisRootString = this.root.value
        val otherRootString = other.root.value

        
        return thisRootString != otherRootString &&
                Query(queryString = thisRootString).matchLinkedDomains(otherRootString)
    }

    private fun loadCreditCardSummary(): List<CreditCardItemToFill>? =
        databaseAccess.loadSummaries<SummaryObject.PaymentCreditCard>(SyncObjectType.PAYMENT_CREDIT_CARD)?.map {
            val zipCode = it.linkedBillingAddress?.let { addressUid ->
                databaseAccess.loadSummary<SummaryObject.Address>(addressUid)?.zipCode
            }
            it.toItemToFill(zipCode = zipCode)
        }

    private fun <T> concat(list1: List<T>?, list2: List<T>?): List<T>? {
        if (list1 == null) return list2
        if (list2 == null) return list1
        return list1 + list2
    }
}