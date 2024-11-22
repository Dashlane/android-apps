package com.dashlane.item

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.StringRes
import com.dashlane.authenticator.Otp
import com.dashlane.item.header.ItemHeader
import com.dashlane.item.subview.ItemSubView
import com.dashlane.item.subview.action.MenuAction
import com.dashlane.teamspaces.ui.TeamSpaceRestrictionNotificator
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.skocken.presentation.definition.Base
import kotlinx.coroutines.CoroutineScope

interface ItemEditViewContract {

    interface View : Base.IView {
        val listener: UiUpdateListener
        var isToolbarCollapsed: Boolean

        fun setConfiguration(
            screenConfiguration: ScreenConfiguration,
            isEditMode: Boolean,
            isToolbarCollapsed: Boolean,
            isChangingMode: Boolean
        )

        fun setMenus(menuActions: List<MenuAction>, menu: Menu)

        fun selectMenuItem(item: MenuItem): Boolean

        fun askForSave(action: (Boolean) -> Unit)

        fun showConfirmDeleteDialog(itemId: String, isShared: Boolean)

        fun showSaveConfirmation()

        interface UiUpdateListener {
            fun notifySubViewChanged(itemSubView: ItemSubView<*>)

            fun notifyColorChanged(color: Int)

            fun notifyHeaderChanged(itemHeader: ItemHeader, editMode: Boolean)

            fun notifyDeleteClicked()

            fun notifyRestorePassword()

            fun notifyPotentialBarCodeScan(requestCode: Int, resultCode: Int, data: Intent?)

            fun notifyNotEnoughDataToSave(@StringRes message: Int)

            fun showNfcPromptDialog(dismissAction: () -> Unit = {})

            fun showNfcErrorDialog()

            fun showNfcSuccessDialog(
                subviewToFocus: ItemSubView<*>? = null,
                dismissAction: () -> Unit = {}
            )

            fun openLinkedServices(
                itemId: String,
                fromViewOnly: Boolean,
                addNew: Boolean,
                temporaryWebsites: List<String>,
                temporaryApps: List<String>?,
                urlDomain: String?
            )

            fun openCollectionSelector(
                fromViewOnly: Boolean,
                temporaryPrivateCollectionsName: List<String>,
                temporarySharedCollectionsId: List<String>,
                spaceId: String?
            )

            fun notifyOtpRefreshed(otp: Otp)
        }
    }

    interface Presenter : Base.IPresenter {
        val isSecureNote: Boolean

        fun setup(context: Context, options: ItemEditViewSetupOptions)

        fun createMenu(
            menu: Menu,
            teamspaceRestrictionNotificator: TeamSpaceRestrictionNotificator,
            openItemHistory: (SyncObject.Authentifiant) -> Unit,
        ): Boolean

        fun selectMenuItem(item: MenuItem): Boolean

        fun deleteClicked()

        fun onPasswordRestored()

        fun otpRefreshed(otp: Otp)

        fun onBackPressed()

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun onNewIntent(data: Intent)

        fun onNewActivityLaunching(callback: Callback)

        fun isToolbarCollapsed(): Boolean

        fun onStart()

        fun onPause()

        interface Callback {
            fun onCompletion()
        }
    }

    interface DataProvider : Base.IDataProvider {

        val vaultItem: VaultItem<*>

        val isEditMode: Boolean

        val isSetup: Boolean

        suspend fun setup(
            context: Context,
            options: ItemEditViewSetupOptions,
            listener: View.UiUpdateListener
        ): Boolean

        suspend fun onSetupEnd(context: Context, options: ItemEditViewSetupOptions, listener: View.UiUpdateListener)

        fun getScreenConfiguration(): ScreenConfiguration

        suspend fun save(
            context: Context,
            subViews: List<ItemSubView<*>>,
            newAttachments: String? = null
        ): Boolean

        suspend fun saveRefreshedOtp(otp: Otp)

        fun hasUnsavedChanges(): Boolean

        fun changeMode(
            context: Context,
            editMode: Boolean,
            listener: View.UiUpdateListener
        )

        suspend fun onPasswordRestored(): Boolean

        fun onNewIntent(intent: Intent, coroutineScope: CoroutineScope)

        fun logViewDisplay()

        fun getAdditionalData(): Bundle

        fun hasPasswordHistory(authentifiant: SyncObject.Authentifiant): Boolean
    }
}