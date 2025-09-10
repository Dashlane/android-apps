package com.dashlane.xml.domain

import com.dashlane.xml.domain.properties.AuthentifiantXml
import com.dashlane.xml.domain.properties.BankStatementXml
import com.dashlane.xml.domain.properties.GeneratedPasswordXml
import com.dashlane.xml.domain.properties.PasskeyXml
import com.dashlane.xml.domain.properties.PaymentCreditCardXml
import com.dashlane.xml.domain.properties.SecurityBreachXml
import com.dashlane.xml.domain.properties.SocialSecurityStatementXml
import com.dashlane.xml.domain.properties.WiFiXml
import javax.annotation.processing.Generated

@Generated
object SyncSensitiveFields {
    val authentifiant = setOf(
        AuthentifiantXml.OTP_URL,
        AuthentifiantXml.OTP_SECRET,
        AuthentifiantXml.PASSWORD,
    )
    val bankStatement = setOf(
        BankStatementXml.BANK_ACCOUNT_B_I_C,
        BankStatementXml.BANK_ACCOUNT_I_B_A_N,
    )
    val generatedPassword = setOf(
        GeneratedPasswordXml.PASSWORD,
    )
    val passkey = setOf(
        PasskeyXml.PRIVATE_KEY,
        PasskeyXml.CLOUD_PASSKEY,
    )
    val paymentCreditCard = setOf(
        PaymentCreditCardXml.CARD_NUMBER,
        PaymentCreditCardXml.PIN,
        PaymentCreditCardXml.C_C_NOTE,
        PaymentCreditCardXml.SECURITY_CODE,
    )
    val securityBreach = setOf(
        SecurityBreachXml.LEAKED_PASSWORDS,
    )
    val socialSecurityStatement = setOf(
        SocialSecurityStatementXml.SOCIAL_SECURITY_NUMBER,
    )
    val wiFi = setOf(
        WiFiXml.PASSPHRASE,
    )
}