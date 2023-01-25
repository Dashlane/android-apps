package com.dashlane.ui.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.dashlane.ui.R;
import com.dashlane.ui.adapter.util.DiffUtilComparator;
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;



public class HeaderItem
        implements DashlaneRecyclerAdapter.MultiColumnViewTypeProvider,
        DiffUtilComparator<HeaderItem> {

    private static final DashlaneRecyclerAdapter.ViewType<? extends HeaderItem> VIEW_TYPE =
            new DashlaneRecyclerAdapter.ViewType<>(R.layout.item_header, HeaderItemViewHolder.class);

    private final String mLabel;

    public HeaderItem(String label) {
        mLabel = label;
    }

    @Override
    public int getSpanSize(int spanCount) {
        return spanCount;
    }

    public String getLabel() {
        return mLabel;
    }

    @NonNull
    @Override
    public DashlaneRecyclerAdapter.ViewType getViewType() {
        return VIEW_TYPE;
    }

    @Override
    public boolean isItemTheSame(HeaderItem item) {
        return Objects.equals(mLabel, item.mLabel);
    }

    @Override
    public boolean isContentTheSame(HeaderItem item) {
        return isItemTheSame(item);
    }

    public static class HeaderItemViewHolder extends EfficientViewHolder<HeaderItem> {

        public HeaderItemViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void updateView(@NonNull Context context, HeaderItem object) {
            TextView textView = (TextView) getView();
            textView.setText(object.mLabel);
            ViewCompat.setAccessibilityHeading(textView, true);
        }

        @Override
        public boolean isClickable() {
            return false;
        }
    }
}
