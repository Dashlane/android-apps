package com.dashlane.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dashlane.R;

import java.util.List;

import androidx.annotation.NonNull;

public class SpinnerAdapterDefaultValue<T> extends AbstractSpinnerAdapterWithDefaultValue<T> {

    private int mDropdownLayoutRes;
    private int mPreviewLayoutRes;

    public SpinnerAdapterDefaultValue(Context context, int resourceDropdown, int resourcePreview, List<T> objects, T
            defaultValue) {
        super(context, resourceDropdown, objects, defaultValue);
        mDropdownLayoutRes = resourceDropdown;
        mPreviewLayoutRes = resourcePreview;
    }

    @Override
    protected View getViewForDropdown(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent, mDropdownLayoutRes);
    }

    @Override
    protected View getPreviewView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent, mPreviewLayoutRes);
    }

    protected void setText(TextView text, T value) {
        text.setText(value == null ? null : value.toString());
    }

    @NonNull
    private View getView(int position, View convertView, ViewGroup parent, int layoutResId) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (convertView == null) {
            convertView = inflater.inflate(layoutResId, parent, false);
        }
        TextView text = convertView.findViewById(R.id.item_value);
        T value = getData().get(position);
        setText(text, value);
        return convertView;
    }
}
