package com.dashlane.ui.screens.settings

import android.content.Context
import com.dashlane.R
import com.dashlane.security.SecurityHelper
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.ui.util.DialogHelper
import javax.inject.Inject

class WarningRememberMasterPasswordDialog @Inject constructor(
    private val dialogHelper: DialogHelper,
    private val sessionManager: SessionManager,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    private val securityHelper: SecurityHelper,
) {

    fun showIfNecessary(
        context: Context,
        onMasterPasswordRememberedIfPossible: () -> Unit,
        onRememberMasterPasswordDeclined: () -> Unit
    ) {
        val session = sessionManager.session
            ?: return 

        val storedMP = sessionCredentialsSaver.areCredentialsSaved(session.username)

        if (storedMP) {
            
            onMasterPasswordRememberedIfPossible()
            return
        }

        if (!securityHelper.isDeviceSecured()) {
            securityHelper.showPopupPinCodeDisable(context)
            return
        }

        dialogHelper
            .builder(context)
            .setMessage(R.string.dialog_allow_remember_mp_description)
            .setCancelable(false)
            .setNegativeButton(R.string.dialog_allow_remember_mp_deny) { _, _ ->
                onRememberMasterPasswordDeclined()
            }
            .setPositiveButton(R.string.dialog_allow_remember_mp_accept) { _, _ ->
                try {
                    sessionCredentialsSaver.saveCredentials(session)
                } catch (e: Exception) {
                    warn("showIfNecessary exception", "", e)
                }
                onMasterPasswordRememberedIfPossible()
            }
            .show()
    }
}
