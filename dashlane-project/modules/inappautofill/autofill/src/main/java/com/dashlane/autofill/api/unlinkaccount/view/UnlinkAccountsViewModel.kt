package com.dashlane.autofill.api.unlinkaccount.view

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dashlane.autofill.api.internal.AutofillApiComponent
import com.dashlane.autofill.api.rememberaccount.AutofillApiRememberAccountComponent
import com.dashlane.autofill.api.unlinkaccount.AutofillApiUnlinkAccountsComponent
import com.dashlane.autofill.api.unlinkaccount.dagger.DaggerUnlinkAccountsViewModelComponent
import com.dashlane.autofill.api.unlinkaccount.dagger.UnlinkAccountsViewModelComponent
import com.dashlane.autofill.formdetector.model.AutoFillFormSource



class UnlinkAccountsViewModel(application: Application, autoFillFormSource: AutoFillFormSource) : ViewModel() {
    val component: UnlinkAccountsViewModelComponent = DaggerUnlinkAccountsViewModelComponent.factory()
        .create(
            autoFillFormSource,
            AutofillApiComponent(application),
            AutofillApiUnlinkAccountsComponent(application),
            AutofillApiRememberAccountComponent(application),
            this.viewModelScope
        )

    class Factory(
        private val application: Application,
        private val autoFillFormSource: AutoFillFormSource
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            check(modelClass == UnlinkAccountsViewModel::class.java)
            @Suppress("UNCHECKED_CAST")
            return UnlinkAccountsViewModel(
                application,
                autoFillFormSource
            ) as T
        }
    }
}
