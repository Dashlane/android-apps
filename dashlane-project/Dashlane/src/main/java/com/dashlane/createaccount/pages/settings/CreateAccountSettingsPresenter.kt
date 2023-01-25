package com.dashlane.createaccount.pages.settings

import com.dashlane.createaccount.CreateAccountPresenter
import com.dashlane.createaccount.pages.CreateAccountBasePresenter

class CreateAccountSettingsPresenter(
    val presenter: CreateAccountPresenter
) : CreateAccountSettingsContract.Presenter,
    CreateAccountBasePresenter<CreateAccountSettingsContract.DataProvider, CreateAccountSettingsContract.ViewProxy>() {

    override val nextEnabled: Boolean = true

    override fun onNextClicked() {
        presenter.updateSettings(view.biometricSettingChecked, view.resetMpSettingChecked)
        presenter.showTos()
    }
}