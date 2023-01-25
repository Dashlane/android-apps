package com.dashlane.autofill.api.request.autofill.database

import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.api.model.AuthentifiantItemToFill
import com.dashlane.autofill.api.model.AuthentifiantSummaryItemToFill
import com.dashlane.autofill.api.model.CreditCardItemToFill
import com.dashlane.autofill.api.model.CreditCardSummaryItemToFill
import com.dashlane.autofill.api.model.EmailItemToFill
import com.dashlane.autofill.api.model.ItemToFill
import com.dashlane.autofill.formdetector.AutoFillFormType
import com.dashlane.hermes.generated.definitions.MatchType
import com.dashlane.search.Query
import com.dashlane.url.UrlDomain
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.util.matchDomain
import com.dashlane.vault.summary.SummaryObject
import java.time.Instant
import javax.inject.Inject

internal interface ItemLoader {
    fun load(
        @AutoFillFormType.FormType formType: Int,
        packageName: String,
        url: String?,
        username: String? = null
    ): List<ItemToFill>?

    fun loadCreditCard(itemId: String?): CreditCardItemToFill?
    fun loadEmail(itemId: String?): EmailItemToFill?
    fun loadAuthentifiant(itemId: String?): AuthentifiantItemToFill?
}

internal class ItemLoaderImpl @Inject constructor(private val databaseAccess: AutofillAnalyzerDef.DatabaseAccess) :
    ItemLoader {

    override fun load(
        @AutoFillFormType.FormType
        formType: Int,
        packageName: String,
        url: String?,
        username: String?
    ): List<ItemToFill>? {
        return when (formType) {
            AutoFillFormType.CREDIT_CARD -> loadForCreditCard()
            AutoFillFormType.CREDENTIAL -> loadForCredential(packageName, url, username)
            AutoFillFormType.EMAIL_ONLY, AutoFillFormType.USERNAME_ONLY -> {
                
                val credentials = loadForCredential(packageName, url, username)
                if (credentials.isNullOrEmpty()) {
                    loadForEmail()
                } else {
                    credentials
                }
            }
            AutoFillFormType.USERNAME_OR_EMAIL -> concat(loadForCredential(packageName, url, username), loadForEmail())
            else -> listOf()
        }?.sort()?.take(10)
    }

    override fun loadCreditCard(itemId: String?): CreditCardItemToFill? {
        itemId ?: return null
        val creditCard = databaseAccess.loadCreditCard(itemId) ?: return null
        return CreditCardItemToFill(
            primaryItem = creditCard,
            optional = creditCard.syncObject.linkedBillingAddress?.let { uid ->
                databaseAccess.loadAddress(uid)
            }
        )
    }

    override fun loadEmail(itemId: String?): EmailItemToFill? {
        itemId ?: return null
        return databaseAccess.loadEmail(itemId)?.let {
            EmailItemToFill(primaryItem = it)
        } ?: return null
    }

    override fun loadAuthentifiant(itemId: String?): AuthentifiantItemToFill? {
        itemId ?: return null
        return databaseAccess.loadAuthentifiant(itemId)?.let {
            AuthentifiantItemToFill(primaryItem = it)
        } ?: return null
    }

    private fun List<ItemToFill>.sort(): List<ItemToFill> {
        return this.sortedWith(
            compareByDescending<ItemToFill> { it.lastUsedDate ?: Instant.now() }
                .thenBy { it.getItemId() }
        )
    }

    private fun loadForEmail(): List<EmailItemToFill>? = databaseAccess.loadEmails()?.map {
        EmailItemToFill(primaryItem = it)
    }

    private fun loadForCredential(
        packageName: String,
        url: String?,
        username: String?
    ): List<AuthentifiantSummaryItemToFill>? {
        databaseAccess.clearCache() 
        val result = if (url.isNullOrBlank()) {
            
            databaseAccess.loadAuthentifiantsByPackageName(packageName)
        } else {
            
            databaseAccess.loadAuthentifiantsByUrl(url)
        }?.map { summaryObject ->
            
            val matchType = when {
                url?.toUrlDomainOrNull()
                    .isMatchFromLinkedDomain(summaryObject.url?.toUrlDomainOrNull()) -> MatchType.ASSOCIATED_WEBSITE
                summaryObject.linkedServices?.associatedDomains?.let { it.any { url?.matchDomain(it.domain) == true } } == true -> MatchType.USER_ASSOCIATED_WEBSITE
                else -> MatchType.REGULAR
            }
            AuthentifiantSummaryItemToFill(summaryObject, matchType = matchType)
        }
        return username?.let {
            result?.filter {
                val item = it.primaryItem as SummaryObject.Authentifiant
                item.email == username || item.login == username
            }
        } ?: result
    }

    

    private fun UrlDomain?.isMatchFromLinkedDomain(other: UrlDomain?): Boolean {

        if (this == null || other == null) return false

        val thisRootString = this.root.value
        val otherRootString = other.root.value

        
        return thisRootString != otherRootString &&
                Query(queryString = thisRootString).matchLinkedDomains(otherRootString)
    }

    private fun loadForCreditCard(): List<CreditCardSummaryItemToFill>? {
        val creditCards = databaseAccess.loadCreditCards() ?: return null
        return loadCreditCardAddresses(creditCards)
    }

    private fun loadCreditCardAddresses(creditCards: List<SummaryObject.PaymentCreditCard>): List<CreditCardSummaryItemToFill> {
        
        val billingAddressUids = creditCards.mapNotNull { it.linkedBillingAddress }.toSet()
        val uidToAddress = databaseAccess.loadAddresses(billingAddressUids)
            ?.associateBy { it.id }

        return creditCards.map {
            CreditCardSummaryItemToFill(
                primaryItem = it,
                optional = it.linkedBillingAddress?.let { uid -> uidToAddress?.get(uid) }
            )
        }
    }

    private fun <T> concat(list1: List<T>?, list2: List<T>?): List<T>? {
        if (list1 == null) return list2
        if (list2 == null) return list1
        return list1 + list2
    }
}