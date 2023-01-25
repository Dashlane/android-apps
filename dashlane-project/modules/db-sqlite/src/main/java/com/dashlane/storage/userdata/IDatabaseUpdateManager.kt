package com.dashlane.storage.userdata

import net.sqlcipher.database.SQLiteDatabase

interface IDatabaseUpdateManager {
    fun createDatabase(db: SQLiteDatabase)
    fun migrateDatabase(db: SQLiteDatabase, oldVersion: Int, newVersion: Int)
}