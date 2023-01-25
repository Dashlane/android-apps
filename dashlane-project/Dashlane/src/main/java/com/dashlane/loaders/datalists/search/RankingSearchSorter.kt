package com.dashlane.loaders.datalists.search

import android.content.Context
import com.dashlane.search.ItemType
import com.dashlane.search.Match
import com.dashlane.search.MatchedSearchResult
import com.dashlane.search.Query
import com.dashlane.search.SearchSorter
import com.dashlane.search.SearchableSettingItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType



class RankingSearchSorter(context: Context, fieldMatcher: SearchImprovementsUtils) : SearchSorter {
    private val filter = fieldMatcher.getFilter(context)

    override fun matchAndSort(all: List<Any>, query: String): List<MatchedSearchResult> {
        val matches = findMatches(all, query)
        return matches.sortedWith(
            compareBy(
                
                { it.match.position },
                
                { it.match.field.fieldType },
                
                { it.match.field.itemType },
                
                { it.match.field.order },
                
                { (it.item as? SummaryObject)?.id }
            )
        )
    }

    override fun match(item: Any, query: Query): Boolean = findMatch(item, query.queryString, filter) != null

    private fun findMatches(all: List<Any>, query: String): List<MatchedSearchResult> {

        return all.mapNotNull {
            findMatch(it, query, filter)
        }
    }

    private fun findMatch(item: Any, query: String, filter: Map<Any, List<(Any, String) -> Match?>>): MatchedSearchResult? {

        val match = when (item) {
            is SummaryObject -> {
                filter[item.syncObjectType.toSearchItemType()]?.firstNotNullOfOrNull { field ->
                    field(item, query)
                }
            }
            is SearchableSettingItem -> {
                filter[ItemType.SETTING]?.firstNotNullOfOrNull { field ->
                    field(item, query)
                }
            }
            else -> null
        }
        return if (match != null) {
            MatchedSearchResult(item, match)
        } else {
            null
        }
    }
}

fun SyncObjectType.toSearchItemType(): ItemType = when (this) {
    SyncObjectType.AUTHENTIFIANT -> ItemType.CREDENTIAL
    SyncObjectType.SECURE_NOTE -> ItemType.SECURE_NOTE
    SyncObjectType.BANK_STATEMENT -> ItemType.BANK_STATEMENT
    SyncObjectType.PAYMENT_CREDIT_CARD -> ItemType.CREDIT_CARD
    SyncObjectType.PAYMENT_PAYPAL -> ItemType.PAYPAL
    SyncObjectType.DRIVER_LICENCE -> ItemType.DRIVER_LICENCE
    SyncObjectType.FISCAL_STATEMENT -> ItemType.FISCAL_STATEMENT
    SyncObjectType.ID_CARD -> ItemType.ID_CARD
    SyncObjectType.PASSPORT -> ItemType.PASSPORT
    SyncObjectType.SOCIAL_SECURITY_STATEMENT -> ItemType.SOCIAL_SECURITY_STATEMENT
    SyncObjectType.ADDRESS -> ItemType.ADDRESS
    SyncObjectType.COMPANY -> ItemType.COMPANY
    SyncObjectType.EMAIL -> ItemType.EMAIL
    SyncObjectType.IDENTITY -> ItemType.IDENTITY
    SyncObjectType.PERSONAL_WEBSITE -> ItemType.PERSONAL_WEBSITE
    SyncObjectType.PHONE -> ItemType.PHONE_NUMBER
    else -> {
        ItemType.UNSUPPORTED
    }
}