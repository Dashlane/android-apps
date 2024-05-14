package com.dashlane.util.clipboard.vault

import com.dashlane.util.clipboard.R

@SuppressWarnings("kotlin:S1479")
fun CopyField.getFeedback() = when (this) {
    CopyField.Password -> R.string.feedback_copy_password
    CopyField.Login, CopyField.PasskeyDisplayName -> R.string.feedback_copy_login
    CopyField.Email -> R.string.feedback_copy_email
    CopyField.SecondaryLogin -> R.string.feedback_copy_secondary_login
    CopyField.PaymentsNumber -> R.string.feedback_copy_payments_number
    CopyField.PaymentsSecurityCode -> R.string.feedback_copy_payments_security_code
    CopyField.PaymentsExpirationDate -> R.string.feedback_copy_payments_expiration_date
    CopyField.PayPalLogin -> R.string.feedback_copy_paypal_login
    CopyField.PayPalPassword -> R.string.feedback_copy_paypal_password
    CopyField.BankAccountBank -> R.string.feedback_copy_bankaccount_bank
    CopyField.BankAccountBicSwift -> R.string.feedback_copy_bankaccount_bic_swift
    CopyField.BankAccountRoutingNumber -> R.string.feedback_copy_bankaccount_routing_number
    CopyField.BankAccountSortCode -> R.string.feedback_copy_bankaccount_sort_code
    CopyField.BankAccountIban -> R.string.feedback_copy_bankaccount_iban
    CopyField.BankAccountAccountNumber -> R.string.feedback_copy_bankaccount_account_number
    CopyField.BankAccountClabe -> R.string.feedback_copy_bankaccount_clabe
    CopyField.Address -> R.string.feedback_copy_address
    CopyField.City -> R.string.feedback_copy_city
    CopyField.ZipCode -> R.string.feedback_copy_zip_code
    CopyField.FullName -> R.string.feedback_copy_name_holder
    CopyField.IdsLinkedIdentity -> R.string.feedback_copy_name_holder
    CopyField.IdsNumber -> R.string.feedback_copy_ids_number
    CopyField.IdsIssueDate -> R.string.feedback_copy_ids_issue_date
    CopyField.IdsExpirationDate -> R.string.feedback_copy_ids_expiration_date
    CopyField.PassportNumber -> R.string.feedback_copy_passport_number
    CopyField.PassportLinkedIdentity -> R.string.feedback_copy_name_holder
    CopyField.PassportIssueDate -> R.string.feedback_copy_passport_issue_date
    CopyField.PassportExpirationDate -> R.string.feedback_copy_passport_expiration_date
    CopyField.DriverLicenseLinkedIdentity -> R.string.feedback_copy_name_holder
    CopyField.DriverLicenseNumber -> R.string.feedback_copy_driverlicense_number
    CopyField.DriverLicenseIssueDate -> R.string.feedback_copy_driverlicense_issue_date
    CopyField.DriverLicenseExpirationDate -> R.string.feedback_copy_driverlicense_expiration_date
    CopyField.SocialSecurityLinkedIdentity -> R.string.feedback_copy_name_holder
    CopyField.SocialSecurityNumber -> R.string.feedback_copy_social_security_number
    CopyField.TaxNumber -> R.string.feedback_copy_fiscal_number
    CopyField.JustEmail -> R.string.feedback_copy_just_email
    CopyField.PhoneNumber -> R.string.feedback_copy_phone_number
    CopyField.PersonalWebsite -> R.string.feedback_copy_personal_website
    CopyField.OtpCode -> R.string.feedback_copy_otp_code
    CopyField.FirstName -> R.string.feedback_copy_first_name
    CopyField.LastName -> R.string.feedback_copy_last_name
    CopyField.MiddleName -> R.string.feedback_copy_middle_name
    CopyField.IdentityLogin -> R.string.feedback_copy_login
    CopyField.CompanyName -> R.string.feedback_copy_company_name
    CopyField.CompanyTitle -> R.string.feedback_copy_company_title
    CopyField.TaxOnlineNumber -> R.string.feedback_copy_tax_online_number
}
