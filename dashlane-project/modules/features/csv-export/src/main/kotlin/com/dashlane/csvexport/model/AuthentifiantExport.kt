package com.dashlane.csvexport.model

import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.objectType

internal typealias ExportData = Map<String, String?>

@Suppress("UNCHECKED_CAST")
fun VaultItem<*>.buildExportVaultItem(): ExportData {
    return when (syncObject) {
        is SyncObject.Authentifiant -> (this as VaultItem<SyncObject.Authentifiant>).buildExportAuthentifiant()
        is SyncObject.Address -> (this as VaultItem<SyncObject.Address>).buildExportAddress()
        is SyncObject.BankStatement -> (this as VaultItem<SyncObject.BankStatement>).buildExportBankStatement()
        is SyncObject.Company -> (this as VaultItem<SyncObject.Company>).buildExportCompany()
        is SyncObject.DriverLicence -> (this as VaultItem<SyncObject.DriverLicence>).buildExportDriverLicence()
        is SyncObject.Email -> (this as VaultItem<SyncObject.Email>).buildExportEmail()
        is SyncObject.FiscalStatement -> (this as VaultItem<SyncObject.FiscalStatement>).buildExportFiscalStatement()
        is SyncObject.IdCard -> (this as VaultItem<SyncObject.IdCard>).buildExportIdCard()
        is SyncObject.Identity -> (this as VaultItem<SyncObject.Identity>).buildExportIdentity()
        is SyncObject.Passport -> (this as VaultItem<SyncObject.Passport>).buildExportPassport()
        is SyncObject.PaymentCreditCard -> (this as VaultItem<SyncObject.PaymentCreditCard>).buildExportPaymentCreditCard()
        is SyncObject.PersonalWebsite -> (this as VaultItem<SyncObject.PersonalWebsite>).buildExportPersonalWebsite()
        is SyncObject.Phone -> (this as VaultItem<SyncObject.Phone>).buildExportPhone()
        is SyncObject.SecureNote -> (this as VaultItem<SyncObject.SecureNote>).buildExportSecureNote()
        is SyncObject.SocialSecurityStatement ->
            (this as VaultItem<SyncObject.SocialSecurityStatement>).buildExportSocialSecurityStatement()
        else -> throw IllegalStateException("Unsupported type to export CSV ${this.syncObject.objectType}")
    }
}

fun VaultItem<SyncObject.Authentifiant>.buildExportAuthentifiant(): ExportData =
    buildMap {
        val syncObject = this@buildExportAuthentifiant.syncObject
        AuthentifiantExport.entries.forEach {
            when (it) {
                AuthentifiantExport.USER_NAME -> put(it.value, syncObject.email)
                AuthentifiantExport.USER_NAME_2 -> put(it.value, syncObject.login)
                AuthentifiantExport.USER_NAME_3 -> put(it.value, syncObject.secondaryLogin)
                AuthentifiantExport.TITLE -> put(it.value, syncObject.title)
                AuthentifiantExport.PASSWORD -> put(it.value, syncObject.password?.toString())
                AuthentifiantExport.NOTE -> put(it.value, syncObject.note)
                AuthentifiantExport.URL -> put(
                    it.value,
                    syncObject.userSelectedUrl ?: syncObject.url,
                )
                AuthentifiantExport.CATEGORY -> put(it.value, null)
                AuthentifiantExport.OTP -> put(it.value, syncObject.otpUrl.toString())
            }
        }
    }

fun VaultItem<SyncObject.SecureNote>.buildExportSecureNote(): ExportData =
    buildMap {
        val syncObject = this@buildExportSecureNote.syncObject
        SecureNoteExport.entries.forEach {
            when (it) {
                SecureNoteExport.TITLE -> put(it.value, syncObject.title)
                SecureNoteExport.NOTE -> put(it.value, syncObject.content)
                SecureNoteExport.CATEGORY -> put(it.value, null)
            }
        }
    }

fun VaultItem<SyncObject.PaymentCreditCard>.buildExportPaymentCreditCard(): ExportData =
    buildMap {
        val syncObject = this@buildExportPaymentCreditCard.syncObject
        put(TYPE, PaymentType.PAYMENT_CARD.value)
        PaymentExport.entries.forEach {
            when (it) {
                PaymentExport.ACCOUNT_NAME -> put(it.value, syncObject.ownerName)
                PaymentExport.CC_NUMBER -> put(it.value, syncObject.cardNumber.toString())
                PaymentExport.CODE -> put(it.value, syncObject.securityCode.toString())
                PaymentExport.EXPIRATION_MONTH -> put(it.value, syncObject.expireMonth.toString())
                PaymentExport.EXPIRATION_YEAR -> put(it.value, syncObject.expireYear.toString())
                PaymentExport.COUNTRY -> put(it.value, null)
                PaymentExport.ISSUING_BANK -> put(it.value, syncObject.bank)
                PaymentExport.NOTE -> put(it.value, syncObject.cCNote.toString())
                PaymentExport.ITEM_NAME -> put(it.value, syncObject.name)
                else -> put(it.value, null)
            }
        }
    }

