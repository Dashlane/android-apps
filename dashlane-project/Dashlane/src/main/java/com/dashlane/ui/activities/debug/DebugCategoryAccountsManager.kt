package com.dashlane.ui.activities.debug

import android.app.Activity
import android.widget.Toast
import androidx.preference.PreferenceGroup
import com.dashlane.authentication.accountsmanager.AccountsManager
import com.dashlane.util.Toaster
import javax.inject.Inject

internal class DebugCategoryAccountsManager @Inject constructor(
    debugActivity: Activity,
    private val accountsManager: AccountsManager,
    private val toaster: Toaster
) : AbstractDebugCategory(debugActivity) {

    override val name: String
        get() = "Accounts Manager"

    override fun addSubItems(group: PreferenceGroup) {
        addPreferenceButton(group, "Clear", "Remove all accounts in AccountsManager") {
            accountsManager.clearAllAccounts()
            toaster.show("Accounts cleared", Toast.LENGTH_SHORT)
            true
        }
    }
}
