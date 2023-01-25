package com.dashlane.ui.widgets.view.chips;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.dashlane.R;
import com.dashlane.ui.drawable.CircleFirstLetterDrawable;
import com.dashlane.util.ContextUtilsKt;

import androidx.core.content.ContextCompat;

public class SharingContactChipsView extends FrameLayout {

    private FrameLayout mDelete;
    private TextView mName;
    private FrameLayout mWrapper;
    private int mBackgroundColor;

    public SharingContactChipsView(Context context) {
        super(context);
        inflateView();
    }

    public SharingContactChipsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflateView();
    }

    public SharingContactChipsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateView();
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected) {
            mDelete.setVisibility(View.VISIBLE);
            mName.setPadding(getResources().getDimensionPixelSize(R.dimen.spacing_small), 0, getResources()
                    .getDimensionPixelSize(R.dimen.spacing_huge), 0);

            updateBackgroundColor(
                    ContextUtilsKt.getThemeAttrColor(getContext(), android.R.attr.textColorTertiary));
        } else {
            mDelete.setVisibility(View.GONE);
            mName.setPadding(getResources().getDimensionPixelSize(R.dimen.spacing_small), 0, getResources()
                    .getDimensionPixelSize(R.dimen.spacing_small), 0);
            updateBackgroundColor(mBackgroundColor);
        }
    }

    private void inflateView() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.chips_text_sharing_contact_layout, this, true);
        mDelete = v.findViewById(R.id.delete);
        mName = v.findViewById(R.id.name);
        mWrapper = v.findViewById(R.id.chip_sharing_contact_wrapper);
    }

    public void setName(String name) {
        mBackgroundColor = ContextCompat.getColor(getContext(), CircleFirstLetterDrawable.getColor(name));
        updateBackgroundColor(mBackgroundColor);
        mName.setText(name);
    }

    private void updateBackgroundColor(int color) {
        Drawable newBackground = getContext().getResources().getDrawable(R.drawable.sharing_contact_bubble).mutate();
        newBackground.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        mWrapper.setBackground(newBackground);
    }
}
