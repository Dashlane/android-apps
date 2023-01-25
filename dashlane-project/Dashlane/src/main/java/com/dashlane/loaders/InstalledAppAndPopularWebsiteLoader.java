package com.dashlane.loaders;

import android.content.Context;

import com.dashlane.ui.util.DataLoaderForUi;
import com.dashlane.util.domain.PopularWebsiteUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;

public class InstalledAppAndPopularWebsiteLoader extends DataLoaderForUi<List<String>> {

    public interface Listener {
        void onLoadFinished(@Nullable List<String> result);
    }

    private final Fragment mFragment;
    private final Listener mListener;

    public InstalledAppAndPopularWebsiteLoader(@NotNull Fragment fragment, Listener listener) {
        super(fragment);
        mFragment = fragment;
        mListener = listener;
    }

    @Override
    protected List<String> loadData() {
        Context context = mFragment.getContext();
        if (context == null) return new ArrayList<>();
        return PopularWebsiteUtils.getPopularWebsites(context);
    }

    @Override
    protected void onPostExecute(@Nullable List<String> result) {
        mListener.onLoadFinished(result);
    }
}
