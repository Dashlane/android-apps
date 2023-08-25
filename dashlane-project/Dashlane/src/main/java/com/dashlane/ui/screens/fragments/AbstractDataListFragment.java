package com.dashlane.ui.screens.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dashlane.R;
import com.dashlane.core.DataSync;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.events.SyncFinishedEvent;
import com.dashlane.hermes.generated.definitions.Trigger;
import com.dashlane.iconcrawler.IconWrapperUtils;
import com.dashlane.loaders.datalists.ListLoader;
import com.dashlane.login.lock.LockManager;
import com.dashlane.session.Session;
import com.dashlane.teamspaces.manager.TeamspaceManager;
import com.dashlane.ui.activities.fragments.AbstractContentFragment;
import com.dashlane.ui.activities.fragments.vault.provider.HeaderProvider;
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter;
import com.dashlane.ui.adapter.HeaderItem;
import com.dashlane.ui.fab.FabViewUtil;
import com.dashlane.ui.widgets.view.MultiColumnRecyclerView;
import com.dashlane.ui.widgets.view.RecyclerViewFloatingActionButton;
import com.dashlane.url.icon.UrlDomainIconAndroidRepository;
import com.dashlane.util.ContextUtilsKt;
import com.dashlane.util.DeviceUtils;
import com.dashlane.vault.model.VaultItem;
import com.skocken.efficientadapter.lib.adapter.EfficientAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public abstract class AbstractDataListFragment extends AbstractContentFragment implements
                                                                               LoaderManager.LoaderCallbacks<Cursor>,
                                                                               SwipeRefreshLayout.OnRefreshListener,
                                                                               EfficientAdapter.OnItemClickListener {

    private static final String SAVED_STATED_GRID_POSITION = "saved_stated_grid_position";
    private static final String SAVED_STATED_FAB_MENU_OPENED = "saved_stated_fab_menu_opened";

    protected static final int NO_LOADER_ID = 0;

    @Inject
    UrlDomainIconAndroidRepository iconAndroidRepository;

    private DashlaneRecyclerAdapter.ViewTypeProvider mEmptyViewProvider;
    protected MultiColumnRecyclerView mDataGridView;
    protected RecyclerViewFloatingActionButton mFloatingButton;
    private ProgressBar mProgressBar;
    protected SwipeRefreshLayout mRefreshLayout;
    protected FrameLayout mFabMenuHolder;

    protected boolean isDataRequestedBySyncOrItemChanged = false;
    private int mFirstVisiblePosition = -1;
    private boolean shouldRestoreShowFABMenu = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutResId(), container, false);
        mDataGridView = view.findViewById(R.id.recyclerview);
        mFloatingButton = view.findViewById(R.id.data_list_floating_button);
        mFloatingButton.setOnClickListener(v -> onClickFloatingButton());
        mProgressBar = view.findViewById(R.id.data_list_loading);
        mRefreshLayout = view.findViewById(R.id.refreshable_layout);
        mFabMenuHolder = view.findViewById(R.id.fab_menu_holder);
        mFabMenuHolder.setOnClickListener(v -> FabViewUtil.INSTANCE.hideFabMenu(mFabMenuHolder, mFloatingButton, true));

        parseSavedState(savedInstanceState);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeColors(container.getContext().getColor(R.color.text_brand_standard));
        mRefreshLayout.setProgressBackgroundColorSchemeColor(
                ContextUtilsKt.getThemeAttrColor(getContext(), R.attr.colorSurface));

        mDataGridView.getAdapter().setOnItemClickListener(this);
        mDataGridView.addOnScrollListener(mFloatingButton.getOnScrollListener());

        return view;
    }

    private int getLayoutResId() {
        return R.layout.fragment_data_list;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (shouldRestoreShowFABMenu) {
            FabViewUtil.INSTANCE.showFabMenu(mFabMenuHolder, mFloatingButton, true);
        } else {
            FabViewUtil.INSTANCE.hideFabMenu(mFabMenuHolder, mFloatingButton, false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        SingletonProvider.getAppEvents().register(this, SyncFinishedEvent.class, false, syncFinishedEvent -> {
            onSyncFinished();
            return null;
        });
        DeviceUtils.hideKeyboard(mDataGridView);
    }

    @Override
    public void onResume() {
        super.onResume();
        Session session = SingletonProvider.getSessionManager().getSession();
        if (session == null) return;
        LockManager lockManager = SingletonProvider.getComponent().getLockRepository()
                                                   .getLockManager(session);
        if (!lockManager.isLocked()) {
            refreshUi();
            return;
        }
        
        lockManager.waitUnlock(getActivity(), event -> refreshUi());
    }

    @Override
    public void onStop() {
        super.onStop();
        SingletonProvider.getAppEvents().unregister(this, SyncFinishedEvent.class);
    }

    @Override
    public abstract ListLoader onCreateLoader(int i, Bundle bundle);

    @Override
    public final void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isActivityReady() || !(cursorLoader instanceof ListLoader)) {
            return;
        }
        onLoadFinished((ListLoader) cursorLoader);
    }

    protected void onLoadFinished(ListLoader listLoader) {
        List<? extends DashlaneRecyclerAdapter.ViewTypeProvider> items = listLoader.getItems();
        onLoadFinished(items);
    }

    public void onLoadFinished(List<? extends DashlaneRecyclerAdapter.ViewTypeProvider> items) {
        reloadList(items);
        showData();
        isDataRequestedBySyncOrItemChanged = false;
        iconAndroidRepository.get(IconWrapperUtils.mapIconWrappersToUrlDomainIcons(items));
    }

    private boolean isActivityReady() {
        FragmentActivity activity = getActivity();
        return activity != null && !activity.isChangingConfigurations();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        reloadList(null);
    }

    protected void storeFirstVisiblePosition() {
        if (mDataGridView == null) {
            return;
        }
        RecyclerView.LayoutManager layoutManager = mDataGridView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            mFirstVisiblePosition = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
        } else if (layoutManager instanceof LinearLayoutManager) {
            mFirstVisiblePosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        } else {
            mFirstVisiblePosition = -1;
        }
    }

    private void refreshUi() {
        showLoader();
        restartLoader();
    }

    private void reloadList(List<? extends DashlaneRecyclerAdapter.ViewTypeProvider> items) {
        if (mDataGridView == null) {
            return;
        }
        DashlaneRecyclerAdapter adapter = mDataGridView.getAdapter();

        if (items != null && !items.isEmpty()) {
            String lastHeader = null;
            List<DashlaneRecyclerAdapter.ViewTypeProvider> list = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                DashlaneRecyclerAdapter.ViewTypeProvider item = items.get(i);
                lastHeader = addHeaderIfNeeded(list, lastHeader, item);
                list.add(item);
            }
            adapter.populateItems(list);

        } else if (mEmptyViewProvider != null) {
            adapter.clear();
            adapter.add(mEmptyViewProvider);
        }
    }

    protected HeaderProvider getHeaderProvider() {
        return null;
    }

    private String addHeaderIfNeeded(List listElements, String lastHeader,
            DashlaneRecyclerAdapter.ViewTypeProvider item) {
        HeaderProvider headerProvider = getHeaderProvider();
        if (headerProvider != null) {
            String newHeader = headerProvider.getHeaderFor(getContext(), item);
            if (newHeader != null && (lastHeader == null || !lastHeader.equals(newHeader))) {
                listElements.add(new HeaderItem(newHeader));
                lastHeader = newHeader;
            }
        }
        return lastHeader;
    }

    public abstract void onClickFloatingButton();

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mDataGridView != null) {
            GridLayoutManager layoutManager = (GridLayoutManager) mDataGridView.getLayoutManager();
            int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
            DashlaneRecyclerAdapter adapter = mDataGridView.getAdapter();
            if (firstVisiblePosition >= 0 && adapter != null && adapter.size() > firstVisiblePosition) {
                Object o = adapter.get(firstVisiblePosition);
                if (o instanceof VaultItem) {
                    outState.putLong(SAVED_STATED_GRID_POSITION, ((VaultItem) o).getId());
                }
            }
        }
        if (mFabMenuHolder != null) {
            outState.putBoolean(SAVED_STATED_FAB_MENU_OPENED, mFabMenuHolder.getVisibility() == View.VISIBLE);
        }
    }

    @Override
    public void onRefresh() {
        SingletonProvider.getDataSync().sync(Trigger.MANUAL);
    }

    private void onSyncFinished() {
        if (getActivity() != null && isAdded()) {
            mRefreshLayout.setRefreshing(false);
            isDataRequestedBySyncOrItemChanged = true;
            restartLoader();
        }
    }

    public void showLoader() {
        if (!isDataRequestedBySyncOrItemChanged && mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            mDataGridView.setVisibility(View.GONE);
            mFloatingButton.hide(false);
        }
    }

    protected void showData() {
        if (getActivity() == null || getActivity().isChangingConfigurations() || !isAdded()) {
            return;
        }
        if (mRefreshLayout != null && mRefreshLayout.isRefreshing()) {
            mRefreshLayout.setRefreshing(false);
        }
        mProgressBar.setVisibility(View.GONE);
        if (mDataGridView.getAdapter().getItemCount() > 0) {
            mDataGridView.setVisibility(View.VISIBLE);
        } else {
            mDataGridView.setVisibility(View.GONE);
        }
        mFloatingButton.show(!isDataRequestedBySyncOrItemChanged);
        if (!isDataRequestedBySyncOrItemChanged && mFirstVisiblePosition > 0) {
            mDataGridView.scrollToPosition(mFirstVisiblePosition);
        }
    }


    @VisibleForTesting
    protected TeamspaceManager getTeamspaceManager() {
        Session session = SingletonProvider.getSessionManager().getSession();
        return SingletonProvider.getComponent().getTeamspaceRepository().getTeamspaceManager(session);
    }

    public void setEmptyViewProvider(DashlaneRecyclerAdapter.ViewTypeProvider emptyViewProvider) {
        mEmptyViewProvider = emptyViewProvider;
    }

    protected void restartLoader() {
        int loaderId = getLoaderId();
        if (loaderId != NO_LOADER_ID) {
            getLoaderManager().restartLoader(loaderId, null, this);
        }
    }

    protected abstract int getLoaderId();

    protected void parseSavedState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mFirstVisiblePosition = savedInstanceState.getInt(SAVED_STATED_GRID_POSITION, mFirstVisiblePosition);
            shouldRestoreShowFABMenu = savedInstanceState.getBoolean(SAVED_STATED_FAB_MENU_OPENED, false);
        } else {
            shouldRestoreShowFABMenu = false;
        }
    }
}
