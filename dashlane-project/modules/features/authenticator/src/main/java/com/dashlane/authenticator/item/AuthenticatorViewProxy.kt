package com.dashlane.authenticator.item

import android.content.res.ColorStateList
import android.widget.ImageView
import android.widget.TextView
import com.dashlane.authenticator.Hotp
import com.dashlane.authenticator.Otp
import com.dashlane.authenticator.Pin
import com.dashlane.authenticator.R
import com.dashlane.authenticator.Totp
import com.dashlane.authenticator.UriParser.incrementHotpCounter
import com.dashlane.ui.drawable.CountDownDrawable
import com.dashlane.util.getThemeAttrDrawable
import com.dashlane.util.otpToDisplay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



class AuthenticatorViewProxy(
    private val otpCodeView: TextView,
    private val actionView: ImageView,
    private val scope: CoroutineScope,
    private val otp: Otp,
    private val valueUpdate: (Otp) -> Unit
) {
    private val countDown = CountDownDrawable(otpCodeView.context)
    private val tintColor = otpCodeView.context.getColor(R.color.text_brand_standard)

    init {
        when (otp) {
            is Totp -> setupTotp(otp)
            is Hotp -> setupHotp(otp)
        }
    }

    private fun setupHotp(hotp: Hotp) {
        val pin = hotp.getPin()
        if (pin != null) {
            setupCode(pin)
            countDown.apply {
                setVisible(false, false)
                stop()
            }
            actionView.apply {
                background = context.getThemeAttrDrawable(android.R.attr.selectableItemBackground)
                imageTintList = ColorStateList.valueOf(tintColor)
                setImageResource(R.drawable.ic_refresh)
                setOnClickListener {
                    
                    val newHotp = incrementHotpCounter(hotp)
                    setupHotp(newHotp)
                    
                    valueUpdate.invoke(newHotp)
                }
            }
        }
    }

    private fun setupTotp(totp: Totp) {
        val pin = totp.getPin()
        if (pin != null) {
            setupCode(pin)
            actionView.apply {
                background = null
                setImageDrawable(countDown)
                setOnClickListener(null)
                isClickable = false
            }
            countDown.apply {
                setColor(tintColor)
                setVisible(true, false)
                start()
            }
            scope.launch {
                scheduleRefresh(pin.timeRemaining.toMillis())
            }
        }
    }

    private suspend fun scheduleRefresh(delayMs: Long) {
        delay(delayMs)
        val totp = otp as? Totp ?: return
        val pin = totp.getPin() ?: return
        setupCode(pin)
        scheduleRefresh(pin.timeRemaining.toMillis())
    }

    private fun setupCode(pin: Pin) {
        
        
        val code = pin.code.otpToDisplay()
        otpCodeView.text = code
        if (pin is Totp.Pin) {
            countDown.setIntervalInfo(pin.refreshInterval.toMillis(), pin.timeRemaining.toMillis())
        }
    }
}
