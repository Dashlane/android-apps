package com.dashlane.createaccount.pages

import android.content.Intent
import com.skocken.presentation.definition.Base

interface CreateAccountBaseContract {
    interface Presenter : Base.IPresenter {

        var visible: Boolean

        val nextEnabled: Boolean

        fun onNextClicked()

        fun onShow()

        fun onBackPressed(): Boolean

        fun onDestroy()

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    }

    interface DataProvider : Base.IDataProvider

    class NetworkException(cause: Throwable? = null) : Exception(cause)
    class ExpiredVersionException(cause: Throwable? = null) : Exception(cause)
}