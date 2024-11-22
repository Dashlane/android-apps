package com.dashlane.vault.textfactory.list

import android.content.res.Resources
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.R
import javax.inject.Inject

class DataIdentifierListTextResolverImpl @Inject constructor(
    private val resources: Resources,
    private val addressFactory: AddressListTextFactory,
    private val authentifiantFactory: AuthentifiantListTextFactory,
    private val bankStatementFactory: BankStatementListTextFactory,
    private val companyFactory: CompanyListTextFactory,
    private val driverLicenceFactory: DriverLicenceListTextFactory,
    private val emailFactory: EmailListTextFactory,
    private val fiscalStatementFactory: FiscalStatementListTextFactory,
    private val idCardFactory: IdCardListTextFactory,
    private val identityFactory: IdentityListTextFactory,
    private val passkeyFactory: PasskeyListTextFactory,
    private val passportFactory: PassportListTextFactory,
    private val paymentCreditCardFactory: PaymentCreditCardListTextFactory,
    private val personalWebsiteFactory: PersonalWebsiteListTextFactory,
    private val phoneFactory: PhoneListTextFactory,
    private val secretFactory: SecretListTextFactory,
    private val secureNoteFactory: SecureNoteListTextFactory,
    private val socialSecurityStatementFactory: SocialSecurityStatementListTextFactory,
    private val emptyFactory: EmptyListTextFactory,
) : DataIdentifierListTextResolver {

    override fun getLine1(item: SummaryObject): StatusText =
        when (item) {
            is SummaryObject.Address -> addressFactory.getTitle(item)
            is SummaryObject.Authentifiant -> authentifiantFactory.getTitle(item)
            is SummaryObject.BankStatement -> bankStatementFactory.getTitle(item)
            is SummaryObject.Company -> companyFactory.getTitle(item)
            is SummaryObject.DriverLicence -> driverLicenceFactory.getTitle(item)
            is SummaryObject.Email -> emailFactory.getTitle(item)
            is SummaryObject.FiscalStatement -> fiscalStatementFactory.getTitle(item)
            is SummaryObject.IdCard -> idCardFactory.getTitle(item)
            is SummaryObject.Identity -> identityFactory.getTitle(item)
            is SummaryObject.Passkey -> passkeyFactory.getTitle(item)
            is SummaryObject.Passport -> passportFactory.getTitle(item)
            is SummaryObject.PaymentCreditCard -> paymentCreditCardFactory.getTitle(item)
            is SummaryObject.PersonalWebsite -> personalWebsiteFactory.getTitle(item)
            is SummaryObject.Phone -> phoneFactory.getTitle(item)
            is SummaryObject.Secret -> secretFactory.getTitle(item)
            is SummaryObject.SecureNote -> secureNoteFactory.getTitle(item)
            is SummaryObject.SocialSecurityStatement -> socialSecurityStatementFactory.getTitle(item)
            is SummaryObject.PaymentPaypal,
            is SummaryObject.AuthCategory,
            is SummaryObject.Collection,
            is SummaryObject.DataChangeHistory,
            is SummaryObject.GeneratedPassword,
            is SummaryObject.SecureFileInfo,
            is SummaryObject.SecureNoteCategory,
            is SummaryObject.SecurityBreach -> emptyFactory.getTitle(item)
        }

    override fun getLine2(item: SummaryObject): StatusText {
        val default = StatusText(resources.getString(R.string.incomplete), true)

        return when (item) {
            is SummaryObject.Address -> addressFactory.getDescription(item, default)
            is SummaryObject.Authentifiant -> authentifiantFactory.getDescription(item, default)
            is SummaryObject.BankStatement -> bankStatementFactory.getDescription(item, default)
            is SummaryObject.Company -> companyFactory.getDescription(item, default)
            is SummaryObject.DriverLicence -> driverLicenceFactory.getDescription(item, default)
            is SummaryObject.Email -> emailFactory.getDescription(item, default)
            is SummaryObject.FiscalStatement -> fiscalStatementFactory.getDescription(item, default)
            is SummaryObject.IdCard -> idCardFactory.getDescription(item, default)
            is SummaryObject.Identity -> identityFactory.getDescription(item, default)
            is SummaryObject.Passkey -> passkeyFactory.getDescription(item, default)
            is SummaryObject.Passport -> passportFactory.getDescription(item, default)
            is SummaryObject.PaymentCreditCard -> paymentCreditCardFactory.getDescription(item, default)
            is SummaryObject.PersonalWebsite -> personalWebsiteFactory.getDescription(item, default)
            is SummaryObject.Phone -> phoneFactory.getDescription(item, default)
            is SummaryObject.Secret -> secretFactory.getDescription(item, default)
            is SummaryObject.SecureNote -> secureNoteFactory.getDescription(item, default)
            is SummaryObject.SocialSecurityStatement -> socialSecurityStatementFactory.getDescription(item, default)
            is SummaryObject.PaymentPaypal,
            is SummaryObject.AuthCategory,
            is SummaryObject.Collection,
            is SummaryObject.DataChangeHistory,
            is SummaryObject.GeneratedPassword,
            is SummaryObject.SecureFileInfo,
            is SummaryObject.SecureNoteCategory,
            is SummaryObject.SecurityBreach -> emptyFactory.getDescription(item, default)
        }
    }
}