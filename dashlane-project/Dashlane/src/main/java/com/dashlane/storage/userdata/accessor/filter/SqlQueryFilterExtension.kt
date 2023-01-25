package com.dashlane.storage.userdata.accessor.filter

import com.dashlane.storage.userdata.SqlQuery



fun SqlQuery.copyWithFilter(filterToSql: FilterToSql, filter: BaseFilter, limitToOne: Boolean = false): SqlQuery? {
    return filterToSql.appendToSqlQuery(this.copy(), filter, limitToOne)
}