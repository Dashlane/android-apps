package com.dashlane.ui.adapter;

import com.dashlane.ui.adapter.util.AdapterViewTypeProviderDiffCallback;
import com.skocken.efficientadapter.lib.adapter.EfficientRecyclerAdapter;
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder;

import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.DiffUtil;

public class DashlaneRecyclerAdapter<T extends DashlaneRecyclerAdapter.ViewTypeProvider>
        extends EfficientRecyclerAdapter<T> {

    private SparseArrayCompat<ViewType> mCachedViewType = new SparseArrayCompat<>();

    @Override
    public int getItemViewType(int position) {
        ViewTypeProvider o = get(position);
        ViewType viewType = o.getViewType();
        int hashCode = viewType.hashCode();
        mCachedViewType.put(hashCode, viewType);
        return hashCode;
    }

    @Override
    public Class<? extends EfficientViewHolder<? extends T>> getViewHolderClass(int viewType) {
        
        return getViewType(viewType).getViewHolderClass();
    }

    @Override
    public int getLayoutResId(int viewType) {
        return getViewType(viewType).getLayoutResId();
    }

    private ViewType getViewType(int viewType) {
        return mCachedViewType.get(viewType);
    }

    public interface ViewTypeProvider {
        @NonNull
        ViewType getViewType();
    }

    public interface MultiColumnViewTypeProvider extends ViewTypeProvider {
        int getSpanSize(int spanCount);
    }

    @Override
    public void add(T object) {
        if (object == null) {
            return;
        }
        super.add(object);
    }

    @Override
    public void add(int position, T object) {
        if (object == null) {
            return;
        }
        super.add(position, object);
    }

    public void populateItems(List<T> list) {
        DiffUtil.DiffResult diffResult =
                DiffUtil.calculateDiff(new AdapterViewTypeProviderDiffCallback(getObjects(), list));
        setNotifyOnChange(false);
        clear();
        addAll(list);
        setNotifyOnChange(true);
        diffResult.dispatchUpdatesTo(this);
    }

    public static class ViewType<V> {

        @LayoutRes
        final private int mLayoutResId;

        final private Class<? extends EfficientViewHolder<V>> mViewHolderClass;

        final private int mHashCode;

        public ViewType(@LayoutRes int layoutResId, Class<? extends EfficientViewHolder<V>> viewHolderClass) {
            mLayoutResId = layoutResId;
            mViewHolderClass = viewHolderClass;
            mHashCode = 31 * mLayoutResId + mViewHolderClass.hashCode();
        }

        public int getLayoutResId() {
            return mLayoutResId;
        }

        public Class<? extends EfficientViewHolder<V>> getViewHolderClass() {
            return mViewHolderClass;
        }

        @Override
        public int hashCode() {
            return mHashCode;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ViewType && hashCode() == o.hashCode();
        }
    }

}