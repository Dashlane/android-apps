package com.dashlane.ui.widgets.view.empty;

import android.content.Context;
import android.content.res.Resources;

import androidx.appcompat.content.res.AppCompatResources;

import com.dashlane.R;

public class IDsEmptyScreen {

    private IDsEmptyScreen() {
        
    }

    public static EmptyScreenViewProvider newInstance(Context context, boolean alignTop) {
        Resources res = context.getResources();
        return new EmptyScreenViewProvider(
                new EmptyScreenConfiguration.Builder()
                        .setImage(AppCompatResources.getDrawable(context, R.drawable.ic_empty_id))
                        .setLine1(res.getString(R.string.empty_screen_ids_line1))
                        .setLine2(res.getString(R.string.empty_screen_ids_line2))
                        .setAlignTop(alignTop)
                        .build()
        );
    }
}
