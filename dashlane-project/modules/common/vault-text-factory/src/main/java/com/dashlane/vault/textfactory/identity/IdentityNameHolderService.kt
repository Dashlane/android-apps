package com.dashlane.vault.textfactory.identity

import com.dashlane.vault.summary.SummaryObject

interface IdentityNameHolderService {
    fun getOwner(item: SummaryObject.DriverLicence): String
    fun getOwner(item: SummaryObject.IdCard): String
    fun getOwner(item: SummaryObject.Passport): String
    fun getOwner(item: SummaryObject.SocialSecurityStatement): String
    fun getOwner(item: SummaryObject.FiscalStatement): String
}