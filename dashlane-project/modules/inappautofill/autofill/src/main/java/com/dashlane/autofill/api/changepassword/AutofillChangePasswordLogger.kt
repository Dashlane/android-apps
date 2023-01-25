package com.dashlane.autofill.api.changepassword

interface AutofillChangePasswordLogger {
    var packageName: String?
    var domain: String?

    

    fun logUpdate(id: String)

    fun logCancel(id: String)

    

    fun logOnClickUpdateAccount(id: String)
}