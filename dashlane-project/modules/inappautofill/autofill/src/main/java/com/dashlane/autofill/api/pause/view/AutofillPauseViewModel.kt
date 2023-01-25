package com.dashlane.autofill.api.pause.view

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dashlane.autofill.api.pause.AutofillApiPauseComponent
import com.dashlane.autofill.api.pause.dagger.AutofillPauseActivityViewModelComponent
import com.dashlane.autofill.api.pause.dagger.DaggerAutofillPauseActivityViewModelComponent



class AutofillPauseViewModel(application: Application, isShownInsideDashlane: Boolean) : ViewModel() {
    val component: AutofillPauseActivityViewModelComponent = DaggerAutofillPauseActivityViewModelComponent.factory()
        .create(AutofillApiPauseComponent(application), isShownInsideDashlane)

    class Factory(
        private val application: Application,
        private val isShownInsideDashlane: Boolean
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            check(modelClass == AutofillPauseViewModel::class.java)
            @Suppress("UNCHECKED_CAST")
            return AutofillPauseViewModel(
                application,
                isShownInsideDashlane
            ) as T
        }
    }
}