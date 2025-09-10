@file:OptIn(ExperimentalMaterial3Api::class)

package com.dashlane.item.v3.display.fields

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.design.component.DateField
import com.dashlane.design.component.DisplayField
import com.dashlane.design.theme.tooling.DashlanePreview
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ItemEditDateField(
    modifier: Modifier = Modifier,
    label: String,
    year: Int?,
    month: Int?,
    editMode: Boolean,
    isEditable: Boolean,
    onDateChange: (Long?) -> Unit,
) {
    val dateTimeFormatter = remember { DateTimeFormatter.ofPattern("MM/yyyy") }
    val date: ZonedDateTime? = remember(year, month) {
        if (year == null || month == null) return@remember null
        
        val year = if (year < 100) 2000 + year else year
        YearMonth.of(year, month)
            .atEndOfMonth()
            .atStartOfDay(ZoneOffset.UTC)
    }
    if (editMode) {
        val dateMillis: Long? = remember(date) { date?.toEpochSecond()?.times(1000) }
        ItemEditDateField(
            modifier = modifier.fillMaxWidth(),
            label = label,
            initialSelectedDateMillis = dateMillis,
            formatter = dateTimeFormatter,
            isEditable = isEditable,
            onDateChange = onDateChange,
        )
    } else {
        val value: String? = remember(date) { date?.let { dateTimeFormatter.format(it) } }
        DisplayField(
            modifier = modifier.fillMaxWidth(),
            label = label,
            value = value,
        )
    }
}

@Composable
private fun ItemEditDateField(
    modifier: Modifier = Modifier,
    label: String,
    initialSelectedDateMillis: Long?,
    formatter: DateTimeFormatter,
    isEditable: Boolean,
    onDateChange: (Long?) -> Unit,
    selectableDates: SelectableDates = DatePickerDefaults.AllDates,
) {
    DateField(
        modifier = modifier,
        label = label,
        initialSelectedDateMillis = initialSelectedDateMillis,
        selectableDates = selectableDates,
        formatter = formatter,
        readOnly = !isEditable,
        onDateChanged = onDateChange,
    )
}

@Preview
@Composable
private fun ItemEditDateFieldViewModePreview() = DashlanePreview {
    ItemEditDateField(
        label = "Dropdown Field",
        year = 2024,
        month = 11,
        editMode = false,
        isEditable = true,
        onDateChange = {},
    )
}

@Preview
@Composable
private fun ItemEditDateFieldEditModePreview() = DashlanePreview {
    ItemEditDateField(
        label = "Dropdown Field",
        year = 2024,
        month = 11,
        editMode = true,
        isEditable = true,
        onDateChange = {},
    )
}