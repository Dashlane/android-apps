package com.dashlane.settings.main.misc

import android.content.Context
import android.content.Intent
import com.dashlane.account.UserAccountStorage
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.accountstatus.premiumstatus.isAdvancedPlan
import com.dashlane.accountstatus.premiumstatus.isPremiumPlan
import com.dashlane.accountstatus.subscriptioncode.SubscriptionCodeRepository
import com.dashlane.debug.services.DaDaDaVersion
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.events.user.Logout
import com.dashlane.server.api.endpoints.invitation.GetSharingLinkService
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.session.SessionManager
import com.dashlane.settings.MainMisc
import com.dashlane.settings.SettingsError
import com.dashlane.settings.SettingsEvent
import com.dashlane.settings.SettingsEvent.DisplayDialog
import com.dashlane.settings.SettingsEvent.Error
import com.dashlane.settings.SettingsItemData
import com.dashlane.settings.main.misc.SettingsMainMiscState.View
import com.dashlane.settings.security.SettingsDialog
import com.dashlane.settings.ui.R
import com.dashlane.user.UserAccountInfo
import com.dashlane.util.NetworkStateProvider
import com.dashlane.util.PackageUtilities.getAppVersionName
import com.dashlane.util.clipboard.ClipboardCopy
import com.dashlane.util.getOsLang
import com.dashlane.util.isNotSemanticallyNull
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


@Suppress("kotlin:S3776", "kotlin:S107", "LongMethod")
internal fun loadMainMiscSettings(
    @ApplicationContext context: Context,
    sessionManager: SessionManager,
    accountStatusRepository: AccountStatusRepository,
    dadadaVersion: DaDaDaVersion,
    state: View.Initial,
): Flow<SettingsEvent<MainMisc>> {
    return flow {

        val mainMiscSettings = mutableListOf<SettingsItemData>()

        val session = sessionManager.session ?: run {
            emit(Error(SettingsError.Generic))
            return@flow
        }
        val accountStatus = accountStatusRepository[session]
        val premiumStatus = accountStatus?.premiumStatus
        val isPremiumRewardCandidate = premiumStatus?.isPremiumPlan == false || premiumStatus?.isAdvancedPlan == true

        mainMiscSettings.add(
            SettingsItemData.Click(
                id = MainMisc.InviteFriend,
                title = context.getString(R.string.invites),
                description = if (isPremiumRewardCandidate) {
                    context.getString(R.string.setting_send_invites_description)
                } else {
                    context.getString(R.string.setting_send_invites_description_premium_user)
                },
                enabled = true,
            ),
        )

        mainMiscSettings.add(
            SettingsItemData.Click(
                id = MainMisc.SendLove,
                title = context.getString(R.string.setting_send_love),
                description = context.getString(R.string.setting_send_love_description),
                enabled = true,
            ),
        )

        mainMiscSettings.add(
            SettingsItemData.Click(
                id = MainMisc.Logout,
                title = context.getString(R.string.logout),
                description = "",
                enabled = true,
            ),
        )

        mainMiscSettings.add(
            SettingsItemData.Click(
                id = MainMisc.Version,
                title = context.getString(R.string.setting_about_version),
                description = dadadaVersion.appVersionName ?: context.getAppVersionName(),
                enabled = true,
            ),
        )

        if (dadadaVersion.isEnabled) {
            mainMiscSettings.add(
                SettingsItemData.Click(
                    id = MainMisc.Debug,
                    title = context.getString(R.string.setting_debug_link),
                    description = "",
                    enabled = true,
                ),
            )
        }
        emit(SettingsEvent.SettingsLoaded(mainMiscSettings))
    }
}

fun dialogPositive(
    logRepository: LogRepository,
    state: View.DialogConfirmed,
): Flow<SettingsEvent<MainMisc>> {
    return flow {
        when (state.data.dialog) {
            SettingsDialog.Logout,
            SettingsDialog.LogoutPasswordless,
            -> {
                logRepository.queueEvent(Logout())
                emit(SettingsEvent.Logout)
            }
            else -> Unit
        }
    }
}

fun updateInviteFriend(
    @ApplicationContext context: Context,
    networkStateProvider: NetworkStateProvider,
    subscriptionCodeRepository: SubscriptionCodeRepository,
    getSharingLinkService: GetSharingLinkService,
    state: View.UpdateInviteFriend,
): Flow<SettingsEvent<MainMisc>> {
    return flow {

        if (!networkStateProvider.isOn()) {
            emit(Error(SettingsError.Offline))
            return@flow
        }

        try {
            val subscriptionCode = subscriptionCodeRepository.get()
            val response = getSharingLinkService.execute(request = GetSharingLinkService.Request(userKey = subscriptionCode))

            val sharingId = response.data.sharingId
            if (sharingId.isNotSemanticallyNull()) {
                val builder = StringBuilder()
                var lang = getOsLang()
                if (("en") != lang && "fr" != lang) {
                    lang = "en"
                }
                val url = builder.append("https://www.dashlane.com/")
                    .append(lang).append("/").append("as/")
                    .append(sharingId)
                    .toString()

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                    
                    putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.checkout_dashlane))
                    putExtra(Intent.EXTRA_TEXT, String.format(context.getString(R.string.refferal_text), url))
                }
                if (intent.resolveActivity(context.packageManager) == null) {
                    emit(Error(SettingsError.MainMiscError.NoAppAvailable))
                } else {
                    val chooserIntent = Intent.createChooser(intent, context.getString(R.string.invites))
                    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    emit(SettingsEvent.InviteFriendIntent(chooserIntent))
                }
            }
        } catch (ex: DashlaneApiException) {
            emit(Error(SettingsError.Network))
        }
    }
}

fun updateLogout(
    sessionManager: SessionManager,
    userAccountStorage: UserAccountStorage,
    state: View.UpdateLogout,
): Flow<SettingsEvent<MainMisc>> {
    return flow {
        val isPasswordlessAccount = sessionManager.session?.username
            ?.let { username -> userAccountStorage[username]?.accountType }
            .let { accountType -> accountType is UserAccountInfo.AccountType.InvisibleMasterPassword }

        if (isPasswordlessAccount) {
            emit(DisplayDialog(SettingsDialog.LogoutPasswordless))
        } else {
            emit(DisplayDialog(SettingsDialog.Logout))
        }
    }
}

fun updateVersion(
    @ApplicationContext context: Context,
    dadadaVersion: DaDaDaVersion,
    clipboardCopy: ClipboardCopy,
    state: View.UpdateVersion,
): Flow<SettingsEvent<MainMisc>> {
    return flow {
        val version = dadadaVersion.appVersionName ?: context.getAppVersionName()
        clipboardCopy.copyToClipboard(version, sensitiveData = false, autoClear = false, showToast = false)
        emit(SettingsEvent.SettingsLoaded(state.data.miscSettings))
    }
}