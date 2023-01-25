package com.dashlane.autofill.api.emptywebsitewarning.dagger

import com.dashlane.autofill.api.emptywebsitewarning.EmptyWebsiteWarningContract
import com.dashlane.autofill.api.emptywebsitewarning.domain.EmptyWebsiteWarningDataProvider
import com.dashlane.autofill.api.emptywebsitewarning.presenter.EmptyWebsiteWarningPresenter
import com.dashlane.autofill.api.emptywebsitewarning.view.EmptyWebsiteWarningViewProxy
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
internal interface AutofillEmptyWebsiteWarningModule {
    @EmptyWebsiteWarningScope
    @Binds
    fun bindsEmptyWebsiteWarningViewProxy(impl: EmptyWebsiteWarningViewProxy): EmptyWebsiteWarningContract.ViewProxy

    @EmptyWebsiteWarningScope
    @Binds
    fun bindsEmptyWebsiteWarningPresenter(impl: EmptyWebsiteWarningPresenter): EmptyWebsiteWarningContract.Presenter

    @EmptyWebsiteWarningScope
    @Binds
    fun bindsEmptyWebsiteWarningDataProvider(impl: EmptyWebsiteWarningDataProvider): EmptyWebsiteWarningContract.DataProvider
}