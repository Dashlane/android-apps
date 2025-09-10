package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class FieldsFilled(
    val website: Int? = null,
    val wifi: Int? = null,
    val address: Int? = null,
    val fiscalStatement: Int? = null,
    val passkey: Int? = null,
    val securityBreach: Int? = null,
    val idCard: Int? = null,
    val otp: Int? = null,
    val socialSecurity: Int? = null,
    val creditCard: Int? = null,
    val credential: Int? = null,
    val passport: Int? = null,
    val phone: Int? = null,
    val identity: Int? = null,
    val company: Int? = null,
    val bankStatement: Int? = null,
    val driverLicense: Int? = null,
    val generatedPassword: Int? = null,
    val paypal: Int? = null,
    val email: Int? = null,
    val secureNote: Int? = null,
) : TrackingLog.Object {
    override fun collect(collector: TrackingLog.Collector) {
        collector["website"] = website
        collector["wifi"] = wifi
        collector["address"] = address
        collector["fiscal_statement"] = fiscalStatement
        collector["passkey"] = passkey
        collector["security_breach"] = securityBreach
        collector["id_card"] = idCard
        collector["otp"] = otp
        collector["social_security"] = socialSecurity
        collector["credit_card"] = creditCard
        collector["credential"] = credential
        collector["passport"] = passport
        collector["phone"] = phone
        collector["identity"] = identity
        collector["company"] = company
        collector["bank_statement"] = bankStatement
        collector["driver_license"] = driverLicense
        collector["generated_password"] = generatedPassword
        collector["paypal"] = paypal
        collector["email"] = email
        collector["secure_note"] = secureNote
    }
}