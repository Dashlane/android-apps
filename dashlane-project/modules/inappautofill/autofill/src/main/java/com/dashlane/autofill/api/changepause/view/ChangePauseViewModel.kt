package com.dashlane.autofill.api.changepause.view

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dashlane.autofill.api.changepause.AutofillApiChangePauseComponent
import com.dashlane.autofill.api.changepause.dagger.ChangePauseViewModelComponent
import com.dashlane.autofill.api.changepause.dagger.DaggerChangePauseViewModelComponent
import com.dashlane.autofill.api.pause.AutofillApiPauseComponent
import com.dashlane.autofill.formdetector.model.AutoFillFormSource



class ChangePauseViewModel(application: Application, autoFillFormSource: AutoFillFormSource) : ViewModel() {
    val component: ChangePauseViewModelComponent = DaggerChangePauseViewModelComponent.factory()
        .create(
            autoFillFormSource,
            AutofillApiChangePauseComponent(application),
            AutofillApiPauseComponent(application),
            this.viewModelScope
        )

    class Factory(
        private val application: Application,
        private val autoFillFormSource: AutoFillFormSource
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            check(modelClass == ChangePauseViewModel::class.java)
            @Suppress("UNCHECKED_CAST")
            return ChangePauseViewModel(
                application,
                autoFillFormSource
            ) as T
        }
    }
}
