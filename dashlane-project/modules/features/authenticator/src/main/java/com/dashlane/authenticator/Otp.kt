package com.dashlane.authenticator

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.core.net.toUri
import com.dashlane.authenticator.UriParser.DEFAULT_ALGORITHM
import com.dashlane.authenticator.UriParser.DEFAULT_DIGITS
import com.dashlane.authenticator.UriParser.DEFAULT_PERIOD
import com.dashlane.authenticator.util.Base32String
import com.dashlane.authenticator.util.PasscodeGenerator
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.xml.domain.SyncObject
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
open class Otp(
    open val digits: Int,
    open val algorithm: String,
    open val user: String?,
    open val secret: String?,
    open val issuer: String?,
    open val url: String?
) : Parcelable {

    fun isStandardOtp() = this is Totp && secret.isNotSemanticallyNull() &&
        DEFAULT_DIGITS == digits && DEFAULT_PERIOD == period &&
        DEFAULT_ALGORITHM.equals(algorithm, ignoreCase = true)

    open fun getPin(timeOrCounter: Long? = null): Pin? = null

    fun computePin(secret: String?, currentState: Long): String? {
        secret ?: return null
        return try {
            val signer = getSigningOracle(secret) ?: return null
            val pcg = PasscodeGenerator(signer, digits)
            pcg.generateResponseCode(currentState)
        } catch (e: Exception) {
            null
        }
    }

    @Throws(RuntimeException::class)
    private fun getSigningOracle(secret: String): PasscodeGenerator.Signer? {
        return try {
            val keyBytes = Base32String.decode(secret)
            val macAlgorithm = when (algorithm.uppercase()) {
                "SHA224" -> "HmacSHA224"
                "SHA256" -> "HmacSHA256"
                "SHA384" -> "HmacSHA384"
                "SHA512" -> "HmacSHA512"
                DEFAULT_ALGORITHM -> "HmacSHA1"
                else -> throw UnsupportedOperationException("$algorithm is not supported")
            }
            val mac = Mac.getInstance(macAlgorithm)
            mac.init(SecretKeySpec(keyBytes, ""))

            
            PasscodeGenerator.Signer { data: ByteArray? -> mac.doFinal(data) }
        } catch (error: Exception) {
            throw RuntimeException("Cannot generate signing oracle", error)
        }
    }
}

fun SyncObject.Authentifiant.otp(): Otp? {
    val storedSecret = otpSecret?.toString()
    val defaultOtp =
        storedSecret?.let { if (it.isNotSemanticallyNull()) Totp(secret = it) else null }
    return otpUrl?.toString()?.let {
        val result = UriParser.parse(it.toUri()) ?: return defaultOtp
        if (storedSecret.isNotSemanticallyNull() && result.secret != storedSecret) {
            
            null
        } else {
            result
        }
    } ?: defaultOtp
}

internal fun String.sanitizeOtpSecret() = replace("\\s+".toRegex(), "")
