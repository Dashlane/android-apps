package com.dashlane.login.pages

import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import com.skocken.presentation.definition.Base
import kotlinx.coroutines.CoroutineScope



interface LoginBaseContract {

    interface View : Base.IView {
        

        fun requestFocus()

        

        fun showError(@StringRes errorResId: Int, onClick: () -> Unit = {})

        

        fun showError(error: CharSequence?, onClick: () -> Unit = {})

        

        var showProgress: Boolean

        

        var email: String?

        

        fun prepareForTransitionStart()

        

        fun prepareForTransitionEnd()

        

        fun init(savedInstanceState: Bundle?)

        

        fun onSaveInstanceState(outState: Bundle)
    }

    interface Presenter : Base.IPresenter, CoroutineScope {

        

        var visible: Boolean

        var showProgress: Boolean

        

        fun onShow()

        

        fun onNextClicked()

        

        fun notifyOffline()

        

        fun notifyExpiredVersion()

        

        fun notifyNetworkError()

        

        fun onBackPressed(): Boolean

        

        fun onCreate(savedInstanceState: Bundle?)

        

        fun onSaveInstanceState(outState: Bundle)

        

        fun onStart()

        

        fun onNewIntent()

        

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        

        fun getViewOrNull(): View?
    }

    interface DataProvider : Base.IDataProvider {

        val username: String

        

        fun onShow()

        

        fun onBack()
    }

    

    class OfflineException(cause: Throwable? = null) : Exception(cause)

    

    class NetworkException(cause: Throwable? = null) : Exception(cause)

    

    class ExpiredVersionException(cause: Throwable? = null) : Exception(cause)
}