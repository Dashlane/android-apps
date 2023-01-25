package com.dashlane.storage.userdata.accessor

import android.database.Cursor
import com.dashlane.database.sql.AuthentifiantSql
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.storage.userdata.SqlQuery
import com.dashlane.storage.userdata.accessor.filter.CredentialFilter
import com.dashlane.storage.userdata.dao.QueryDao
import com.dashlane.util.getStringListFromCursor
import com.dashlane.util.toList
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject



class CredentialDataQueryImplLegacy @Inject constructor(
    private val genericDataQuery: GenericDataQueryImplLegacy,
    private val queryDao: QueryDao,
    private val sessionManager: SessionManager,
    private val userDataRepository: UserDatabaseRepository
) : CredentialDataQuery {

    override fun createFilter() = CredentialFilter()

    override fun queryFirst(filter: CredentialFilter): SummaryObject.Authentifiant? {
        return genericDataQuery.queryFirst(filter) as SummaryObject.Authentifiant?
    }

    override fun queryAllPasswords(): List<String> {
        return getCursor(
            SqlQuery(
                table = AuthentifiantSql.TABLE_NAME,
                columns = listOf(AuthentifiantSql.FIELD_AUTH_PASSWORD)
            )
        )?.use { cursor ->
            cursor.getStringListFromCursor()
        } ?: listOf()
    }

    override fun queryAllUrls(): Set<String> {
        return getCursor(
            SqlQuery(
                table = AuthentifiantSql.TABLE_NAME,
                columns = listOf(AuthentifiantSql.FIELD_URL_DEPRECATED, AuthentifiantSql.FIELD_USER_SELECTED_URL)
            )
        )?.use { cursor ->
            cursor.toList {
                listOfNotNull(getStringNotEmpty(0), getStringNotEmpty(1))
            }.flatten().toSet()
        } ?: setOf()
    }

    @Suppress("UNCHECKED_CAST")
    override fun queryAll(filter: CredentialFilter): List<SummaryObject.Authentifiant> {
        return genericDataQuery.queryAll(filter) as List<SummaryObject.Authentifiant>
    }

    override fun count(filter: CredentialFilter) = genericDataQuery.count(filter)

    private fun Cursor.getStringNotEmpty(columnIndex: Int): String? {
        return getString(columnIndex)?.takeIf { it.isNotBlank() }
    }

    private fun getCursor(sqlQuery: SqlQuery): Cursor? {
        val database = sessionManager.session?.let { userDataRepository.getDatabase(it) } ?: return null
        return queryDao.getCursorForTable(database, sqlQuery)
    }
}