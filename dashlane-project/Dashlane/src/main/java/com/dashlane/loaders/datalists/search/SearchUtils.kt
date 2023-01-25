package com.dashlane.loaders.datalists.search

import com.dashlane.search.Query
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType



object SearchUtils {
    val FILTER_BY_DATA_TYPE = mapOf<SyncObjectType, (SummaryObject, Query) -> (Boolean)>(
        SyncObjectType.AUTHENTIFIANT to { item, query ->
            item is SummaryObject.Authentifiant &&
                    (query.match(item.title) ||
                            query.match(item.url) ||
                            query.match(item.userSelectedUrl) ||
                            query.match(item.login) ||
                            query.match(item.email))
        },
        SyncObjectType.SECURE_NOTE to { item, query ->
            item is SummaryObject.SecureNote &&
                    (query.match(item.title) ||
                            
                            ((item.secured != true) && query.match(item.content)))
        },
        SyncObjectType.COMPANY to { item, query ->
            item is SummaryObject.Company &&
                    (query.match(item.name) ||
                            query.match(item.sirenNumber) ||
                            query.match(item.siretNumber))
        },
        SyncObjectType.PERSONAL_WEBSITE to { item, query ->
            item is SummaryObject.PersonalWebsite && (query.match(item.name) || query.match(item.website))
        },
        SyncObjectType.BANK_STATEMENT to { item, query ->
            item is SummaryObject.BankStatement &&
                    (query.match(item.bankAccountName) ||
                            query.match(item.bankAccountOwner) ||
                            query.match(item.bankAccountBank))
        },
        SyncObjectType.PAYMENT_CREDIT_CARD to { item, query ->
            item is SummaryObject.PaymentCreditCard &&
                    (query.match(item.name) ||
                            query.match(item.ownerName) ||
                            query.match(item.cardNumberLastFourDigits) ||
                            query.match(item.bank))
        },
        SyncObjectType.EMAIL to { item, query ->
            item is SummaryObject.Email &&
                    (query.match(item.emailName) ||
                            query.match(item.email))
        },
        SyncObjectType.PHONE to { item, query ->
            item is SummaryObject.Phone &&
                    (query.match(item.phoneName) ||
                            query.match(item.number))
        },
        SyncObjectType.PAYMENT_PAYPAL to { item, query ->
            item is SummaryObject.PaymentPaypal && (query.match(item.name) || query.match(item.login))
        },
        SyncObjectType.PASSPORT to { item, query ->
            item is SummaryObject.Passport && (query.match(item.fullname) || query.match(item.number))
        },
        SyncObjectType.DRIVER_LICENCE to { item, query ->
            item is SummaryObject.DriverLicence &&
                    (query.match(item.fullname) ||
                            query.match(item.number) ||
                            query.match(item.state))
        },
        SyncObjectType.ID_CARD to { item, query ->
            item is SummaryObject.IdCard &&
                    (query.match(item.fullname) ||
                            query.match(item.number))
        },
        SyncObjectType.FISCAL_STATEMENT to { item, query ->
            item is SummaryObject.FiscalStatement && (query.match(item.fiscalNumber))
        },
        SyncObjectType.ADDRESS to { item, query ->
            item is SummaryObject.Address &&
                    (query.match(item.addressFull) ||
                            query.match(item.addressName) ||
                            query.match(item.city) ||
                            query.match(item.country?.isoCode) ||
                            query.match(item.state) ||
                            query.match(item.zipCode))
        },
        SyncObjectType.IDENTITY to { item, query ->
            item is SummaryObject.Identity &&
                    (query.match(item.firstName) ||
                            query.match(item.lastName) ||
                            query.match(item.middleName) ||
                            query.match(item.pseudo))
        },
        SyncObjectType.SOCIAL_SECURITY_STATEMENT to { item, query ->
            item is SummaryObject.SocialSecurityStatement &&
                    (query.match(item.socialSecurityFullname))
        }
    )

    

    fun order(syncObjectType: SyncObjectType?) = when (syncObjectType) {
        SyncObjectType.AUTHENTIFIANT -> -2 
        SyncObjectType.SECURE_NOTE -> -1 
        SyncObjectType.ADDRESS -> 0
        SyncObjectType.AUTH_CATEGORY -> 1
        SyncObjectType.COMPANY -> 3
        SyncObjectType.DRIVER_LICENCE -> 4
        SyncObjectType.EMAIL -> 5
        SyncObjectType.FISCAL_STATEMENT -> 6
        SyncObjectType.GENERATED_PASSWORD -> 7
        SyncObjectType.ID_CARD -> 8
        SyncObjectType.IDENTITY -> 9
        SyncObjectType.PASSPORT -> 10
        SyncObjectType.PAYMENT_PAYPAL -> 11
        SyncObjectType.PAYMENT_CREDIT_CARD -> 12
        SyncObjectType.PERSONAL_WEBSITE -> 13
        SyncObjectType.PHONE -> 14
        SyncObjectType.SOCIAL_SECURITY_STATEMENT -> 15
        SyncObjectType.SECURE_NOTE_CATEGORY -> 17
        SyncObjectType.BANK_STATEMENT -> 18
        SyncObjectType.DATA_CHANGE_HISTORY -> 19
        SyncObjectType.SECURE_FILE_INFO -> 20
        SyncObjectType.SECURITY_BREACH -> 21
        SyncObjectType.SETTINGS -> 22
        else -> Int.MAX_VALUE
    }
}