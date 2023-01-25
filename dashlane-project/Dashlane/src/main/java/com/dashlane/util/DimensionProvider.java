package com.dashlane.util;

import android.view.ViewGroup;

import com.dashlane.BuildConfig;



public class DimensionProvider {
    static final int WIDTH_INDEX = 0;
    static final int HEIGHT_INDEX = 1;
    private final int[][] mDimensions;

    public DimensionProvider(int[][] dimensions) {
        if (BuildConfig.DEBUG) {
            if (dimensions.length < 1) {
                throw new IllegalArgumentException("Need default dimension");
            }

            for (int i = 0; i < dimensions.length; i++) {
                int[] dim = dimensions[i];
                if (dim.length < 2) {
                    throw new IllegalArgumentException("Specified dimension must be at least 2D, i = " + i);
                }
            }
        }
        mDimensions = dimensions;
    }

    public int[] getSupportedDimen(ViewGroup.LayoutParams layoutParams) {
        int[] supportedDimen = mDimensions[0];
        if (layoutParams.width <= 0) { 
            return supportedDimen; 
        }
        for (int i = 1; i < mDimensions.length; i++) {
            int[] curr = mDimensions[i];
            if (layoutParams.width < curr[WIDTH_INDEX]) {
                supportedDimen = curr;
            }
        }

        return supportedDimen;
    }
}
