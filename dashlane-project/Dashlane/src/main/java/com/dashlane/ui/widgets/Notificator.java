package com.dashlane.ui.widgets;

import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.dashlane.R;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment;
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment.TwoButtonClicker;
import com.dashlane.useractivity.log.usage.UsageLog;
import com.dashlane.useractivity.log.usage.UsageLogRepository;



public class Notificator {

    public static void customErrorDialogMessage(final FragmentActivity activity, String topic, String message,
                                                final boolean shouldCloseCaller) {
        customErrorDialogMessage(activity, topic, message, shouldCloseCaller, null, null);
    }

    public static void customErrorDialogMessage(final FragmentActivity activity, String topic, String message,
                                                final boolean shouldCloseCaller,
                                                @Nullable UsageLogRepository usageLogRepository,
                                                @Nullable UsageLog positiveButtonUsageLog) {
        
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
                            if (positiveButtonUsageLog != null && usageLogRepository != null) {
                                usageLogRepository.enqueue(positiveButtonUsageLog, false);
                            }
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
