package com.dashlane.session

import com.dashlane.util.isValidEmail

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