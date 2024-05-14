package com.dashlane.ui.widgets.view.empty;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.view.ViewCompat;

import com.dashlane.ui.R;
import com.dashlane.ui.widgets.view.Infobox;

public class EmptyScreenConfiguration {

    private final Drawable mImage;
    private final String mLine1;
    private final String mLine2;
    private final String mInfoBoxText;
    private boolean mAlignTop;
    private final CharSequence mButtonText;
    private final View.OnClickListener mButtonClickListener;

    EmptyScreenConfiguration(Drawable image, String line1, String line2, String infoBoxText, boolean alignTop,
                             CharSequence buttonText, View.OnClickListener buttonClickListener) {
        mImage = image;
        mLine1 = line1;
        mLine2 = line2;
        mInfoBoxText = infoBoxText;
        mAlignTop = alignTop;
        mButtonText = buttonText;
        mButtonClickListener = buttonClickListener;
    }

    public void configureWithView(View view) {
        ImageView emptyImageVIew = view.findViewById(R.id.empty_screen_img);
        if (mAlignTop) {
            view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        emptyImageVIew.setImageDrawable(mImage);

        TextView line1 = view.findViewById(R.id.empty_screen_line1);
        ViewCompat.setAccessibilityHeading(line1, true);
        line1.setText(mLine1);
        if (TextUtils.isEmpty(mLine1)) {
            line1.setVisibility(View.GONE);
        }

        ((TextView) view.findViewById(R.id.empty_screen_line2)).setText(mLine2);

        Infobox infobox = view.findViewById(R.id.empty_screen_infobox);
        infobox.setText(mInfoBoxText);
        if (TextUtils.isEmpty(mInfoBoxText)) {
            infobox.setVisibility(View.GONE);
        }

        Button button = view.findViewById(R.id.empty_screen_button);
        if (mButtonText != null) {
            button.setVisibility(View.VISIBLE);
            button.setText(mButtonText);
            button.setOnClickListener(mButtonClickListener);
        } else {
            button.setVisibility(View.GONE);
        }
    }

    public static class Builder {
        private Drawable mImage;
        private String mLine1;
        private String mLine2;
        private String mInfoBox;
        private boolean mAlignTop;
        private CharSequence mButtonText;
        private View.OnClickListener mButtonClickListener;

        public Builder setImage(Drawable image) {
            mImage = image;
            return this;
        }

        public Builder setLine1(String line1) {
            mLine1 = line1;
            return this;
        }

        public Builder setLine2(String line2) {
            mLine2 = line2;
            return this;
        }

        public void setInfoBox(String text) {
            mInfoBox = text;
        }

        public Builder setAlignTop(boolean alignTop) {
            mAlignTop = alignTop;
            return this;
        }

        public Builder setButton(CharSequence text, View.OnClickListener listener) {
            mButtonText = text;
            mButtonClickListener = listener;
            return this;
        }

        public EmptyScreenConfiguration build() {
            return new EmptyScreenConfiguration(mImage, mLine1, mLine2, mInfoBox, mAlignTop, mButtonText,
                    mButtonClickListener);
        }
    }

}
