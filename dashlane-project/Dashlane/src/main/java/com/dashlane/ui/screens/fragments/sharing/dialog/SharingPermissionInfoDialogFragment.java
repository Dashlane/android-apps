package com.dashlane.ui.screens.fragments.sharing.dialog;

import android.content.Context;

import com.dashlane.R;
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment;



public class SharingPermissionInfoDialogFragment {

    public static final String TAG = SharingPermissionInfoDialogFragment.class.getName();

    private SharingPermissionInfoDialogFragment() {
        
    }

    public static NotificationDialogFragment newInstance(Context context) {
        return new NotificationDialogFragment.Builder()
                .setTitle(context, R.string.sharing_permission_explanation_title)
                .setPositiveButtonText(context, R.string.ok)
                .setCustomView(R.layout.fragment_dialog_sharing_permissions_info)
                .build();
    }

}
