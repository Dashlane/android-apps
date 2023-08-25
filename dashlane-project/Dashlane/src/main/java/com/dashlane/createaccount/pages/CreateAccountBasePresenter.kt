package com.dashlane.createaccount.pages

import android.content.Intent
import com.skocken.presentation.definition.Base
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.properties.Delegates

abstract class CreateAccountBasePresenter<D : CreateAccountBaseContract.DataProvider, V : Base.IView> :
    BasePresenter<D, V>(), CreateAccountBaseContract.Presenter {

    private val job = Job()
    protected val coroutineScope = CoroutineScope(job)

    override fun onShow() = Unit
    final override fun onBackPressed(): Boolean {
        return false
    }

    override var visible by Delegates.observable(false) { _, _, value -> onVisibilityChanged(value) }

    open fun onVisibilityChanged(visible: Boolean) {}

    override fun onDestroy() {
        job.cancel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = Unit
}