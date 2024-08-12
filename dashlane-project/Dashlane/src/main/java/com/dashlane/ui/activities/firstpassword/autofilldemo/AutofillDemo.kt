package com.dashlane.ui.activities.firstpassword.autofilldemo

import android.os.Bundle
import androidx.annotation.IdRes
import com.skocken.presentation.definition.Base

interface AutofillDemo {
    interface Presenter : Base.IPresenter {
        fun onCreate(url: String, login: String, password: String, savedInstanceState: Bundle?)
        fun onEditTextFocusAcquired(@IdRes id: Int)
        fun onAutofillTriggered()
        fun onButtonFinishClicked()
        fun onSaveInstanceState(outState: Bundle)
    }

    interface ViewProxy : Base.IView {
        fun setWebsiteIcon(domain: String?)
        fun showAutofillSuggestion(login: String?, url: String, editTextId: Int)
        fun setCredential(login: String?, password: String?)
        fun hideAutofillSuggestion()
        fun enableFinish()
    }

    interface DataProvider : Base.IDataProvider
}