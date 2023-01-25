package com.dashlane.storage.userdata

import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType
import java.util.Locale



class EmailSuggestionProviderImpl(private val mainDataAccessor: MainDataAccessor) :
    EmailSuggestionProvider {
    private val genericDataQuery: GenericDataQuery
        get() = mainDataAccessor.getGenericDataQuery()

    override fun getAllEmails(): List<String> {
        val filter = genericFilter { specificDataType(SyncObjectType.AUTHENTIFIANT, SyncObjectType.EMAIL) }
        val emails = genericDataQuery.queryAll(filter).mapNotNull {
            when (it) {
                is SummaryObject.Authentifiant -> it.email
                is SummaryObject.Email -> it.email
                else -> null
            }
        }.map { it.lowercase(Locale.US) }
        val count = emails.groupingBy { it }.eachCount()
        return emails.distinct().sortedByDescending { count[it] }
    }
}