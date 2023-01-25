package com.dashlane.authenticator

import android.net.Uri
import com.dashlane.util.isSemanticallyNull
import java.util.Locale



object UriParser {
    

    private const val OTP_SCHEME = "otpauth"

    

    private const val TOTP = "totp"

    

    private const val HOTP = "hotp"

    

    private const val SECRET_PARAM = "secret"
    private const val COUNTER_PARAM = "counter"
    private const val PERIOD_PARAM = "period"
    private const val DIGITS_PARAM = "digits"
    private const val ISSUER_PARAM = "issuer"
    private const val ALGORITHM_PARAM = "algorithm"

    

    const val DEFAULT_ALGORITHM = "SHA1"
    const val DEFAULT_DIGITS = 6
    const val DEFAULT_PERIOD = 30
    const val DEFAULT_COUNTER = 0L

    

    fun parse(uri: Uri): Otp? {
        val scheme = uri.scheme
        
        if (scheme == null || OTP_SCHEME != scheme.lowercase(Locale.US)) {
            return null
        }
        
        val userAndIssuer = getUserAndIssuer(uri) ?: return null
        val user = userAndIssuer.first ?: return null
        
        
        val secret = uri.getQueryParameter(SECRET_PARAM)?.sanitizeOtpSecret() ?: return null
        if (secret.isSemanticallyNull()) return null
        
        val digits = uri.getQueryParameter(DIGITS_PARAM)?.let {
            
            it.toIntOrNull() ?: return@parse null
        }
        val algorithm = uri.getQueryParameter(ALGORITHM_PARAM)
        val issuer = userAndIssuer.second
        return when (uri.authority) {
            TOTP -> parseTotp(uri, digits, algorithm, user, issuer, secret)
            HOTP -> parseHotp(uri, digits, algorithm, user, issuer, secret)
            else -> null
        }
    }

    fun incrementHotpCounter(hotp: Hotp): Hotp {
        val newCount = hotp.counter + 1
        val newUrl = hotp.url?.replace("$COUNTER_PARAM=${hotp.counter}", "$COUNTER_PARAM=$newCount")
        return hotp.copy(counter = hotp.counter + 1, url = newUrl)
    }

    private fun parseHotp(
        uri: Uri,
        digits: Int?,
        algorithm: String?,
        user: String,
        issuer: String?,
        secret: String?
    ): Hotp? {
        val counter = uri.getQueryParameter(COUNTER_PARAM)?.let {
            
            it.toIntOrNull() ?: return null
        }
        return Hotp(
            counter = counter?.toLong() ?: DEFAULT_COUNTER,
            digits = digits ?: DEFAULT_DIGITS,
            algorithm = algorithm ?: DEFAULT_ALGORITHM,
            user = user,
            issuer = issuer,
            secret = secret,
            url = uri.toString()
        )
    }

    private fun parseTotp(
        uri: Uri,
        digits: Int?,
        algorithm: String?,
        user: String,
        issuer: String?,
        secret: String?
    ): Totp? {
        val period = uri.getQueryParameter(PERIOD_PARAM)?.let {
            
            it.toIntOrNull() ?: return null
        }
        return Totp(
            period = period ?: DEFAULT_PERIOD,
            digits = digits ?: DEFAULT_DIGITS,
            algorithm = algorithm ?: DEFAULT_ALGORITHM,
            user = user,
            issuer = issuer,
            secret = secret,
            url = uri.toString()
        )
    }

    private fun getUserAndIssuer(uri: Uri): Pair<String?, String?>? {
        val path = uri.path
        if (path == null || !path.startsWith("/")) {
            return null
        }
        
        val user = path.substring(1).trim()
        val issuer = uri.getQueryParameter(ISSUER_PARAM)?.sanitizeIssuer()
        val foundUserAndIssuer = when {
            user.contains(":") -> findUserAndIssuer(user, issuer, ":")
            user.contains("@") -> findUserAndIssuer(user, issuer, "@")
            else -> null
        }
        foundUserAndIssuer?.let { return it.first to it.second }
        return user to issuer
    }

    private fun findUserAndIssuer(
        user: String,
        originalIssuer: String?,
        separator: String
    ): Pair<String, String>? {
        val foundIssuer = (if (separator == "@") {
            user.substring(user.indexOf(separator) + 1, user.length)
        } else {
            user.substring(0, user.indexOf(separator))
        }).sanitizeIssuer()
        if (originalIssuer == null || originalIssuer == foundIssuer) {
            if (separator == "@") {
                return user.substring(0, user.indexOf(separator)) to foundIssuer
            }
            return user.substring(foundIssuer.length + 1) to foundIssuer
        }
        return null
    }

    private fun String.sanitizeIssuer() = replaceFirst("www.", "")
}