package com.dashlane.ui.widgets.view.empty;

import android.content.Context;
import android.view.View;

import com.dashlane.ui.R;
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter;
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder;

import androidx.annotation.NonNull;

public class EmptyScreenViewProvider
        implements DashlaneRecyclerAdapter.MultiColumnViewTypeProvider {

    private static final DashlaneRecyclerAdapter.ViewType<? extends EmptyScreenViewProvider> VIEW_TYPE =
            new DashlaneRecyclerAdapter.ViewType<>(R.layout.empty_screen_generic,
                                                   EmptyScreenViewProvider.ItemViewHolder.class);

    private final EmptyScreenConfiguration mEmptyScreen;

    public EmptyScreenViewProvider(EmptyScreenConfiguration emptyScreenConfiguration) {
        mEmptyScreen = emptyScreenConfiguration;
    }

    @NonNull
    @Override
    public DashlaneRecyclerAdapter.ViewType getViewType() {
        return VIEW_TYPE;
    }

    @Override
    public int getSpanSize(int spanCount) {
        return spanCount;
    }

    public static class ItemViewHolder extends EfficientViewHolder<EmptyScreenViewProvider> {

        public ItemViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void updateView(Context context, EmptyScreenViewProvider object) {
            object.mEmptyScreen.configureWithView(getView());
        }
    }
}
