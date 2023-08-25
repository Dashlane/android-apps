package com.dashlane.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public abstract class AbstractSpinnerAdapterWithDefaultValue<T> extends ArrayAdapter<T> implements SpinnerAdapter {

    private boolean isFirstTime;
    private T mFirstElement;
    private List<T> mData;
    private WeakReference<Spinner> mSpinnerRef;

    AbstractSpinnerAdapterWithDefaultValue(Context context, int resource, List<T> objects, T defaultText) {
        super(context, resource, objects);
        isFirstTime = true;
        mData = new ArrayList<>(objects);
        mFirstElement = defaultText;
        setDefaultElement(defaultText);
    }

    private void setDefaultElement(T defaultText) {
        if (!mData.isEmpty()) {
            mFirstElement = mData.get(0);
            mData.set(0, defaultText);
        }
    }

    public void setSpinner(Spinner s) {
        mSpinnerRef = new WeakReference<>(s);
    }


    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        if (isFirstTime) {
            mData.set(0, mFirstElement);
            isFirstTime = false;
            if (mSpinnerRef != null && mSpinnerRef.get() != null) {
                mSpinnerRef.get().setSelection(0, false);
            }
        }
        return getViewForDropdown(position, convertView, parent);
    }

    public void setPreselection(int position) {
        if (isFirstTime) {
            mData.set(0, mFirstElement);
            isFirstTime = false;
            if (mSpinnerRef != null && mSpinnerRef.get() != null) {
                mSpinnerRef.get().setSelection(position, false);
            }
        }
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        notifyDataSetChanged();
        return getPreviewView(position, convertView, parent);
    }

    protected List<T> getData() {
        return mData;
    }

    protected abstract View getPreviewView(int position, View convertView, ViewGroup parent);

    protected abstract View getViewForDropdown(int position, View convertView, ViewGroup parent);

}
