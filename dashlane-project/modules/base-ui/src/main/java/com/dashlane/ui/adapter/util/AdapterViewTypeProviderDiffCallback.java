package com.dashlane.ui.adapter.util;

import com.dashlane.ui.adapter.DashlaneRecyclerAdapter;

import java.util.List;

import androidx.recyclerview.widget.DiffUtil;


public class AdapterViewTypeProviderDiffCallback<T extends DashlaneRecyclerAdapter.ViewTypeProvider>
        extends DiffUtil.Callback {
    private final List<T> mOldList;
    private final List<T> mNewList;

    public AdapterViewTypeProviderDiffCallback(List<T> oldList, List<T> newList) {
        mOldList = oldList;
        mNewList = newList;
    }

    @Override
    public int getOldListSize() {
        return mOldList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        DashlaneRecyclerAdapter.ViewTypeProvider oldItem = mOldList.get(oldItemPosition);
        DashlaneRecyclerAdapter.ViewTypeProvider newItem = mNewList.get(newItemPosition);
        return isEquals(oldItem, newItem);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        DashlaneRecyclerAdapter.ViewTypeProvider oldItem = mOldList.get(oldItemPosition);
        DashlaneRecyclerAdapter.ViewTypeProvider newItem = mNewList.get(newItemPosition);
        if (!oldItem.getClass().equals(newItem.getClass())) {
            return false;
        }
        if (oldItem instanceof DiffUtilComparator) {
            try {
                return ((DiffUtilComparator) oldItem).isContentTheSame(newItem);
            } catch (ClassCastException e) {
                return false;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean isEquals(DashlaneRecyclerAdapter.ViewTypeProvider o1, DashlaneRecyclerAdapter.ViewTypeProvider o2) {
        if (o1 == null || o2 == null) {
            return false;
        }
        if (o1.equals(o2)) {
            return true;
        }
        if (!o1.getClass().equals(o2.getClass())) {
            return false;
        }
        if (o1 instanceof DiffUtilComparator) {
            try {
                return ((DiffUtilComparator) o1).isItemTheSame(o2);
            } catch (ClassCastException e) {
                return false;
            }
        }
        return false;
    }
}
