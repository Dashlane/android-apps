package com.dashlane.autofill.createaccount.domain

enum class AutofillCreateAccountErrors(val message: String) {
    INCOMPLETE("At least one field is empty"),
    USER_LOGGED_OUT("User not logged in"),
    DATABASE_ERROR("Could not save the credential")
}