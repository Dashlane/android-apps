package com.dashlane.storage.userdata



data class SqlQuery(
    val table: String,
    val columns: List<String>? = null,
    val selection: String? = null,
    val selectionArgs: List<String>? = null,
    val groupBy: String? = null,
    val having: String? = null,
    val orderBy: String? = null,
    val limit: String? = null
) {

    val columnsAsArray: Array<String>?
        get() = columns?.toTypedArray()
    val selectionArgsAsArray: Array<String>?
        get() = selectionArgs?.toTypedArray()

    class Builder(val table: String) {

        var columns: Array<String>? = null
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        var groupBy: String? = null
        var having: String? = null
        var orderBy: String? = null
        var limit: String? = null

        fun columns(columns: Array<String>?) = also { this.columns = columns }

        fun selection(selection: String?) = also { this.selection = selection }

        fun selectionArgs(selectionArgs: Array<String>?) = also { this.selectionArgs = selectionArgs }

        fun groupBy(groupBy: String?) = also { this.groupBy = groupBy }

        fun having(having: String?) = also { this.having = having }

        fun orderBy(orderBy: String?) = also { this.orderBy = orderBy }

        fun limit(limit: String?) = also { this.limit = limit }

        fun build(): SqlQuery {
            return SqlQuery(
                table,
                columns?.toList(),
                selection,
                selectionArgs?.toList(),
                groupBy,
                having,
                orderBy,
                limit
            )
        }
    }
}