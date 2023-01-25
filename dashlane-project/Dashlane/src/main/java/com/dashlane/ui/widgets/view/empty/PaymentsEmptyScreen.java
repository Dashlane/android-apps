package com.dashlane.ui.widgets.view.empty;

import android.content.Context;
import android.content.res.Resources;

import com.dashlane.R;

import androidx.appcompat.content.res.AppCompatResources;

public class PaymentsEmptyScreen {

    private PaymentsEmptyScreen() {
        
    }

    public static EmptyScreenViewProvider newInstance(Context context, boolean alignTop) {
        Resources res = context.getResources();
        return new EmptyScreenViewProvider(
                new EmptyScreenConfiguration.Builder()
                        .setImage(AppCompatResources.getDrawable(context, R.drawable.ic_empty_payment))
                        .setLine1(res.getString(R.string.empty_screen_payments_line1))
                        .setLine2(res.getString(R.string.empty_screen_payments_line2))
                        .setAlignTop(alignTop)
                        .build()
        );
    }
}
