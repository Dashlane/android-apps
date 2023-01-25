@file:JvmName("PageViewUtil")

package com.dashlane.util

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.inject.HermesComponent
import com.skocken.presentation.definition.Base



@JvmOverloads
fun Activity.setCurrentPageView(page: AnyPage, fromAutofill: Boolean = false) {
    if (this is CurrentPageViewLogger.Owner) {
        currentPageViewLogger.setCurrentPageView(page, fromAutofill)
    } else {
        
        logI("PageView") { "Event not sent" }

        if (BuildConfig.DEBUG) {
            error("Host Activity must extend CurrentPageViewLogger.Owner")
        }
    }
}



@JvmOverloads
fun Fragment.setCurrentPageView(page: AnyPage, fromAutofill: Boolean = false) {
    activity?.setCurrentPageView(page, fromAutofill)
}



@JvmOverloads
fun Base.IPresenter.setCurrentPageView(page: AnyPage, fromAutofill: Boolean = false) {
    activity?.setCurrentPageView(page, fromAutofill)
}



@JvmOverloads
fun Context.logPageView(page: AnyPage, fromAutofill: Boolean = false) {
    HermesComponent(this)
        .logRepository
        .logPageView(page, fromAutofill)
}



private fun LogRepository.logPageView(page: AnyPage, fromAutofill: Boolean) {
    logI("PageView") { "page: $page fromAutofill: $fromAutofill" }
    queuePageView(
        component = if (fromAutofill) BrowseComponent.OS_AUTOFILL else BrowseComponent.MAIN_APP,
        page = page
    )
}

interface CurrentPageViewLogger {
    fun setCurrentPageView(page: AnyPage, fromAutofill: Boolean = false)

    interface Owner {
        val currentPageViewLogger: CurrentPageViewLogger
    }
}

@Suppress("FunctionNaming")
fun CurrentPageViewLogger(activity: AppCompatActivity): CurrentPageViewLogger = CurrentPageViewLoggerImpl(activity)

private class CurrentPageViewLoggerImpl(
    activity: AppCompatActivity
) : CurrentPageViewLogger, LifecycleEventObserver {
    private val logRepository = HermesComponent(activity).logRepository
    private var latestPageView: Pair<AnyPage, Boolean>? = null
    private var resumedAtLeastOnce = false

    init {
        activity.lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_RESUME) {
            if (!resumedAtLeastOnce) {
                resumedAtLeastOnce = true
            } else {
                
                logLatestPageView()
            }
        }
    }

    override fun setCurrentPageView(page: AnyPage, fromAutofill: Boolean) {
        latestPageView = page to fromAutofill
        logLatestPageView()
    }

    private fun logLatestPageView() {
        latestPageView?.let { (page, fromAutofill) -> logRepository.logPageView(page, fromAutofill) }
    }
}