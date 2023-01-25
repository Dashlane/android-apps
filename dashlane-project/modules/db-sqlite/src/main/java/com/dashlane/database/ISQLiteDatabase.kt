package com.dashlane.database

import android.content.ContentValues
import android.database.Cursor

interface ISQLiteDatabase {
    val isOpen: Boolean

    val version: Int

    fun close()

    fun insert(
        table: String,
        nullColumnHack: String? = null,
        values: ContentValues?
    ): Long

    fun insertWithOnConflict(
        table: String,
        nullColumnHack: String? = null,
        initialValues: ContentValues? = null,
        conflictAlgorithm: Int = 0
    ): Long

    fun delete(
        table: String,
        whereClause: String? = null,
        whereArgs: Array<String?>? = null
    ): Int

    fun update(
        table: String,
        values: ContentValues? = null,
        whereClause: String? = null,
        whereArgs: Array<String?>? = null
    ): Int

    fun query(
        table: String,
        columns: Array<String?>? = null,
        selection: String? = null,
        selectionArgs: Array<String?>? = null,
        groupBy: String? = null,
        having: String? = null,
        orderBy: String? = null,
        limit: String? = null
    ): Cursor?

    fun rawQuery(sql: String?, selectionArgs: Array<String?>?): Cursor?
    fun execSQL(sql: String?)
    fun rawExecSQL(sql: String?)
    fun beginTransaction()
    fun setTransactionSuccessful()
    fun endTransaction()
    fun inTransaction(): Boolean
}