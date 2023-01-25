package com.dashlane.util

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import com.dashlane.R
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.navigation.NavigationHelper
import com.dashlane.navigation.NavigationUriBuilder
import com.dashlane.navigation.SchemeUtils.getHost
import com.dashlane.ui.activities.fragments.list.wrapper.toItemWrapper
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.util.inject.qualifiers.GlobalCoroutineScope
import com.dashlane.vault.model.DataIdentifierId
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.desktopId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class AppShortcutsUtil @Inject constructor(
    @GlobalCoroutineScope
    private val globalCoroutineScope: CoroutineScope,
) {
    

    fun refreshShortcuts(context: Context) {
        globalCoroutineScope.launch {
            val shortcuts = ArrayList<ShortcutInfo>()
            val shortcutManager = context.getSystemService(
                ShortcutManager::class.java
            ) ?: return@launch 

            
            
            addLocalShortcuts(shortcuts, context)

            
            val genericDataQuery = SingletonProvider.getMainDataAccessor().getGenericDataQuery()
            val items: List<SummaryObject> = try {
                RecentItemsLoader(genericDataQuery).loadRecentItems(2, true)
            } catch (e: Exception) {
                
                return@launch
            }
            for (item in items) {
                val itemWrapper =
                    item.toItemWrapper(ItemListContext.Container.NONE.asListContext(ItemListContext.Section.NONE))
                        ?: continue 
                val label = itemWrapper.getTitle(context).text
                if (label.isSemanticallyNull()) {
                    
                    continue
                }
                val iconRes = getShortcutIcon(item)

                
                
                
                val longLabel = label + " (" + itemWrapper.getDescription(context).text.take(50_000) + ")"

                
                val cleanUid = with(item.id) { substring(1, length - 1) }

                val type = getHost(item.syncObjectType.desktopId)
                val shortcut = ShortcutInfo.Builder(context, item.id)
                    .setShortLabel(label)
                    .setLongLabel(longLabel)
                    .setIcon(Icon.createWithResource(context, iconRes))
                    .setRank(shortcuts.size)
                    .setIntent(
                        createIntent(
                            NavigationUriBuilder()
                                .appendPath(type)
                                .appendPath(cleanUid)
                                .origin(ORIGIN)
                                .build()
                        )
                    )
                    .build()
                shortcuts.add(shortcut)
            }
            try {
                shortcutManager.dynamicShortcuts = shortcuts
            } catch (exception: IllegalArgumentException) {
                
            }
        }
    }

    

    private fun addLocalShortcuts(shortcuts: ArrayList<ShortcutInfo>, context: Context) {
        var label = context.getString(R.string.search)
        val searchShortcut = ShortcutInfo.Builder(context, "searchShortcut")
            .setShortLabel(label)
            .setLongLabel(label)
            .setIcon(Icon.createWithResource(context, R.drawable.ic_search_appshortcut))
            .setRank(shortcuts.size)
            .setIntent(
                createIntent(
                    NavigationUriBuilder()
                        .appendPath(NavigationHelper.Destination.MainPath.SEARCH)
                        .origin(ORIGIN)
                        .build()
                )
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            .build()
        shortcuts.add(searchShortcut)
        label = context.getString(R.string.empty_screen_authentifiants_button)
        val addPwdShortcut = ShortcutInfo.Builder(context, "addPwdShortcut")
            .setShortLabel(label)
            .setLongLabel(label)
            .setIcon(Icon.createWithResource(context, R.drawable.ic_add_appshortcut))
            .setRank(shortcuts.size)
            .setIntent(
                createIntent(
                    NavigationUriBuilder()
                        .appendPath(NavigationHelper.Destination.MainPath.PASSWORDS)
                        .appendPath(NavigationHelper.Destination.SecondaryPath.Items.NEW)
                        .origin(ORIGIN)
                        .build()
                )
            )
            .build()
        shortcuts.add(addPwdShortcut)
    }

    private fun createIntent(uri: Uri): Intent {
        return Intent(
            Intent.ACTION_VIEW,
            uri
        ) 
            
            
            
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }

    

    private fun getShortcutIcon(item: SummaryObject): Int {
        
        val desktopId = item.syncObjectType.desktopId
        return when (desktopId) {
            DataIdentifierId.AUTH_CATEGORY, DataIdentifierId.AUTHENTIFIANT -> R.drawable.ic_passwords_appshortcut
            DataIdentifierId.COMPANY, DataIdentifierId.DRIVER_LICENCE, DataIdentifierId.EMAIL, DataIdentifierId.FISCAL_STATEMENT, DataIdentifierId.ID_CARD, DataIdentifierId.IDENTITY, DataIdentifierId.PASSPORT, DataIdentifierId.SOCIAL_SECURITY_STATEMENT -> R.drawable.ic_ids_appshortcut
            DataIdentifierId.ADDRESS, DataIdentifierId.GENERATED_PASSWORD, DataIdentifierId.MERCHANT, DataIdentifierId.PERSONAL_DATA_DEFAULT, DataIdentifierId.PERSONAL_WEBSITE, DataIdentifierId.PHONE, DataIdentifierId.PURCHASE_ARTICLE, DataIdentifierId.PURCHASE_BASKET, DataIdentifierId.PURCHASE_CATEGORY, DataIdentifierId.PURCHASE_CONFIRMATION, DataIdentifierId.PURCHASE_PAID_BASKET, DataIdentifierId.REACTIVATION_OBJECT -> R.drawable.ic_personal_info_appshortcut
            DataIdentifierId.SECURE_NOTE, DataIdentifierId.SECURE_NOTE_CATEGORY -> R.drawable.ic_secure_notes_appshortcut
            DataIdentifierId.BANK_STATEMENT, DataIdentifierId.PAYMENT_PAYPAL, DataIdentifierId.PAYMENT_CREDIT_CARD -> R.drawable.ic_payments_appshortcut
            else -> R.mipmap.ic_launcher
        }
    }

    companion object {
        private const val ORIGIN = "appShortcut"
    }
}