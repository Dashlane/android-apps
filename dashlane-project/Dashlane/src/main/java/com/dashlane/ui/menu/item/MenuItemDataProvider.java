package com.dashlane.ui.menu.item;

import android.os.Bundle;

import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.navigation.NavigationUtils;
import com.dashlane.navigation.Navigator;
import com.skocken.presentation.provider.BaseDataProvider;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;

import static com.dashlane.ui.menu.item.MenuItemDef.IDataProvider;
import static com.dashlane.ui.menu.item.MenuItemDef.IPresenter;



public class MenuItemDataProvider extends BaseDataProvider<IPresenter> implements IDataProvider {

    private final Navigator mNavigator;

    private MenuItem mMenuItem;

    private NavController.OnDestinationChangedListener mOnDestinationChangedListener;
    private boolean mIsListenerAdded = false;

    public MenuItemDataProvider() {
        this(SingletonProvider.getNavigator());
    }

    @VisibleForTesting
    MenuItemDataProvider(Navigator navigator) {
        mNavigator = navigator;
        mOnDestinationChangedListener = new NavigationChangedListener(this);
    }

    @Override
    protected void onPresenterChanged() {
        super.onPresenterChanged();
        if (mIsListenerAdded) return;
        mIsListenerAdded = true;
        mNavigator.addOnDestinationChangedListener(mOnDestinationChangedListener);
    }

    @Override
    public boolean isSelected() {
        NavDestination currentDestination = mNavigator.getCurrentDestination();
        if (currentDestination == null) return false;
        return NavigationUtils.matchDestination(currentDestination, mMenuItem.getDestinationResIds());
    }

    @Override
    public MenuItem getItem() {
        return mMenuItem;
    }

    @Override
    public void setItem(MenuItem object) {
        if (mMenuItem == object) return;

        mMenuItem = object;
    }

    private static class NavigationChangedListener implements NavController.OnDestinationChangedListener {

        private final WeakReference<MenuItemDataProvider> mProviderReference;

        private NavigationChangedListener(MenuItemDataProvider dataProvider) {
            mProviderReference = new WeakReference<>(dataProvider);
        }

        @Override
        public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination,
                @Nullable Bundle arguments) {
            MenuItemDataProvider dataProvider = mProviderReference.get();
            if (dataProvider == null) {
                
                controller.removeOnDestinationChangedListener(this);
            } else {
                dataProvider.getPresenter().update();
            }
        }
    }
}
