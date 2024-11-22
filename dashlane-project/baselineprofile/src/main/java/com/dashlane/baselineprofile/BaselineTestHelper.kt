package com.dashlane.baselineprofile

import java.security.SecureRandom.getInstanceStrong

data object BaselineTestHelper {
    const val DEFAULT_TIMEOUT = 1000L
    const val PACKAGE_NAME = "com.dashlane"
    const val GET_STARTED_BUTTON_TEXT = "GET STARTED"
    const val LOGIN_BUTTON_TEXT = "LOG IN"
    const val CONTINUE_BUTTON_TEXT = "CONTINUE"
    const val SKIP_BUTTON_TEXT = "SKIP"
    const val EMAIL_TEXT = "Email"
    const val I_AGREE_TEXT = "I AGREE"

    const val CREATE_MP_TEXT = "Create your Master Password"
    const val TYPE_MP_TEXT = "Type your Master Password again"
    const val NEVER_USED_TEXT = "I have never used Dashlane before"

    const val TEST_PW = "Dashlane12"

    fun generateTestAccount() = "randomemail@provider.com"
}