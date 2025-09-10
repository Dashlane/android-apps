package com.dashlane.autofill.api.util

import android.view.autofill.AutofillValue

class AutofillValueFactoryAndroidImpl : AutofillValueFactory {
    override fun forText(value: CharSequence?): AutofillValue = AutofillValue.forText(value)

    override fun forToggle(value: Boolean): AutofillValue = AutofillValue.forToggle(value)

    override fun forList(value: Int): AutofillValue = AutofillValue.forList(value)

    override fun forDate(value: Long): AutofillValue = AutofillValue.forDate(value)
}