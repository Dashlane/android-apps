package com.dashlane.csvimport.internal.onboardingchromeimport

import android.os.Bundle
import com.skocken.presentation.definition.Base

internal interface OnboardingChromeImportContract {
    interface ViewProxy : Base.IView {
        var currentIllustration: Int

        val illustrationCount: Int

        fun showImportError()
    }

    interface Presenter : Base.IPresenter {
        fun onCreate(savedInstanceState: Bundle?)

        fun onResume()

        fun onPause()

        fun onSaveInstanceState(outState: Bundle?)

        fun onDestroy()

        fun onSwipeLeft()

        fun onSwipeRight()

        fun onStepClicked(index: Int)

        fun onImportErrorSkipClicked()

        fun onImportErrorCanceled()

        fun onMayBeLaterClicked()

        fun onOpenChromeClicked()
    }

    interface DataProvider : Base.IDataProvider
}