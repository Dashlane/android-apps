package com.dashlane.autofill.formdetector.field

import android.view.View

object AutoFillHint {
    
    const val CREDIT_CARD_EXPIRATION_DATE = View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DATE
    const val CREDIT_CARD_EXPIRATION_DAY = View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DAY
    const val CREDIT_CARD_EXPIRATION_MONTH = View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH
    const val CREDIT_CARD_EXPIRATION_YEAR = View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR
    const val CREDIT_CARD_NUMBER = View.AUTOFILL_HINT_CREDIT_CARD_NUMBER
    const val CREDIT_CARD_SECURITY_CODE = View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE
    const val EMAIL_ADDRESS = View.AUTOFILL_HINT_EMAIL_ADDRESS
    const val NAME = View.AUTOFILL_HINT_NAME
    const val USERNAME = View.AUTOFILL_HINT_USERNAME
    const val PASSWORD = View.AUTOFILL_HINT_PASSWORD
    const val PHONE = View.AUTOFILL_HINT_PHONE
    const val POSTAL_ADDRESS = View.AUTOFILL_HINT_POSTAL_ADDRESS
    const val POSTAL_CODE = View.AUTOFILL_HINT_POSTAL_CODE

    
    const val SMS_OTP_CODE = "smsOTPCode"
    const val SMS_OTP_CODE_1 = "smsOTPCode1"
    const val SMS_OTP_CODE_2 = "smsOTPCode2"
    const val SMS_OTP_CODE_3 = "smsOTPCode3"
    const val SMS_OTP_CODE_4 = "smsOTPCode4"
    const val SMS_OTP_CODE_5 = "smsOTPCode5"
    const val SMS_OTP_CODE_6 = "smsOTPCode6"
    const val SMS_OTP_CODE_7 = "smsOTPCode7"
    const val SMS_OTP_CODE_8 = "smsOTPCode8"
    const val SMS_OTP_CODE_9 = "smsOTPCode9"

    
    const val CURRENT_PASSWORD = "current-password" 

    
    const val NEW_PASSWORD = "new-password"

    const val EDIT_TEXT_CANDIDATE = "editTextCandidate"
    const val EDIT_TEXT_PASSWORD_CANDIDATE = "editTextPasswordCandidate"

    val smsOtpCodePerDigit = arrayOf(
        SMS_OTP_CODE_1,
        SMS_OTP_CODE_2,
        SMS_OTP_CODE_3,
        SMS_OTP_CODE_4,
        SMS_OTP_CODE_5,
        SMS_OTP_CODE_6,
        SMS_OTP_CODE_7,
        SMS_OTP_CODE_8,
        SMS_OTP_CODE_9,
    )

    val all = arrayOf(
        CREDIT_CARD_EXPIRATION_DATE,
        CREDIT_CARD_EXPIRATION_DAY,
        CREDIT_CARD_EXPIRATION_MONTH,
        CREDIT_CARD_EXPIRATION_YEAR,
        CREDIT_CARD_NUMBER,
        CREDIT_CARD_SECURITY_CODE,
        EMAIL_ADDRESS,
        NAME,
        USERNAME,
        PASSWORD,
        PHONE,
        POSTAL_ADDRESS,
        POSTAL_CODE,
        SMS_OTP_CODE,
        SMS_OTP_CODE_1,
        SMS_OTP_CODE_2,
        SMS_OTP_CODE_3,
        SMS_OTP_CODE_4,
        SMS_OTP_CODE_5,
        SMS_OTP_CODE_6,
        SMS_OTP_CODE_7,
        SMS_OTP_CODE_8,
        SMS_OTP_CODE_9,
    )
}