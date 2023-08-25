package com.dashlane.ui.util;

import com.dashlane.ui.adapter.DashlaneRecyclerAdapter;
import com.dashlane.ui.screens.fragments.AbstractDataListFragment;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.Nullable;

public abstract class DataLoaderForDataListFragment
        extends DataLoaderForUi<List<DashlaneRecyclerAdapter.ViewTypeProvider>> {

    private final AbstractDataListFragment mListFragment;

    public DataLoaderForDataListFragment(@NotNull AbstractDataListFragment listFragment) {
        super(listFragment);
        mListFragment = listFragment;
    }

    @Override
    protected void onPreExecute() {
        mListFragment.showLoader();
    }

    @Override
    protected void onPostExecute(@Nullable List<DashlaneRecyclerAdapter.ViewTypeProvider> items) {
        mListFragment.onLoadFinished(items);
    }
}