fun VaultItem<SyncObject.BankStatement>.buildExportBankStatement(): ExportData =
    buildMap {
        val syncObject = this@buildExportBankStatement.syncObject
        put(TYPE, PaymentType.BANK.value)
        PaymentExport.entries.forEach {
            when (it) {
                PaymentExport.ACCOUNT_NAME -> put(it.value, syncObject.bankAccountName)
                PaymentExport.ACCOUNT_HOLDER -> put(it.value, syncObject.bankAccountOwner)
                PaymentExport.ROUTING_NUMBER -> put(it.value, syncObject.bankAccountBIC.toString())
                PaymentExport.ACCOUNT_NUMBER -> put(it.value, syncObject.bankAccountIBAN.toString())
                PaymentExport.COUNTRY -> put(it.value, null)
                PaymentExport.ISSUING_BANK -> put(it.value, syncObject.bankAccountBank)
                else -> put(it.value, null)
            }
        }
    }

fun VaultItem<SyncObject.IdCard>.buildExportIdCard(): ExportData =
    buildMap {
        val syncObject = this@buildExportIdCard.syncObject
        put(TYPE, IdType.CARD.value)
        IdExport.entries.forEach {
            when (it) {
                IdExport.NUMBER -> put(it.value, syncObject.number)
                IdExport.ISSUE_DATE -> put(it.value, syncObject.deliveryDate.toString())
                IdExport.EXPIRATION_DATE -> put(it.value, syncObject.expireDate.toString())
                IdExport.ITEM_NAME -> put(it.value, syncObject.fullname)
                else -> put(it.value, null)
            }
        }
    }

fun VaultItem<SyncObject.Passport>.buildExportPassport(): ExportData =
    buildMap {
        val syncObject = this@buildExportPassport.syncObject
        put(TYPE, IdType.PASSPORT.value)
        IdExport.entries.forEach {
            when (it) {
                IdExport.NUMBER -> put(it.value, syncObject.number)
                IdExport.ISSUE_DATE -> put(it.value, syncObject.deliveryDate.toString())
                IdExport.EXPIRATION_DATE -> put(it.value, syncObject.expireDate.toString())
                IdExport.ITEM_NAME -> put(it.value, syncObject.fullname)
                IdExport.PLACE_OF_ISSUE -> put(it.value, syncObject.deliveryPlace)
                else -> put(it.value, null)
            }
        }
    }

fun VaultItem<SyncObject.DriverLicence>.buildExportDriverLicence(): ExportData =
    buildMap {
        val syncObject = this@buildExportDriverLicence.syncObject
        put(TYPE, IdType.LICENSE.value)
        IdExport.entries.forEach {
            when (it) {
                IdExport.NUMBER -> put(it.value, syncObject.number)
                IdExport.ISSUE_DATE -> put(it.value, syncObject.deliveryDate.toString())
                IdExport.EXPIRATION_DATE -> put(it.value, syncObject.expireDate.toString())
                IdExport.ITEM_NAME -> put(it.value, syncObject.fullname)
                else -> put(it.value, null)
            }
        }
    }

fun VaultItem<SyncObject.SocialSecurityStatement>.buildExportSocialSecurityStatement(): ExportData =
    buildMap {
        val syncObject = this@buildExportSocialSecurityStatement.syncObject
        put(TYPE, IdType.SOCIAL_SECURITY.value)
        IdExport.entries.forEach {
            when (it) {
                IdExport.NUMBER -> put(it.value, syncObject.socialSecurityNumber.toString())
                IdExport.ITEM_NAME -> put(it.value, syncObject.socialSecurityFullname)
                else -> put(it.value, null)
            }
        }
    }

fun VaultItem<SyncObject.FiscalStatement>.buildExportFiscalStatement(): ExportData =
    buildMap {
        val syncObject = this@buildExportFiscalStatement.syncObject
        put(TYPE, IdType.TAX_NUMBER.value)
        IdExport.entries.forEach {
            when (it) {
                IdExport.NUMBER -> put(it.value, syncObject.fiscalNumber)
                else -> put(it.value, null)
            }
        }
    }

