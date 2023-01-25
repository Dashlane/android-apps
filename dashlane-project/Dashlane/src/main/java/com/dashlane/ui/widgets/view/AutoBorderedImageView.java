package com.dashlane.ui.widgets.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.dashlane.R;
import com.dashlane.util.graphics.BitmapUtils;

import androidx.appcompat.widget.AppCompatImageView;



public class AutoBorderedImageView extends AppCompatImageView {

    private int mDefaultBackgroundRes;
    private int mBorderDrawableRes;
    private int[] mFilteredColors;

    private boolean hasBorderColor = false;
    private boolean shouldApplyDominantColorOnDefaultBackground = false;
    private int mBorderColor;


    public AutoBorderedImageView(Context context) {
        super(context);
    }

    public AutoBorderedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AutoBorderedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (isInEditMode()) {
            return;
        }
        Bitmap b = getBitmapFromDrawable(drawable);
        if (b != null) {
            setImageDrawableFromBitmap(b);
        }
    }

    private void setImageDrawableFromBitmap(Bitmap b) {
        int dominantColorFromBorder = BitmapUtils.getDominantColorFromBorder(b);
        setBackgroundResource(mDefaultBackgroundRes);
        boolean isBorderSet = false;
        for (int filteredColor : mFilteredColors) {
            if (dominantColorFromBorder == filteredColor) {
                setBackgroundResource(mBorderDrawableRes);
                isBorderSet = true;
                if (hasBorderColor) {
                    Drawable coloredBg = getBackground().mutate();
                    coloredBg.setColorFilter(mBorderColor, PorterDuff.Mode.MULTIPLY);
                    setBackgroundDrawable(coloredBg);
                }
                break;
            }
        }
        if (!isBorderSet && shouldApplyDominantColorOnDefaultBackground) {
            Drawable coloredBg = getBackground().mutate();
            coloredBg.setColorFilter(dominantColorFromBorder, PorterDuff.Mode.MULTIPLY);
            setBackgroundDrawable(coloredBg);

        }
        invalidate();
    }

    private void init(Context context, AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }
        TypedArray array = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.AutoBorderedImageView,
                0, 0);
        try {
            mDefaultBackgroundRes = array.getResourceId(R.styleable.AutoBorderedImageView_defaultBackground, 0);
            mBorderDrawableRes = array.getResourceId(R.styleable.AutoBorderedImageView_borderDrawable, 0);
            int colorArrayRes = array.getResourceId(R.styleable.AutoBorderedImageView_applyBorderOnColors, -1);
            if (colorArrayRes != -1) {
                TypedArray colorsFiltered = context.getResources().obtainTypedArray(colorArrayRes);
                mFilteredColors = new int[colorsFiltered.length()];
                for (int i = 0; i < colorsFiltered.length(); ++i) {
                    mFilteredColors[i] = colorsFiltered.getColor(i, 0);
                }
                colorsFiltered.recycle();
            }
            if (array.hasValue(R.styleable.AutoBorderedImageView_borderColor)) {
                hasBorderColor = true;
                mBorderColor = array.getColor(R.styleable.AutoBorderedImageView_borderColor, Color.TRANSPARENT);
            }
            shouldApplyDominantColorOnDefaultBackground = array.getBoolean(R.styleable
                                                                                   .AutoBorderedImageView_applyDominantColorOnDefaultBackground,
                                                                           false);
        } finally {
            array.recycle();
        }
    }

    private Bitmap getBitmapFromDrawable(Drawable d) {
        if (d instanceof BitmapDrawable) {
            return ((BitmapDrawable) d).getBitmap();
        }
        return null;
    }
}
