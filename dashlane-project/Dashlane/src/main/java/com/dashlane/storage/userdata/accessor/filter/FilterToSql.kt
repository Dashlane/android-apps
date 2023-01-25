package com.dashlane.storage.userdata.accessor.filter

import android.content.Context
import com.dashlane.database.sql.AuthentifiantSql
import com.dashlane.database.sql.DataIdentifierSql
import com.dashlane.database.util.AuthentifiantSqlRequestUtil
import com.dashlane.database.util.DataIdentifierSqlRequestUtil
import com.dashlane.storage.userdata.SqlQuery
import com.dashlane.storage.userdata.accessor.filter.sharing.SharingFilter
import com.dashlane.storage.userdata.accessor.filter.status.StatusFilter
import com.dashlane.storage.userdata.accessor.filter.uid.UidFilter
import com.dashlane.util.model.UserPermission
import com.dashlane.vault.model.getTableName
import com.dashlane.vault.util.SyncObjectTypeUtils
import com.dashlane.xml.domain.SyncObjectType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject



class FilterToSql @Inject constructor(@ApplicationContext private val context: Context) {

    

    fun appendToSqlQuery(sqlQuery: SqlQuery, filter: BaseFilter, limitToOne: Boolean): SqlQuery? {
        val tableName = sqlQuery.table
        val dataType = filter.dataTypes.firstOrNull { it.getTableName() == tableName } ?: return null

        val whereStatementBuilder = StringBuilder()
        val queryValues = mutableListOf<String>()

        
        sqlQuery.selection?.let { whereStatementBuilder.append(it) }
        sqlQuery.selectionArgs?.let { queryValues.addAll(it) }

        whereStatementBuilder.startWhereStatement()

        
        whereStatementBuilder.filterByStatus(filter, tableName)

        whereStatementBuilder.filterByUid(filter)

        
        whereStatementBuilder.filterBySharingPermission(filter, dataType)

        whereStatementBuilder.appendSubFilterSpecific(context, tableName, filter, queryValues)

        
        whereStatementBuilder.endWhereStatement()
        return sqlQuery.copy(
            limit = if (limitToOne) "1" else sqlQuery.limit, 
            selection = whereStatementBuilder.toString(),
            selectionArgs = queryValues
        )
    }
}

private fun StringBuilder.startWhereStatement() {
    if (isEmpty()) {
        append("(")
    } else {
        
        append(" AND (")
    }
}

private fun StringBuilder.endWhereStatement() {
    append(") ")
}

private fun StringBuilder.filterBySharingPermission(filter: BaseFilter, dataType: SyncObjectType) {
    (filter as? SharingFilter)?.let {
        it.sharingPermissions?.let { permission ->
            appendWhereInSharingPermission(dataType, permission)
        }
    }
}

private fun StringBuilder.filterByUid(filter: BaseFilter) {
    (filter as? UidFilter)?.onlyOnUids?.let {
        if (isNotEmpty()) {
            append(" AND ")
        }
        append("(")
        var firstUid = true
        it.forEach {
            if (firstUid) {
                firstUid = false
            } else {
                append(" OR ")
            }
            append(DataIdentifierSql.getWhereStatement(it))
        }
        append(")")
    }
}

private fun StringBuilder.appendSubFilterSpecific(
    context: Context,
    tableName: String,
    filter: BaseFilter,
    queryValues: MutableList<String>
) {
    if (filter is CredentialFilter && tableName == AuthentifiantSql.TABLE_NAME) {
        appendCredentialFilterSpecific(context, filter, queryValues)
    }
}

private fun StringBuilder.appendCredentialFilterSpecific(
    context: Context,
    filter: CredentialFilter,
    queryValues: MutableList<String>
) {

    
    filter.domains?.let {
        append(" AND ")
        append(AuthentifiantSqlRequestUtil.getUrlFilter(filter.allowSimilarDomains, queryValues, it))
    }
    filter.packageName?.let {
        append(" AND ")
        append(AuthentifiantSqlRequestUtil.getPackageNameFilter(context, it, queryValues))
    }
    filter.email?.let {
        append(" AND LOWER(${AuthentifiantSql.FIELD_AUTH_EMAIL}) = ?")
        queryValues.add(it.lowercase())
    }
}

private fun StringBuilder.appendWhereInSharingPermission(
    dataType: SyncObjectType,
    permissions: Array<out String>
) {

    
    if (!SyncObjectTypeUtils.SHAREABLE.contains(dataType)) return

    if (isNotEmpty()) {
        append(" AND ")
    }
    append("(")
    permissions
        .toSet() 
        .joinTo(this, separator = " OR ") { permission ->
            if (permission == UserPermission.UNDEFINED) {
                "${DataIdentifierSql.FIELD_SHARING_PERMISSION} IS NULL"
            } else {
                "${DataIdentifierSql.FIELD_SHARING_PERMISSION} = '$permission'"
            }
        }
    append(")")
}



private fun StringBuilder.filterByStatus(filter: BaseFilter, tableName: String) {
    (filter as StatusFilter).onlyVisibleStatus.let { onlyVisibleStatus ->
        if (onlyVisibleStatus) {
            DataIdentifierSqlRequestUtil.appendGetDefaultShowList(this, tableName)
        } else {
            append("1=1")
        }
    }
}