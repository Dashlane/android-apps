package com.dashlane.autofill.api.util

import android.view.autofill.AutofillValue

interface AutofillValueFactory {
    fun forText(value: CharSequence?): AutofillValue

    fun forToggle(value: Boolean): AutofillValue

    fun forList(value: Int): AutofillValue

    fun forDate(value: Long): AutofillValue
}