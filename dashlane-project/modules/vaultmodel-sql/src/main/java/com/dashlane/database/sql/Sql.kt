package com.dashlane.database.sql

interface Sql {
    val tableName: String
    val databaseColumns: String?
    val createStatement: String
}