package com.dashlane.ui.widgets;

import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.dashlane.R;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment;
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment.TwoButtonClicker;

public class Notificator {

    public static void customErrorDialogMessage(final FragmentActivity activity, String topic, String message,
                                                final boolean shouldCloseCaller) {
        
        if (activity != null && !activity.isFinishing()) {
            NotificationDialogFragment dialog = new NotificationDialogFragment.Builder()
                    .setTitle(topic)
                    .setMessage(message)
                    .setNegativeButtonText(null)
                    .setPositiveButtonText(activity.getString(R.string.ok))
                    .setCancelable(false)
                    .setClicker(new TwoButtonClicker() {

                        @Override
                        public void onNegativeButton() {
                            if (shouldCloseCaller) {
                                activity.finish();
                            }
                        }

                        @Override
                        public void onPositiveButton() {
                            if (shouldCloseCaller) {
                                activity.finish();
                            }
                        }
                    }).build();
            dialog.show(activity.getSupportFragmentManager(), "customErrorMessageDialog");
        } else {
            
            SingletonProvider.getToaster().show(message, Toast.LENGTH_LONG);
        }
    }
}
