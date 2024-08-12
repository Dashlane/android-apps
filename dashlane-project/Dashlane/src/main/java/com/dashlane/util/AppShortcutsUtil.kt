package com.dashlane.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.dashlane.R
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.iconography.IconTokens
import com.dashlane.navigation.NavigationHelper.Destination.MainPath
import com.dashlane.navigation.NavigationUriBuilder
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppShortcutsUtil @Inject constructor(
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
) {
    fun refreshShortcuts(context: Context) = applicationCoroutineScope.launch {
        val shortcuts = ArrayList<ShortcutInfoCompat>()
        addLocalShortcuts(shortcuts, context)
        shortcuts.forEach {
            try {
                ShortcutManagerCompat.pushDynamicShortcut(context, it)
            } catch (exception: IllegalArgumentException) {
                
            } catch (exception: IllegalStateException) {
                
            }
        }
    }

    private fun addLocalShortcuts(shortcuts: ArrayList<ShortcutInfoCompat>, context: Context) {
        val searchShortcut = createShortcut(
            context,
            "searchShortcut",
            MainPath.SEARCH,
            context.getString(R.string.search),
            IconTokens.actionSearchOutlined,
            shortcuts.size
        )
        shortcuts.add(searchShortcut)

        val passwordGeneratorShortcut = createShortcut(
            context,
            "passwordGeneratorShortcut",
            MainPath.PASSWORD_GENERATOR,
            context.getString(R.string.action_bar_password_generator),
            IconTokens.featurePasswordGeneratorOutlined,
            shortcuts.size
        )
        shortcuts.add(passwordGeneratorShortcut)

        val sharingCenterShortcut = createShortcut(
            context,
            "sharingCenterShortcut",
            MainPath.SHARING_CENTER,
            context.getString(R.string.menu_v3_section_sharing_center),
            IconTokens.actionShareOutlined,
            shortcuts.size
        )
        shortcuts.add(sharingCenterShortcut)

        val passwordHealthShortcut = createShortcut(
            context,
            "passwordHealthShortcut",
            MainPath.PASSWORD_HEALTH,
            context.getString(R.string.menu_v3_section_security_dashboard),
            IconTokens.featurePasswordHealthOutlined,
            shortcuts.size
        )
        shortcuts.add(passwordHealthShortcut)
    }

    private fun createShortcut(
        context: Context,
        id: String,
        path: String,
        label: String,
        icon: IconToken,
        rank: Int
    ) = ShortcutInfoCompat.Builder(context, id)
        .setShortLabel(label)
        .setLongLabel(label)
        .setIcon(IconCompat.createWithBitmap(createIcon(context, icon)))
        .setRank(rank)
        .setIntent(
            createIntent(NavigationUriBuilder().appendPath(path).build())
        )
        .build()

    private fun createIcon(context: Context, icon: IconToken) =
        AppCompatResources.getDrawable(context, icon.resource)!!
            .apply { setTint(context.getColor(R.color.text_brand_standard)) }
            .toBitmap()

    private fun createIntent(uri: Uri) = Intent(
        Intent.ACTION_VIEW,
        uri
    ) 
        
        
        
        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
}
