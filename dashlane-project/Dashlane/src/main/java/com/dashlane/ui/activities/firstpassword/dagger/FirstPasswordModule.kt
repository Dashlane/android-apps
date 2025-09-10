package com.dashlane.ui.activities.firstpassword.dagger

import com.dashlane.ui.activities.firstpassword.autofilldemo.AutofillDemo
import com.dashlane.ui.activities.firstpassword.autofilldemo.AutofillDemoDataProvider
import com.dashlane.ui.activities.firstpassword.autofilldemo.AutofillDemoPresenter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface FirstPasswordModule {
    @Binds
    fun bindAutofillDemoDataProvider(dataProvider: AutofillDemoDataProvider): AutofillDemo.DataProvider

    @Binds
    fun bindAutofillDemoPresenter(presenter: AutofillDemoPresenter): AutofillDemo.Presenter
}