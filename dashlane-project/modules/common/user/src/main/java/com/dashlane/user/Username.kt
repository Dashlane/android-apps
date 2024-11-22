package com.dashlane.user

import com.dashlane.util.isValidEmail
import java.util.regex.Pattern

@Suppress("InternalTestExpressions")
const val TEST_PREFIX = "kw_test_"

private val automationAccountRegex =
    Pattern.compile("randomemail@provider.com){1}$")

private val automationSsoAccountRegex =
    Pattern.compile("^$TEST_PREFIX.*@.*sso\\.kwtest\\.io\$")

class Username private constructor(val email: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Username) return false
        if (email != other.email) return false
        return true
    }

    override fun hashCode(): Int =
        email.hashCode()

    override fun toString(): String =
        "Username(email='$email')"

    companion object {
        fun ofEmail(email: String): Username {
            val sanitizedEmail = sanitizeEmail(email)
            require(sanitizedEmail.isValidEmail()) { "Invalid email '$email'." }
            return Username(sanitizedEmail)
        }

        fun ofEmailOrNull(email: String): Username? {
            val sanitizedEmail = sanitizeEmail(email)
            if (!sanitizedEmail.isValidEmail()) return null
            return Username(sanitizedEmail)
        }

        private fun sanitizeEmail(email: String): String =
            email.trim().lowercase()
    }
}

fun Username.isTestingAccount() = email.startsWith(TEST_PREFIX)

fun Username.isSmokeTestAccount() =
    automationAccountRegex.matcher(email).matches() || automationSsoAccountRegex.matcher(email).matches()
