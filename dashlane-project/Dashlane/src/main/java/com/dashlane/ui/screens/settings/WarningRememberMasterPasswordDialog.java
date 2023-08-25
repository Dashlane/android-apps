package com.dashlane.ui.screens.settings;

import android.content.Context;

import com.dashlane.R;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.login.lock.LockTypeManager;
import com.dashlane.security.SecurityHelper;
import com.dashlane.session.Session;
import com.dashlane.useractivity.log.usage.UsageLogConstant;

import androidx.appcompat.app.AlertDialog;

public class WarningRememberMasterPasswordDialog {

    public void showIfNecessary(final Context context,
                                final @LockTypeManager.LockType int lockTypePinCode,
                                final ConfirmRememberMasterPasswordListener listener) {
        Session session = SingletonProvider.getSessionManager().getSession();
        if (session == null) {
            
            return;
        }

        boolean storedMP = SingletonProvider.getComponent().getSessionCredentialsSaver().areCredentialsSaved(session.getUsername());

        if (storedMP) {
            
            listener.onMasterPasswordRememberedIfPossible();
            return;
        }

        SecurityHelper securityHelper = SingletonProvider.getSecurityHelper();

        if (!securityHelper.isDeviceSecured()) {
            securityHelper.showPopupPinCodeDisable(context);
            return;
        }

        final String usageLog35Type;
        switch (lockTypePinCode) {
            case LockTypeManager.LOCK_TYPE_BIOMETRIC:
                usageLog35Type = UsageLogConstant.LockType.fingerPrint;
                break;
            case LockTypeManager.LOCK_TYPE_MASTER_PASSWORD:
                usageLog35Type = UsageLogConstant.LockType.master;
                break;
            case LockTypeManager.LOCK_TYPE_PIN_CODE:
                usageLog35Type = UsageLogConstant.LockType.pin;
                break;
            default:
                usageLog35Type = "unknown";
        }

        AlertDialog.Builder builder = SingletonProvider.getDialogHelper()
                                                       .builder(context);

        builder.setMessage(R.string.dialog_allow_remember_mp_description)
               .setCancelable(false)
               .setNegativeButton(R.string.dialog_allow_remember_mp_deny, (dialogInterface, i) -> {
                   getLogger().logUsageLog35(usageLog35Type, "deny_save_mpwd");

                   listener.onRememberMasterPasswordDeclined();
               })
               .setPositiveButton(R.string.dialog_allow_remember_mp_accept, (dialogInterface, i) -> {
                   getLogger().logUsageLog35(usageLog35Type, "accept_save_mpwd");
                   try {
                       SingletonProvider.getComponent().getSessionCredentialsSaver().saveCredentials(session);
                   } catch (Exception e) {
                   }
                   listener.onMasterPasswordRememberedIfPossible();
               });
        builder.show();
    }

    private WarningRememberMasterPasswordDialogLogger getLogger() {
        return new WarningRememberMasterPasswordDialogLogger(
                SingletonProvider.getSessionManager(),
                SingletonProvider.getComponent().getBySessionUsageLogRepository()
        );
    }

    public interface ConfirmRememberMasterPasswordListener {

        void onMasterPasswordRememberedIfPossible();

        void onRememberMasterPasswordDeclined();
    }
}
