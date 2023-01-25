package com.dashlane.ui.menu;




import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;





public class ScrimInsetsRecyclerView extends RecyclerView {

    

    private static final int FOREGROUND_COLOR = Color.argb(68, 0, 0, 0); 

    private Drawable mInsetForeground;

    private Rect mInsets;

    private Rect mTempRect = new Rect();

    public ScrimInsetsRecyclerView(Context context) {
        this(context, null);
    }

    public ScrimInsetsRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrimInsetsRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mInsetForeground = new ColorDrawable(FOREGROUND_COLOR);
        setWillNotDraw(true); 

        ViewCompat.setOnApplyWindowInsetsListener(this,
                                                  (v, insets) -> {
                                                      if (null == mInsets) {
                                                          mInsets = new Rect();
                                                      }
                                                      mInsets.set(insets.getSystemWindowInsetLeft(),
                                                                  insets.getSystemWindowInsetTop(),
                                                                  insets.getSystemWindowInsetRight(),
                                                                  insets.getSystemWindowInsetBottom());
                                                      setWillNotDraw(mInsets.isEmpty() || mInsetForeground == null);
                                                      ViewCompat.postInvalidateOnAnimation(
                                                              ScrimInsetsRecyclerView.this);
                                                      return insets;
                                                  });
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (mInsets != null && mInsetForeground != null) {
            int sc = canvas.save();
            canvas.translate(getScrollX(), getScrollY());

            
            mTempRect.set(0, 0, width, mInsets.top);
            mInsetForeground.setBounds(mTempRect);
            mInsetForeground.draw(canvas);

            
            mTempRect.set(0, height - mInsets.bottom, width, height);
            mInsetForeground.setBounds(mTempRect);
            mInsetForeground.draw(canvas);

            
            mTempRect.set(0, mInsets.top, mInsets.left, height - mInsets.bottom);
            mInsetForeground.setBounds(mTempRect);
            mInsetForeground.draw(canvas);

            
            mTempRect.set(width - mInsets.right, mInsets.top, width, height - mInsets.bottom);
            mInsetForeground.setBounds(mTempRect);
            mInsetForeground.draw(canvas);

            canvas.restoreToCount(sc);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mInsetForeground != null) {
            mInsetForeground.setCallback(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mInsetForeground != null) {
            mInsetForeground.setCallback(null);
        }
    }
}
