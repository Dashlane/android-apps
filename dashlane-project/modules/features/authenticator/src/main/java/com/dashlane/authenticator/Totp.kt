package com.dashlane.authenticator

import android.os.Parcelable
import androidx.annotation.Keep
import com.dashlane.authenticator.UriParser.DEFAULT_ALGORITHM
import com.dashlane.authenticator.UriParser.DEFAULT_DIGITS
import com.dashlane.authenticator.UriParser.DEFAULT_PERIOD
import com.dashlane.authenticator.util.TotpCounter
import kotlinx.parcelize.Parcelize
import java.time.Duration

@Keep
@Parcelize
data class Totp(
    val period: Int = DEFAULT_PERIOD,
    override val digits: Int = DEFAULT_DIGITS,
    override val algorithm: String = DEFAULT_ALGORITHM,
    override val user: String? = null,
    override val secret: String? = null,
    override val issuer: String? = null,
    override val url: String? = null
) : Otp(digits, algorithm, user, secret, issuer, url), Parcelable {

    override fun getPin(timeOrCounterMillis: Long?): Pin? {
        val totpCounter = TotpCounter(period.toLong())
        val timeMillis = timeOrCounterMillis ?: System.currentTimeMillis()
        val currentInterval = totpCounter.getValueAtTime(timeMillis / 1000)
        val currentPin = computePin(secret, currentInterval) ?: return null
        val interval = Duration.ofSeconds(period.toLong())
        val remaining = Duration.ofMillis((currentInterval + 1) * interval.toMillis() - timeMillis)
        return Pin(
            code = currentPin,
            refreshInterval = interval,
            timeRemaining = remaining
        )
    }

    data class Pin(
        override val code: String,
        val refreshInterval: Duration,
        val timeRemaining: Duration
    ) : com.dashlane.authenticator.Pin
}