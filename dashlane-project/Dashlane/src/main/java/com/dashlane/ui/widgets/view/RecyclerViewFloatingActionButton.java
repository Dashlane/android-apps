package com.dashlane.ui.widgets.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;

public class RecyclerViewFloatingActionButton extends FloatingActionButton {
    private static final int TRANSLATE_DURATION_MILLIS = 200;

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState == SCROLL_STATE_IDLE) {
                show();
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (dy > 0) {
                
                hide();
            } else {
                
                show();
            }
        }
    };

    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    private boolean mVisible = true;
    private boolean isPositionLocked = false;

    public RecyclerViewFloatingActionButton(Context context) {
        super(context);
    }

    public RecyclerViewFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewFloatingActionButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RecyclerView.OnScrollListener getOnScrollListener() {
        return mOnScrollListener;
    }

    public void lockPosition() {
        isPositionLocked = true;
    }

    public void unlockPosition() {
        isPositionLocked = false;
    }

    @Override
    public void show() {
        show(true);
    }

    @Override
    public void hide() {
        hide(true);
    }

    public void show(boolean animate) {
        if (isPositionLocked) {
            return;
        }
        toggle(true, animate, false);
    }

    public void hide(boolean animate) {
        if (isPositionLocked) {
            return;
        }
        toggle(false, animate, false);
    }

    private int getMarginBottom() {
        int marginBottom = 0;
        final ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
        }
        return marginBottom;
    }

    private void toggle(final boolean visible, final boolean animate, boolean force) {
        if (mVisible == visible && !force) {
            return;
        }
        mVisible = visible;
        int height = getHeight();
        if (height == 0 && !force) {
            ViewTreeObserver vto = getViewTreeObserver();
            if (vto.isAlive()) {
                vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        ViewTreeObserver currentVto = getViewTreeObserver();
                        if (currentVto.isAlive()) {
                            currentVto.removeOnPreDrawListener(this);
                        }
                        toggle(visible, animate, true);
                        return true;
                    }
                });
                return;
            }
        }
        int translationY = visible ? 0 : height + getMarginBottom();
        if (animate) {
            animate().setInterpolator(mInterpolator)
                     .setDuration(TRANSLATE_DURATION_MILLIS)
                     .translationY(translationY)
                     .setListener(null);
        } else {
            setTranslationY(translationY);
        }
    }
}