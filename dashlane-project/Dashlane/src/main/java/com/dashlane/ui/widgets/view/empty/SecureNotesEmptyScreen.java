package com.dashlane.ui.widgets.view.empty;

import android.content.Context;
import android.content.res.Resources;

import androidx.appcompat.content.res.AppCompatResources;

import com.dashlane.R;

public class SecureNotesEmptyScreen {

    private SecureNotesEmptyScreen() {
        
    }

    public static EmptyScreenViewProvider newInstance(Context context, boolean alignTop) {
        Resources res = context.getResources();
        EmptyScreenConfiguration.Builder builder = new EmptyScreenConfiguration.Builder()
            .setImage(AppCompatResources.getDrawable(context, R.drawable.ic_empty_secure_note))
            .setLine1(res.getString(R.string.empty_screen_securenotes_line1))
            .setLine2(res.getString(R.string.empty_screen_securenotes_line2))
            .setAlignTop(alignTop);
        return new EmptyScreenViewProvider(builder.build());
    }
}
