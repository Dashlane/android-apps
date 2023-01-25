package com.dashlane.autofill.api.changepassword.domain

import com.dashlane.autofill.api.R



enum class AutofillChangePasswordErrors(val resId: Int) {
    NO_MATCHING_CREDENTIAL(R.string.autofill_change_password_no_credential_error),
    INCOMPLETE(R.string.autofill_change_password_incomplete_error),
    USER_LOGGED_OUT(R.string.autofill_change_password_logged_out_error),
    DATABASE_ERROR(R.string.autofill_change_password_database_error)
}