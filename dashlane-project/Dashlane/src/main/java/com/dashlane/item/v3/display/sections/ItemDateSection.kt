package com.dashlane.item.v3.display.sections

import android.content.Context
import android.text.format.DateUtils
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.data.SecureNoteFormData
import com.dashlane.item.v3.display.fields.GenericField
import com.dashlane.item.v3.display.fields.SectionContent
import com.dashlane.util.isSemanticallyNull
import java.time.Clock
import java.time.Duration
import java.time.Instant

@Composable
fun ItemDateSection(
    clock: Clock,
    data: FormData,
    editMode: Boolean
) {
    if (editMode) return
    SectionContent(editMode = false) {
        GenericField(
            label = stringResource(id = R.string.creation_date_header),
            data = data.created?.toRelativeDateTimeFormat(LocalContext.current, clock),
            editMode = false,
            onValueChanged = {}
        )
        if (data.updated != null) {
            GenericField(
                label = if (data.isShared) {
                    stringResource(id = R.string.latest_update_header_for_shared_item)
                } else {
                    stringResource(id = R.string.latest_update_header)
                },
                data = data.updated?.toRelativeDateTimeFormat(LocalContext.current, clock),
                editMode = false,
                onValueChanged = {}
            )
        }
    }
}

private fun Instant.toRelativeDateTimeFormat(context: Context, clock: Clock): String? = when {
    this.isSemanticallyNull() -> null
    this.isInNowRange(clock) -> context.getString(R.string.now_display_format)
    else -> {
        DateUtils.getRelativeTimeSpanString(
            toEpochMilli(),
            clock.millis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or
                DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_SHOW_TIME
        ).toString()
    }
}

private fun Instant.isInNowRange(clock: Clock) =
    Duration.between(this, Instant.now(clock)).abs() < Duration.ofMinutes(1)

@Preview
@Composable
private fun ItemDateSectionPreview() {
    val formData = SecureNoteFormData(
        created = Instant.ofEpochSecond(200_000),
        updated = Instant.now()
    )
    val clock = Clock.systemUTC()
    DashlanePreview {
        Column {
            ItemDateSection(
                clock = clock,
                data = formData,
                editMode = false
            )
            ItemDateSection(
                clock = clock,
                data = formData.copy(isShared = true),
                editMode = false
            )
            ItemDateSection(
                clock = clock,
                data = formData.copy(updated = null, created = null),
                editMode = false
            )
        }
    }
}