package com.dashlane.ui.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.LinearLayout;

import com.dashlane.R;
import com.dashlane.locking.animations.pincode.PinCodeDotsTranslations;

public class PinCodeView extends LinearLayout {

    private static final int PIN_CODE_LENGTH = 4;

    public PinCodeView(Context context) {
        super(context);
        init();
    }

    public PinCodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PinCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setUnderlinesInvisible() {
        for (int i = 0; i < getChildCount(); i++) {
            View underlineView = getUnderlineView(i);
            if (underlineView != null) {
                underlineView.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void setPinsVisible(int pinCount, int underlineFilledColor, int underLineFocusedColor, int underLineDefaultColor) {
        for (int i = 0; i < getChildCount(); i++) {
            View dotView = getDotView(i);
            View underlineView = getUnderlineView(i);
            boolean hasValue = i < pinCount;
            if (hasValue) {
                dotView.setVisibility(View.VISIBLE);
                underlineView.setBackgroundColor(underlineFilledColor);
            } else {
                dotView.setVisibility(View.INVISIBLE);
                if (i == pinCount) {
                    
                    underlineView.setBackgroundColor(underLineFocusedColor);
                } else {
                    underlineView.setBackgroundColor(underLineDefaultColor);
                }
            }
        }
    }

    public void setUnderlinesColor(int color) {
        for (int i = 0; i < getChildCount(); i++) {
            setUnderlineColor(i, color);
        }
    }

    public int startAnimationDots(ViewGroup rootView, boolean disableAnimationEffect) {
        View[] dots = new View[getChildCount()];
        for (int i = 0; i < dots.length; i++) {
            dots[i] = getDotView(i);
        }
        PinCodeDotsTranslations translations = new PinCodeDotsTranslations(dots, rootView, disableAnimationEffect);
        return translations.startAnimation();
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection inputConnection = super.onCreateInputConnection(outAttrs);
        outAttrs.inputType |= InputType.TYPE_CLASS_NUMBER;
        outAttrs.imeOptions |= EditorInfo.IME_FLAG_NO_EXTRACT_UI;
        return inputConnection;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthGiven = MeasureSpec.getSize(widthMeasureSpec);
        Resources res = getResources();
        int maxWidthNecessary = Math.round(
                (res.getDimension(R.dimen.fragment_lock_pincode_pin_area_item_width) +
                 res.getDimension(R.dimen.spacing_normal)) * getChildCount());

        if (widthGiven > maxWidthNecessary) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(maxWidthNecessary, MeasureSpec.getMode(widthMeasureSpec));
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void setUnderlineColor(int index, int color) {
        getUnderlineView(index).setBackgroundColor(color);
    }

    private View getUnderlineView(int index) {
        return getChildAt(index).findViewById(R.id.dot_underline);
    }

    private View getDotView(int index) {
        return getChildAt(index).findViewById(R.id.dot);
    }

    private void init() {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (int i = 0; i < PIN_CODE_LENGTH; i++) {
            inflater.inflate(R.layout.include_fragment_lock_pincode_area_item, this, true);
        }
    }


}