fun VaultItem<SyncObject.Identity>.buildExportIdentity(): ExportData =
    buildMap {
        val syncObject = this@buildExportIdentity.syncObject
        put(TYPE, ContactType.CONTACT_NAME.value)
        PersonalInfoExport.entries.forEach {
            when (it) {
                PersonalInfoExport.TITLE -> put(it.value, syncObject.title?.value)
                PersonalInfoExport.FIRST_NAME -> put(it.value, syncObject.firstName)
                PersonalInfoExport.MIDDLE_NAME -> put(it.value, syncObject.middleName)
                PersonalInfoExport.LASTNAME -> put(it.value, syncObject.lastName)
                PersonalInfoExport.LOGIN -> put(it.value, syncObject.pseudo)
                PersonalInfoExport.DATE_OF_BIRTH -> put(it.value, syncObject.birthDate.toString())
                PersonalInfoExport.PLACE_OF_BIRTH -> put(it.value, syncObject.birthPlace)
                else -> put(it.value, null)
            }
        }
    }

fun VaultItem<SyncObject.Email>.buildExportEmail(): ExportData =
    buildMap {
        val syncObject = this@buildExportEmail.syncObject
        put(TYPE, ContactType.EMAIL.value)
        PersonalInfoExport.entries.forEach {
            when (it) {
                PersonalInfoExport.EMAIL -> put(it.value, syncObject.email)
                PersonalInfoExport.ITEM_NAME -> put(it.value, syncObject.emailName)
                PersonalInfoExport.EMAIL_TYPE -> put(it.value, syncObject.type?.value)
                else -> put(it.value, null)
            }
        }
    }

fun VaultItem<SyncObject.Phone>.buildExportPhone(): ExportData =
    buildMap {
        val syncObject = this@buildExportPhone.syncObject
        put(TYPE, ContactType.NUMBER.value)
        PersonalInfoExport.entries.forEach {
            when (it) {
                PersonalInfoExport.PHONE_NUMBER -> put(it.value, syncObject.number)
                PersonalInfoExport.ITEM_NAME -> put(it.value, syncObject.phoneName)
                else -> put(it.value, null)
            }
        }
    }

fun VaultItem<SyncObject.Address>.buildExportAddress(): ExportData =
    buildMap {
        val syncObject = this@buildExportAddress.syncObject
        put(TYPE, ContactType.ADDRESS.value)
        PersonalInfoExport.entries.forEach {
            when (it) {
                PersonalInfoExport.ITEM_NAME -> put(it.value, syncObject.addressName)
                PersonalInfoExport.ADDRESS -> put(it.value, syncObject.addressFull)
                PersonalInfoExport.COUNTRY -> put(it.value, syncObject.country?.isoCode)
                PersonalInfoExport.STATE -> put(it.value, syncObject.state)
                PersonalInfoExport.CITY -> put(it.value, syncObject.city)
                PersonalInfoExport.ZIP -> put(it.value, syncObject.zipCode)
                PersonalInfoExport.ADDRESS_RECIPIENT -> put(it.value, syncObject.receiver)
                PersonalInfoExport.ADDRESS_BUILDING -> put(it.value, syncObject.building)
                PersonalInfoExport.ADDRESS_APARTMENT -> put(it.value, syncObject.door)
                PersonalInfoExport.ADDRESS_FLOOR -> put(it.value, syncObject.floor)
                PersonalInfoExport.ADDRESS_DOOR_CODE -> put(it.value, syncObject.digitCode)
                else -> put(it.value, null)
            }
        }
    }

fun VaultItem<SyncObject.Company>.buildExportCompany(): ExportData =
    buildMap {
        val syncObject = this@buildExportCompany.syncObject
        put(TYPE, ContactType.COMPANY.value)
        PersonalInfoExport.entries.forEach {
            when (it) {
                PersonalInfoExport.ITEM_NAME -> put(it.value, syncObject.name)
                PersonalInfoExport.JOB_TITLE -> put(it.value, syncObject.jobTitle)
                else -> put(it.value, null)
            }
        }
    }

fun VaultItem<SyncObject.PersonalWebsite>.buildExportPersonalWebsite(): ExportData =
    buildMap {
        val syncObject = this@buildExportPersonalWebsite.syncObject
        put(TYPE, ContactType.WEBSITE.value)
        PersonalInfoExport.entries.forEach {
            when (it) {
                PersonalInfoExport.ITEM_NAME -> put(it.value, syncObject.name)
                PersonalInfoExport.URL -> put(it.value, syncObject.website)
                else -> put(it.value, null)
            }
        }
    }