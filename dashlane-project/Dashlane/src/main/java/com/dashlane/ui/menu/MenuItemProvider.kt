package com.dashlane.ui.menu

import androidx.annotation.StringRes
import com.dashlane.R
import com.dashlane.navigation.Navigator
import com.dashlane.ui.menu.footer.MenuLockFooterItem
import com.dashlane.ui.menu.item.CapabilityMenuItemPremiumTagProvider
import com.dashlane.ui.menu.item.MenuItem
import com.dashlane.ui.menu.item.MenuItemEndIconProvider
import com.dashlane.ui.menu.item.NotificationCenterMenuItemEndIconProvider
import com.dashlane.ui.menu.item.VpnMenuItemPremiumTagProvider
import com.dashlane.ui.menu.separator.MenuSectionHeaderItem
import com.dashlane.ui.menu.separator.MenuSeparatorViewHolder
import com.dashlane.util.userfeatures.UserFeaturesChecker.Capability.DATA_LEAK
import java.util.Collections



class MenuItemProvider(private val navigator: Navigator) {
    private val menuItemPasswordHealth = MenuItem(
        R.drawable.ic_drawer_password_health,
        R.drawable.ic_drawer_password_health_selected,
        R.string.menu_v3_section_security_dashboard,
        arrayOf(R.id.nav_identity_dashboard, R.id.nav_password_analysis)
    ) {
        navigator.goToIdentityDashboard(ORIGIN_MENU)
    }
    val menuItemVpn = MenuItem(
        R.drawable.ic_drawer_vpn,
        R.drawable.ic_drawer_vpn_selected,
        R.string.menu_vpn,
        arrayOf(R.id.nav_vpn_third_party),
        MenuItemEndIconProvider.None,
        VpnMenuItemPremiumTagProvider()
    ) {
        navigator.goToVpn(ORIGIN_MENU)
    }
    private val menuItemActionCenter = MenuItem(
        R.drawable.ic_drawer_notification,
        R.drawable.ic_drawer_notification_selected,
        R.string.talk_to_me_menu_entry_title,
        arrayOf(R.id.nav_notif_center),
        NotificationCenterMenuItemEndIconProvider()
    ) {
        navigator.goToActionCenter(ORIGIN_MENU)
    }
    private val menuItemDarkWebMonitoring = MenuItem(
        R.drawable.ic_drawer_dark_web,
        R.drawable.ic_drawer_dark_web_selected,
        R.string.menu_v3_section_dark_web_monitoring,
        arrayOf(R.id.nav_dark_web_monitoring),
        MenuItemEndIconProvider.None,
        CapabilityMenuItemPremiumTagProvider(DATA_LEAK)
    ) {
        navigator.goToDarkWebMonitoring(ORIGIN_MENU)
    }
    val menuItemPersonalPlan = MenuItem(
        R.drawable.ic_drawer_getting_started,
        R.drawable.ic_drawer_getting_started_selected,
        R.string.menu_v3_section_personal_plan,
        arrayOf(R.id.nav_personal_plan)
    ) {
        navigator.goToPersonalPlanOrHome()
    }
    val menuItemAuthenticator = MenuItem(
        R.drawable.ic_drawer_authenticator,
        R.drawable.ic_drawer_authenticator_selected,
        R.string.menu_authenticator,
        arrayOf(R.id.nav_authenticator_dashboard, R.id.nav_authenticator_suggestions)
    ) {
        navigator.goToAuthenticator()
    }

    val fullMenu: List<MenuDef.Item>?
        get() = Collections.unmodifiableList(buildFullMenu())

    @Suppress("LongMethod")
    private fun buildFullMenu(): List<MenuDef.Item> {
        val list: MutableList<MenuDef.Item> = ArrayList()
        addItem(list, menuItemPersonalPlan)
        addItem(
            list, MenuItem(
                R.drawable.ic_drawer_home,
                R.drawable.ic_drawer_home_selected,
                R.string.menu_v2_home,
                arrayOf(R.id.nav_home)
            ) {
                navigator.goToHome(ORIGIN_MENU)
            }
        )
        addItem(list, menuItemActionCenter)
        addItem(
            list, MenuItem(
                R.drawable.ic_drawer_password_generator, R.drawable.ic_drawer_password_generator_selected,
                R.string.generated_password,
                arrayOf(R.id.nav_password_generator)
            ) {
                navigator.goToPasswordGenerator(ORIGIN_MENU)
            }
        )
        list.add(MenuSeparatorViewHolder.ITEM)

        
        addHeader(list, R.string.menu_v3_header_security_boosters)
        addItem(
            list,
            MenuItem(
                R.drawable.ic_drawer_sharing_center,
                R.drawable.ic_drawer_sharing_center_selected,
                R.string.menu_v3_section_sharing_center,
                arrayOf(R.id.nav_sharing_center)
            ) {
                navigator.goToPasswordSharing(ORIGIN_MENU)
            }
        )
        addItem(list, menuItemPasswordHealth)
        addItem(list, menuItemDarkWebMonitoring)
        addItem(list, menuItemAuthenticator)
        addItem(list, menuItemVpn)
        list.add(MenuSeparatorViewHolder.ITEM)

        
        addHeader(list, R.string.menu_v3_header_manage_account)
        addItem(
            list, MenuItem(
                R.drawable.ic_drawer_settings, R.drawable.ic_drawer_settings_selected,
                R.string.menu_v2_settings_button,
                arrayOf(R.id.nav_settings)
            ) {
                navigator.goToSettings(origin = ORIGIN_MENU)
            }
        )
        addItem(
            list,
            MenuItem(
                R.drawable.ic_drawer_help,
                R.drawable.ic_drawer_help_selected,
                R.string.menu_help_center
            ) {
                navigator.goToHelpCenter(ORIGIN_MENU)
            }
        )
        list.add(MenuLockFooterItem())
        return list
    }

    private fun addItem(list: MutableList<MenuDef.Item>, menuItem: MenuItem) {
        list.add(menuItem)
    }

    private fun addHeader(list: MutableList<MenuDef.Item>, @StringRes textResId: Int) {
        list.add(MenuSectionHeaderItem(textResId))
    }

    companion object {
        const val ORIGIN_MENU = "mainMenu"
    }
}