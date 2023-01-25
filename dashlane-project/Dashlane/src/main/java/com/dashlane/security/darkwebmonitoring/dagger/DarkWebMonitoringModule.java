package com.dashlane.security.darkwebmonitoring.dagger;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.dashlane.security.darkwebmonitoring.DarkWebMonitoringAlertViewModel;
import com.dashlane.security.darkwebmonitoring.DarkWebMonitoringContract;
import com.dashlane.security.darkwebmonitoring.DarkWebMonitoringDataProvider;
import com.dashlane.security.darkwebmonitoring.DarkWebMonitoringPresenter;
import com.dashlane.security.darkwebmonitoring.detail.BreachAlertDetail;
import com.dashlane.security.darkwebmonitoring.detail.BreachAlertDetailDataProvider;
import com.dashlane.security.darkwebmonitoring.detail.BreachAlertDetailFragmentArgs;
import com.dashlane.security.darkwebmonitoring.detail.BreachAlertDetailLogger;
import com.dashlane.security.darkwebmonitoring.detail.BreachAlertDetailPresenter;
import com.dashlane.security.identitydashboard.breach.BreachWrapper;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.FragmentComponent;

@Module
@InstallIn(FragmentComponent.class)
public abstract class DarkWebMonitoringModule {
    @Binds
    abstract DarkWebMonitoringContract.Presenter bindDWMPresenter(DarkWebMonitoringPresenter presenter);

    @Binds
    abstract DarkWebMonitoringContract.DataProvider bindDWMDataProvider(
            DarkWebMonitoringDataProvider dataProvider);


    @Binds
    abstract BreachAlertDetail.DataProvider bindBreachAlertDetailDataProvider(
            BreachAlertDetailDataProvider dataProvider);

    @Binds
    abstract BreachAlertDetail.Logger bindBreachAlertDetailLogger(BreachAlertDetailLogger logger);

    @Binds
    abstract BreachAlertDetail.Presenter bindBreachAlertDetailPresenter(BreachAlertDetailPresenter presenter);

    @Provides
    static DarkWebMonitoringAlertViewModel provideDWMAlertViewModel(Fragment fragment) {
        return new ViewModelProvider(fragment.requireActivity()).get(DarkWebMonitoringAlertViewModel.class);
    }

    @Provides
    static BreachWrapper provideBreachWrapper(Fragment fragment) {
        return BreachAlertDetailFragmentArgs.fromBundle(fragment.requireArguments()).getBreach();
    }

    @Provides
    static LifecycleOwner provideLifecycleOwner(Fragment fragment) {
        return fragment;
    }
}
