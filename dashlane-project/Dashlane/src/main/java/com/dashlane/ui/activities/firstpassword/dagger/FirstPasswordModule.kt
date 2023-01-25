package com.dashlane.ui.activities.firstpassword.dagger

import com.dashlane.ui.activities.firstpassword.AddFirstPassword
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.dashlane.ui.activities.firstpassword.AddFirstPasswordDataProvider
import com.dashlane.ui.activities.firstpassword.AddFirstPasswordLogger
import com.dashlane.ui.activities.firstpassword.AddFirstPasswordPresenter
import com.dashlane.ui.activities.firstpassword.autofilldemo.AutofillDemo
import com.dashlane.ui.activities.firstpassword.autofilldemo.AutofillDemoDataProvider
import com.dashlane.ui.activities.firstpassword.autofilldemo.AutofillDemoLogger
import com.dashlane.ui.activities.firstpassword.autofilldemo.AutofillDemoPresenter
import dagger.Binds
import dagger.Module

@Module
@InstallIn(SingletonComponent::class)
interface FirstPasswordModule {
    @Binds
    fun bindAddFirstPasswordDataProvider(dataProvider: AddFirstPasswordDataProvider): AddFirstPassword.DataProvider

    @Binds
    fun bindAddFirstPasswordLogger(logger: AddFirstPasswordLogger): AddFirstPassword.Logger

    @Binds
    fun bindAddFirstPasswordPresenter(presenter: AddFirstPasswordPresenter): AddFirstPassword.Presenter

    @Binds
    fun bindAutofillDemoDataProvider(dataProvider: AutofillDemoDataProvider): AutofillDemo.DataProvider

    @Binds
    fun bindAutofillDemoLogger(logger: AutofillDemoLogger): AutofillDemo.Logger

    @Binds
    fun bindAutofillDemoPresenter(presenter: AutofillDemoPresenter): AutofillDemo.Presenter
}