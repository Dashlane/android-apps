package com.dashlane.autofill.api.changepassword

import android.content.Context
import com.dashlane.autofill.api.changepassword.domain.AutofillUpdateAccountService
import com.dashlane.util.Toaster



interface AutofillApiChangePasswordComponent {
    val autofillUpdateAccountService: AutofillUpdateAccountService
    val changePasswordLogger: AutofillChangePasswordLogger
    val toaster: Toaster

    companion object {
        operator fun invoke(context: Context) =
            (context.applicationContext as AutofillApiChangePasswordApplication).component
    }
}
