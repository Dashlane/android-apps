package com.dashlane.ui.adapter;

import android.content.Context;

import java.util.List;

public class SpinnerAdapterDefaultValueString extends SpinnerAdapterDefaultValue<String> {

    public SpinnerAdapterDefaultValueString(Context context, int resourceDropdown, int resourcePreview,
                                            List<String> objects, String defaultText) {
        super(context, resourceDropdown, resourcePreview, objects, defaultText);
    }
}
