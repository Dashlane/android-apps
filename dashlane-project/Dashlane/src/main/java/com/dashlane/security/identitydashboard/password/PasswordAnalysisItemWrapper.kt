package com.dashlane.security.identitydashboard.password

import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.dashlane.R
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.ui.activities.fragments.list.action.ListItemAction
import com.dashlane.ui.activities.fragments.list.wrapper.DefaultVaultItemWrapper
import com.dashlane.ui.activities.fragments.list.wrapper.VaultItemDoubleWrapper
import com.dashlane.useractivity.log.usage.UsageLogCode125
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject

class PasswordAnalysisItemWrapper(
    mode: PasswordAnalysisContract.Mode,
    authentifiantWrapper: DefaultVaultItemWrapper<SummaryObject.Authentifiant>,
    private val actionListener: ActionListener
) : VaultItemDoubleWrapper<SummaryObject.Authentifiant>(authentifiantWrapper) {

    private val listItemAction = object : ListItemAction {

        override val icon: Int = R.drawable.ic_item_action_more

        override val contentDescription: Int = R.string.and_accessibility_vault_item_menu

        override val visibility: Int = View.VISIBLE

        @Suppress("UNCHECKED_CAST")
        override fun onClickItemAction(v: View, item: SummaryObject) {
            if (item !is SummaryObject.Authentifiant) return
            val vaultItem = SingletonProvider.getMainDataAccessor().getVaultDataQuery()
                .query(
                    vaultFilter {
                    specificUid(item.id)
                }
                ) as? VaultItem<SyncObject.Authentifiant> ?: return

            PopupMenu(v.context, v).apply {
                val (menuItemInclude, menuItemExclude) =
                    if (mode == PasswordAnalysisContract.Mode.EXCLUDED) {
                        menu.add(R.string.security_dashboard_option_include) to null
                    } else {
                        null to menu.add(R.string.security_dashboard_option_exclude)
                    }
                val menuItemGoWebsite = menu.add(R.string.security_dashboard_option_go_to_website)
                setOnMenuItemClickListener {
                    when (it) {
                        menuItemInclude -> actionListener.saveModified(
                            vaultItem.copySyncObject { checked = false },
                            UsageLogCode125.Action.REINTRODUCE
                        )
                        menuItemExclude -> actionListener.saveModified(
                            vaultItem.copySyncObject { checked = true },
                            UsageLogCode125.Action.EXCLUDE
                        )
                        menuItemGoWebsite -> actionListener.goToWebsite(item)
                    }
                    true
                }
                show()
            }
        }
    }

    override fun getListItemActions(): List<ListItemAction> {
        return listOf(listItemAction)
    }

    interface ActionListener {
        fun saveModified(authentifiant: VaultItem<SyncObject.Authentifiant>, reason: UsageLogCode125.Action)
        fun open(authentifiant: SummaryObject.Authentifiant)
        fun goToWebsite(authentifiant: SummaryObject.Authentifiant)
    }
}