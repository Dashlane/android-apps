package com.dashlane.ui.activities.debug

import android.content.Context
import android.widget.Toast
import androidx.preference.PreferenceGroup
import com.dashlane.authentication.accountsmanager.AccountsManager
import com.dashlane.util.Toaster
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

internal class DebugCategoryAccountsManager @Inject constructor(
    @ActivityContext override val context: Context,
    private val accountsManager: AccountsManager,
    private val toaster: Toaster
) : AbstractDebugCategory() {

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
