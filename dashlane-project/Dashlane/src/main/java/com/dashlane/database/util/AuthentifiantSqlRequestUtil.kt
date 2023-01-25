package com.dashlane.database.util

import android.content.Context
import com.dashlane.core.helpers.PackageNameSignatureHelper
import com.dashlane.database.sql.AuthentifiantSql
import com.dashlane.util.PackageUtilities.getKeywords
import com.dashlane.util.SearchKeywordUtils
import java.util.Locale



object AuthentifiantSqlRequestUtil {
    private val FIELDS_IN_APP_LOGIN_SEARCH_KEYWORD_INTO = arrayOf(
        AuthentifiantSql.FIELD_TITLE,
        AuthentifiantSql.FIELD_TRUSTED_URL,
        AuthentifiantSql.FIELD_URL_DEPRECATED,
        AuthentifiantSql.FIELD_USER_SELECTED_URL
    )

    fun getPackageNameFilter(
        context: Context,
        packageName: String,
        queryValues: MutableList<String>
    ): String {
        val keywordsToQuery = getKeywords(context, packageName)
        val keywordsWithPercent: MutableList<String> =
            ArrayList(keywordsToQuery.size)
        for (i in keywordsToQuery.indices) {
            keywordsWithPercent.add("%${keywordsToQuery[i]}%")
        }
        val whereRequestBuilder = StringBuilder("(")
        var first = true
        
        for (field in FIELDS_IN_APP_LOGIN_SEARCH_KEYWORD_INTO) {
            for (i in keywordsWithPercent.indices) {
                if (!first) {
                    whereRequestBuilder.append(OR)
                }
                whereRequestBuilder.append(UPPER).append(field).append(LIKE_STATEMENT)
                queryValues.add(keywordsWithPercent[i])
                first = false
            }
        }

        
        if (!first) {
            whereRequestBuilder.append(OR)
        }
        whereRequestBuilder.append("(")
        whereRequestBuilder.append(AuthentifiantSql.FIELD_AUTH_META).append(" LIKE ? AND ")
        queryValues.add("%\"${PackageNameSignatureHelper.KEY_PACKAGE_NAME}\"%")
        whereRequestBuilder.append(AuthentifiantSql.FIELD_AUTH_META).append(" LIKE ?")
        queryValues.add("%\"$packageName\"%")
        whereRequestBuilder.append(")")

        
        whereRequestBuilder.append(")")
        return whereRequestBuilder.toString()
    }

    fun getUrlFilter(
        withLinkedDomain: Boolean,
        queryValues: MutableList<String>,
        domains: Array<out String>
    ): String {
        val selectionBuilder = StringBuilder()
        selectionBuilder.append("(")
        for (domain in domains) {
            appendUrlFilter(selectionBuilder, withLinkedDomain, queryValues, domain)
        }
        if (selectionBuilder.length == 1) {
            return "" 
        }
        selectionBuilder.append(")")
        return selectionBuilder.toString()
    }

    private fun appendUrlFilter(
        selectionBuilder: StringBuilder,
        withLinkedDomain: Boolean,
        queryValues: MutableList<String>,
        domain: String
    ) {
        val searchKeywords = SearchKeywordUtils.fromUrl(domain, withLinkedDomain)

        for (keyword in searchKeywords.distinct()) {
            if (selectionBuilder.length > 1) {
                selectionBuilder.append(OR)
            }

            

            
            
            selectionBuilder.append("(")
            addSearchField(AuthentifiantSql.FIELD_URL_DEPRECATED, selectionBuilder, queryValues, keyword)

            
            selectionBuilder.append(OR)
            addSearchField(AuthentifiantSql.FIELD_USER_SELECTED_URL, selectionBuilder, queryValues, keyword)

            
            selectionBuilder.append(OR)
            addSearchField(AuthentifiantSql.FIELD_TITLE, selectionBuilder, queryValues, keyword)

            
            selectionBuilder.append(")")
        }
    }

    private fun addSearchField(
        field: String,
        selectionBuilder: StringBuilder,
        queryValues: MutableList<String>,
        keyword: String
    ) {
        selectionBuilder.append(UPPER).append(field).append(LIKE_STATEMENT)
        queryValues.add("%.${keyword.uppercase(Locale.US)}%")
        
        selectionBuilder.append("$OR$UPPER").append(field).append(LIKE_STATEMENT)
        queryValues.add("%/${keyword.uppercase(Locale.US)}%")
        
        selectionBuilder.append("$OR$UPPER").append(field).append(LIKE_STATEMENT)
        queryValues.add("${keyword.uppercase(Locale.US)}%")
    }

    private const val LIKE_STATEMENT = ") LIKE ?)"
    private const val OR = " OR "
    private const val UPPER = "(UPPER("
}