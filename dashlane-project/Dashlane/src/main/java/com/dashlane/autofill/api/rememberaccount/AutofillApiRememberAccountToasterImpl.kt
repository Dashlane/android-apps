package com.dashlane.autofill.api.rememberaccount

import android.widget.Toast
import com.dashlane.R
import com.dashlane.autofill.rememberaccount.AutofillApiRememberedAccountToaster
import com.dashlane.util.Toaster
import javax.inject.Inject

class AutofillApiRememberAccountToasterImpl @Inject constructor(
    val toaster: Toaster
) : AutofillApiRememberedAccountToaster {

    override fun onAccountRemembered() {
        toaster.show(R.string.autofill_rememberaccount_message, Toast.LENGTH_SHORT)
    }
}
