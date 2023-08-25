package com.dashlane.authenticator

import android.os.Parcelable
import androidx.annotation.Keep
import com.dashlane.authenticator.UriParser.DEFAULT_ALGORITHM
import com.dashlane.authenticator.UriParser.DEFAULT_COUNTER
import com.dashlane.authenticator.UriParser.DEFAULT_DIGITS
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Hotp(
    val counter: Long = DEFAULT_COUNTER,
    override val digits: Int = DEFAULT_DIGITS,
    override val algorithm: String = DEFAULT_ALGORITHM,
    override val user: String? = null,
    override val secret: String? = null,
    override val issuer: String? = null,
    override val url: String? = null,
) : Parcelable, Otp(digits, algorithm, user, secret, issuer, url) {

    override fun getPin(timeOrCounter: Long?): Pin? {
        val counter = timeOrCounter ?: counter
        val currentPin = computePin(secret, counter) ?: return null
        return Pin(
            code = currentPin,
            counter = counter
        )
    }

    data class Pin(
        override val code: String,
        val counter: Long
    ) : com.dashlane.authenticator.Pin
}