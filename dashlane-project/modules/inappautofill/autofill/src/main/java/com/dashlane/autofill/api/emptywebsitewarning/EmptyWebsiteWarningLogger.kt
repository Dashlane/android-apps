package com.dashlane.autofill.api.emptywebsitewarning



interface EmptyWebsiteWarningLogger {
    

    fun logDisplay(website: String)

    

    fun logCancel(website: String)

    

    fun logUpdateAccount(website: String)
}