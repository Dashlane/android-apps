package com.dashlane.debug

import com.dashlane.session.Session
import com.dashlane.user.Username
import java.util.regex.Pattern

object TestAccountDebug {
    fun Session.isTestingAccount() = username.isTestingAccount()

    fun Username.isTestingAccount() = email.startsWith(TEST_PREFIX)

    fun Session.isSmokeTestAccount() = this.username.isSmokeTestAccount()

    fun Username.isSmokeTestAccount() =
        AUTOMATION_ACCOUNT_REGEX.matcher(email).matches() || AUTOMATION_SSO_ACCOUNT_REGEX.matcher(email).matches()

    @Suppress("InternalTestExpressions")
    const val TEST_PREFIX = "kw_test_"

    private val AUTOMATION_ACCOUNT_REGEX =
        Pattern.compile("randomemail@provider.com){1}$")

    private val AUTOMATION_SSO_ACCOUNT_REGEX =
        Pattern.compile("^$TEST_PREFIX.*@.*sso\\.kwtest\\.io\$")
}