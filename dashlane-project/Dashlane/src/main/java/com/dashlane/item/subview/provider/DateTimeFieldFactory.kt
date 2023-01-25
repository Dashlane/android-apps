package com.dashlane.item.subview.provider

import android.content.Context
import com.dashlane.R
import com.dashlane.item.subview.readonly.ItemMetaReadValueDateTimeSubView
import com.dashlane.util.date.RelativeDateFormatter
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.model.VaultItem
import java.time.Clock
import java.time.Duration
import java.time.Instant



class DateTimeFieldFactory(
    private val clock: Clock,
    private val relativeDateFormatter: RelativeDateFormatter
) {
    

    fun createCreationDateField(
        editMode: Boolean,
        context: Context,
        item: VaultItem<*>
    ): ItemMetaReadValueDateTimeSubView? {
        if (editMode) {
            return null
        }
        val creationDateInstant = item.syncObject.creationDatetime
        val formattedString = creationDateInstant?.toRelativeDateTimeFormat(context)
        formattedString?.let {
            return ItemMetaReadValueDateTimeSubView(
                header = context.getString(R.string.creation_date_header),
                value = creationDateInstant,
                formattedDate = formattedString
            )
        }
        return null
    }

    

    fun createLatestUpdateDateField(
        editMode: Boolean,
        shared: Boolean = false,
        context: Context,
        item: VaultItem<*>
    ): ItemMetaReadValueDateTimeSubView? {
        if (editMode) {
            return null
        }
        val modificationDatetimeInstant = item.syncObject.userModificationDatetime
        val formattedString = modificationDatetimeInstant?.toRelativeDateTimeFormat(context)
        formattedString?.let {
            return ItemMetaReadValueDateTimeSubView(
                header = context.getString(R.string.latest_update_header).takeUnless { shared }
                    ?: context.getString(R.string.latest_update_header_for_shared_item),
                value = modificationDatetimeInstant,
                formattedDate = formattedString
            )
        }
        return null
    }

    

    private fun Instant.toRelativeDateTimeFormat(context: Context): String? =
        when {
            this.isSemanticallyNull() -> null
            this.isInNowRange(clock) -> context.getString(R.string.now_display_format)
            else -> relativeDateFormatter.format(instant = this)
        }

    

    private fun Instant.isInNowRange(clock: Clock) =
        Duration.between(this, Instant.now(clock)).abs() < NOW_RANGE_THRESHOLD_DURATION

    companion object {
        private val NOW_RANGE_THRESHOLD_DURATION = Duration.ofMinutes(1)
    }
}