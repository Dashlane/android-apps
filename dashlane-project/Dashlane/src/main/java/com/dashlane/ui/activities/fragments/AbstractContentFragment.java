package com.dashlane.ui.activities.fragments;

import static com.dashlane.R.id.ID_SEARCH_VIEW;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.widget.SearchView;

import com.dashlane.R;
import com.dashlane.authenticator.AuthenticatorDashboardFragment;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.ui.activities.fragments.vault.VaultFragment;
import com.dashlane.ui.fragments.BaseUiFragment;
import com.dashlane.ui.screens.fragments.userdata.CredentialAddStep1Fragment;
import com.dashlane.ui.util.ActionBarUtil;
import com.dashlane.util.ColorUtilsKt;
import com.dashlane.util.usagelogs.ViewLogger;


public abstract class AbstractContentFragment extends BaseUiFragment implements
                                                                     OnClickListener,
                                                                     SearchView.OnQueryTextListener,
                                                                     SearchView.OnCloseListener,
                                                                     ActionBarUtil.Delegate {
    private final ViewLogger mViewLogger = new ViewLogger();
    protected SearchView mSearchView;
    protected Menu mMenu;
    protected int mActionBarColor = Color.TRANSPARENT;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (menu.findItem(R.id.action_search) == null
            && !(this instanceof VaultFragment)
            && !(this instanceof CredentialAddStep1Fragment)
            && !(this instanceof PasswordGeneratorAndGeneratedPasswordFragment)
            && !(this instanceof AuthenticatorDashboardFragment)) {
            inflater.inflate(R.menu.main_menu, menu);
            MenuItem searchItem = menu.findItem(R.id.action_search);
            setupSearchView((SearchView) searchItem.getActionView());
            mMenu = menu;
        }
    }

    @Override
    public void onDestroyView() {
        if (mSearchView != null) {
            mSearchView.setOnSearchClickListener(null);
            mSearchView.setOnQueryTextListener(null);
            mSearchView.setOnCloseListener(null);
        }
        super.onDestroyView();
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        SingletonProvider.getComponent().getLockRepository()
                         .getLockManager(SingletonProvider.getSessionManager().getSession())
                         .setLastActionTimestampToNow();
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == ID_SEARCH_VIEW) {
            SingletonProvider.getNavigator().goToSearch(null);
        }
    }

    @Override
    public boolean onClose() {
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public ViewLogger getViewLogger() {
        return mViewLogger;
    }

    @Override
    public int getActionBarColor() {
        if (mActionBarColor == Color.TRANSPARENT) {
            Context context = getContext();
            if (context != null) {
                mActionBarColor = context.getColor(R.color.container_agnostic_neutral_standard);
            }
        }
        return mActionBarColor;
    }

    protected void setupSearchView(SearchView searchView) {
        mSearchView = searchView;
        if (mSearchView == null) { 
            return;
        }
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setId(ID_SEARCH_VIEW);
        mSearchView.setOnSearchClickListener(this);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);

        mSearchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        ColorUtilsKt.setSearchViewMagIconTint(mSearchView,
                ColorUtilsKt.getColorOnForToolbar(mSearchView.getContext(),
                        getActionBarColor()));
    }
}
