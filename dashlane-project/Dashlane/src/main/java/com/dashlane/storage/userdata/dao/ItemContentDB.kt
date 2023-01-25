package com.dashlane.storage.userdata.dao

import android.database.Cursor



data class ItemContentDB(
    val id: String,
    val timestamp: Long,
    val extraData: String,
    val itemKeyBase64: String
)

fun Cursor.toItemContentDBList(): List<ItemContentDB> =
    this.use {
        val idColumnIndex = it.getColumnIndex(SharingDataType.ColumnName.ITEM_ID)
        val timestampColumnIndex = it.getColumnIndex(SharingDataType.ColumnName.ITEM_TIMESTAMP)
        val extraDataColumnIndex = it.getColumnIndex(SharingDataType.ColumnName.EXTRA_DATA)
        val itemKeyBase64ColumnIndex = it.getColumnIndex(SharingDataType.ColumnName.ITEM_KEY)
        asSequence()
            .map { cursor ->
                ItemContentDB(
                    id = cursor.getString(idColumnIndex) ?: "",
                    timestamp = cursor.getLong(timestampColumnIndex),
                    extraData = cursor.getString(extraDataColumnIndex) ?: "",
                    itemKeyBase64 = cursor.getString(itemKeyBase64ColumnIndex) ?: ""
                )
            }
            .toList()
    }

private fun Cursor.asSequence(): Sequence<Cursor> =
    generateSequence { this }.takeWhile { it.moveToNext() }.constrainOnce()
