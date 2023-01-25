package com.dashlane.login.pages.pin

import android.view.View
import com.dashlane.login.pages.LoginLockBaseContract
import com.dashlane.ui.widgets.PinCodeKeyboardView



interface PinLockContract {

    interface ViewProxy : LoginLockBaseContract.ViewProxy {
        fun setupKeyboard(pinLockKeyboardListener: PinCodeKeyboardView.PinCodeKeyboardListener)

        

        fun setPinsVisible(quantity: Int)

        

        fun enableAllKeyboardButtons(enabled: Boolean)

        

        fun animateSuccess(disableAnimationEffect: Boolean): Int

        

        fun animateError()

        

        fun setTextError(error: String)

        

        fun setTopic(topic: String)

        

        fun setQuestion(question: String)

        

        fun initLogoutButton(text: String?, listener: View.OnClickListener)
    }

    interface Presenter : LoginLockBaseContract.Presenter {
        

        fun enableAllKeyboardButtons(enabled: Boolean)

        

        fun onUnlockSuccess()

        

        fun onUnlockError()

        

        fun onRequestReenterPin()

        

        fun clearInput()

        

        fun animateError()

        

        fun newPinConfirmed(disableAnimationEffect: Boolean)
    }

    interface DataProvider : LoginLockBaseContract.DataProvider {
        

        val userPin: StringBuilder

        

        var currentStep: Int

        

        var firstStepPin: String?

        

        fun appendToUserPin(value: Int): Boolean

        

        fun onUserPinUpdated(disableAnimationEffect: Boolean)

        

        fun removeLastPinNumber()

        

        fun savePinValue()
    }
}