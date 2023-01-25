package com.dashlane.createaccount.pages.settings

import com.skocken.presentation.provider.BaseDataProvider
import javax.inject.Inject

class CreateAccountSettingsDataProvider @Inject constructor(
    override val logger: CreateAccountSettingsLogger
) : BaseDataProvider<CreateAccountSettingsContract.Presenter>(), CreateAccountSettingsContract.DataProvider {
    override fun onShow() = logger.logLand()
    override fun onBack() = logger.logBack()
}