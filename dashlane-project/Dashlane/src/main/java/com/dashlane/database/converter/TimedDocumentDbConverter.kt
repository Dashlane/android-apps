package com.dashlane.database.converter

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.converter.TimedDocumentDbConverter.getLocalDate
import com.dashlane.database.sql.TimedDocumentSql
import com.dashlane.util.time.toSeconds
import com.dashlane.util.getString
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

object TimedDocumentDbConverter {

    internal fun getLocalDate(c: Cursor, columnName: String): LocalDate? {
        return c.getString(columnName)
            ?.toLongOrNull()
            ?.takeIf { it != -1L } 
            ?.let { LocalDateTime.ofInstant(Instant.ofEpochSecond(it), ZoneOffset.UTC).toLocalDate() }
    }

    internal fun putLocalDate(cv: ContentValues, columnName: String, value: LocalDate?) {
        cv.put(columnName, value?.toSeconds()?.toString())
    }

    internal fun putDeliveryDate(cv: ContentValues, value: LocalDate?) {
        putLocalDate(cv, TimedDocumentSql.FIELD_DELIVERY_DATE, value)
    }

    internal fun putExpireDate(cv: ContentValues, value: LocalDate?) {
        putLocalDate(cv, TimedDocumentSql.FIELD_EXPIRY_DATE, value)
    }
}

internal fun Cursor.getDeliveryDateField() =
    getLocalDate(this, TimedDocumentSql.FIELD_DELIVERY_DATE)

internal fun Cursor.getExpireDateField() =
    getLocalDate(this, TimedDocumentSql.FIELD_EXPIRY_DATE)
