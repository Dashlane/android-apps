package com.dashlane.security.darkwebmonitoring.dagger

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.dashlane.security.darkwebmonitoring.DarkWebMonitoringAlertViewModel
import com.dashlane.security.darkwebmonitoring.DarkWebMonitoringContract
import com.dashlane.security.darkwebmonitoring.DarkWebMonitoringDataProvider
import com.dashlane.security.darkwebmonitoring.DarkWebMonitoringPresenter
import com.dashlane.security.darkwebmonitoring.detail.BreachAlertDetail
import com.dashlane.security.darkwebmonitoring.detail.BreachAlertDetailDataProvider
import com.dashlane.security.darkwebmonitoring.detail.BreachAlertDetailFragmentArgs
import com.dashlane.security.darkwebmonitoring.detail.BreachAlertDetailLogger
import com.dashlane.security.darkwebmonitoring.detail.BreachAlertDetailPresenter
import com.dashlane.security.identitydashboard.breach.BreachWrapper
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
abstract class DarkWebMonitoringModule {

    @Binds
    abstract fun bindDWMPresenter(presenter: DarkWebMonitoringPresenter): DarkWebMonitoringContract.Presenter

    @Binds
    abstract fun bindDWMDataProvider(dataProvider: DarkWebMonitoringDataProvider): DarkWebMonitoringContract.DataProvider

    @Binds
    abstract fun bindBreachAlertDetailDataProvider(dataProvider: BreachAlertDetailDataProvider): BreachAlertDetail.DataProvider

    @Binds
    abstract fun bindBreachAlertDetailLogger(logger: BreachAlertDetailLogger): BreachAlertDetail.Logger

    @Binds
    abstract fun bindBreachAlertDetailPresenter(presenter: BreachAlertDetailPresenter): BreachAlertDetail.Presenter

    companion object {

        @Provides
        fun provideDWMAlertViewModel(fragment: Fragment): DarkWebMonitoringAlertViewModel =
            ViewModelProvider(fragment)[DarkWebMonitoringAlertViewModel::class.java]

        @Provides
        fun provideBreachWrapper(fragment: Fragment): BreachWrapper =
            BreachAlertDetailFragmentArgs.fromBundle(fragment.requireArguments()).breach

        @Provides
        fun provideLifecycleOwner(fragment: Fragment): LifecycleOwner = fragment
    }
}