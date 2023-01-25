package com.dashlane.item.subview.view

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.dashlane.R
import com.dashlane.util.getThemeAttrDrawable
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.ZonedDateTime



object DatePickerInputProvider {

    const val DATE_PICKER_DIALOG_TAG = "date_picker_dialog"

    fun create(activity: AppCompatActivity, header: String, value: String?): TextInputLayout {
        return TextInputLayoutProvider.create(activity, header, value, false)
    }

    fun setClickListener(
        activity: AppCompatActivity,
        textInputLayout: TextInputLayout,
        originalDate: LocalDate?,
        dateSetAction: (LocalDate) -> Unit
    ) {
        val clickListener = View.OnClickListener {
            val date = originalDate ?: LocalDate.now()
            MaterialDatePicker.Builder.datePicker()
                .setLocalDateSelection(date)
                .build()
                .apply {
                    addLocalDateSelectionListener(dateSetAction)
                }
                .show(activity.supportFragmentManager, DATE_PICKER_DIALOG_TAG)
        }
        textInputLayout.apply {
            setOnClickListener(clickListener)
            editText?.apply {
                setOnClickListener(clickListener)
                
                background = context.getThemeAttrDrawable(R.attr.editTextBackground)
            }
        }
    }
}



private fun MaterialDatePicker.Builder<Long>.setLocalDateSelection(date: LocalDate): MaterialDatePicker.Builder<Long> =
    setSelection(ZonedDateTime.of(date, LocalTime.MIDNIGHT, ZoneOffset.UTC).toInstant().toEpochMilli())

private inline fun MaterialDatePicker<Long>.addLocalDateSelectionListener(crossinline block: (LocalDate) -> Unit) =
    addOnPositiveButtonClickListener {
        block(ZonedDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC).toLocalDate())
    }