package com.dashlane.guidedpasswordchange.internal

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.skocken.presentation.definition.Base

internal interface OnboardingGuidedPasswordChangeContract {
    interface ViewProxy : Base.IView {
        var currentIllustration: Int
        fun showEnableAutofillApiDialog()
    }

    interface Presenter : Base.IPresenter {
        fun onCreate(savedInstanceState: Bundle?)

        fun onSaveInstanceState(outState: Bundle?)

        fun onStepClicked(index: Int)

        fun onChangePasswordClicked()

        fun onEnableAutofillApiClicked()

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    }

    interface DataProvider : Base.IDataProvider {
        suspend fun getPasswordChangeUrl(domain: String): Uri?
        fun isAutofillApiEnabled(context: Context): Boolean
    }
}