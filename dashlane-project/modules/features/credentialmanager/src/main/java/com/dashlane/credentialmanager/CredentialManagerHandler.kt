package com.dashlane.credentialmanager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BlendMode
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat.getColor
import androidx.core.graphics.drawable.DrawableCompat
import androidx.credentials.provider.BeginCreateCredentialRequest
import androidx.credentials.provider.BeginCreatePasswordCredentialRequest
import androidx.credentials.provider.BeginCreatePublicKeyCredentialRequest
import androidx.credentials.provider.BeginGetCredentialRequest
import androidx.credentials.provider.BeginGetPasswordOption
import androidx.credentials.provider.BeginGetPublicKeyCredentialOption
import androidx.credentials.provider.CreateEntry
import androidx.credentials.provider.CredentialEntry
import androidx.credentials.provider.PasswordCredentialEntry
import androidx.credentials.provider.PublicKeyCredentialEntry
import com.dashlane.credentialmanager.authentication.CredentialBiometricAuthProvider
import com.dashlane.credentialmanager.model.CredentialManagerPlatform
import com.dashlane.credentialmanager.model.fido.request.PasskeyCreationOptions
import com.dashlane.credentialmanager.model.fido.request.PasskeyRequestOptions
import com.dashlane.credentialsmanager.R
import com.dashlane.user.Username
import com.dashlane.util.toBitmap
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.summary.SummaryObject
import kotlinx.serialization.json.Json
import java.time.Instant
import javax.inject.Inject

interface CredentialManagerHandler {
    fun handleCreateCredential(
        context: Context,
        request: BeginCreateCredentialRequest,
        username: Username?,
    ): List<CreateEntry>

    fun handleGetCredentials(
        context: Context,
        request: BeginGetCredentialRequest,
        username: Username?,
    ): List<CredentialEntry>
}

@RequiresApi(34)
class CredentialManagerHandlerImpl @Inject constructor(
    private val credentialLoader: CredentialLoader,
    private val credentialManagerLogger: CredentialManagerLogger,
    private val biometricAuthProvider: CredentialBiometricAuthProvider,
    private val currentPlatform: CredentialManagerPlatform,
) : CredentialManagerHandler {
    override fun handleCreateCredential(
        context: Context,
        request: BeginCreateCredentialRequest,
        username: Username?,
    ): List<CreateEntry> {
        return when (request) {
            is BeginCreatePasswordCredentialRequest ->
                listOf(buildCreatePasswordEntry(context, username, credentialLoader))
            is BeginCreatePublicKeyCredentialRequest -> {
                val passkeyCreationOptions =
                    Json.decodeFromString<PasskeyCreationOptions>(request.requestJson)
                if (matchExcludedCredentials(passkeyCreationOptions)) {
                    return emptyList()
                }
                listOf(buildCreatePasskeyEntry(context, username, credentialLoader))
            }
            else -> listOf()
        }
    }

    override fun handleGetCredentials(
        context: Context,
        request: BeginGetCredentialRequest,
        username: Username?,
    ): List<CredentialEntry> =
        request.beginGetCredentialOptions.flatMap { option ->
            when (option) {
                is BeginGetPasswordOption -> request.callingAppInfo?.let { callingAppInfo ->
                    credentialLoader.loadPasswordCredentials(callingAppInfo.packageName)
                        .map { login -> buildGetPasswordEntry(context, username, login, option) }
                } ?: listOf()
                is BeginGetPublicKeyCredentialOption -> {
                    val passkeyRequestOptions =
                        Json.decodeFromString<PasskeyRequestOptions>(option.requestJson)
                    credentialLoader.loadPasskeyCredentials(
                        passkeyRequestOptions.rpId,
                        passkeyRequestOptions.allowCredentials ?: listOf(),
                    ).map { passkey -> buildGetPasskeyEntry(context, passkey, username, option) }
                        .also {
                            credentialManagerLogger.logSuggestPasskeyLogin(
                                request.callingAppInfo,
                                it.size,
                            )
                        }
                }
                else -> throw UnsupportedOperationException("Unknown option: $option")
            }
        }

    private fun matchExcludedCredentials(options: PasskeyCreationOptions): Boolean {
        if (options.excludeCredentials.isNullOrEmpty()) return false
        return credentialLoader.loadPasskeyCredentials(options.rp.id, listOf()).let {
            it.any { summary ->
                if (summary.credentialId == null) {
                    return@any false
                }
                options.excludeCredentials.any { descriptor ->
                    descriptor.id == summary.credentialId!!
                }
            }
        }
    }

    private fun buildCreatePasskeyEntry(
        context: Context,
        username: Username?,
        credentialLoader: CredentialLoader,
    ) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        val biometricPromptData = biometricAuthProvider.getBiometricPromptData(username)
        CreateEntry(
            accountName = context.getString(R.string.credential_manager_create_passkey),
            pendingIntent = pendingIntentForCreate(
                context,
                biometricPromptData?.cryptoObject != null,
            ),
            icon = getEntryLogo(context = context),
            description = context.getString(R.string.credential_manager_create_passkey_description),
            passwordCredentialCount = credentialLoader.countPasswords(),
            publicKeyCredentialCount = credentialLoader.countPasskeys(),
            biometricPromptData = biometricPromptData,
        )
    } else {
        CreateEntry.Builder(
            context.getString(R.string.credential_manager_create_passkey),
            pendingIntentForCreate(context, false),
        )
            .setIcon(getEntryLogo(context = context))
            .setDescription(context.getString(R.string.credential_manager_create_passkey_description))
            .setPasswordCredentialCount(credentialLoader.countPasswords())
            .setPublicKeyCredentialCount(credentialLoader.countPasskeys())
            .build()
    }

    private fun buildCreatePasswordEntry(
        context: Context,
        username: Username?,
        credentialLoader: CredentialLoader,
    ) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        val biometricPromptData = biometricAuthProvider.getBiometricPromptData(username)
        CreateEntry(
            accountName = context.getString(R.string.credential_manager_create_password),
            pendingIntent = pendingIntentForCreate(
                context,
                biometricPromptData?.cryptoObject != null,
            ),
            icon = getEntryLogo(context = context),
            description = context.getString(R.string.credential_manager_create_password_description),
            passwordCredentialCount = credentialLoader.countPasswords(),
            publicKeyCredentialCount = credentialLoader.countPasskeys(),
            biometricPromptData = biometricPromptData,
        )
    } else {
        CreateEntry.Builder(
            context.getString(R.string.credential_manager_create_password),
            pendingIntentForCreate(context, false),
        )
            .setIcon(getEntryLogo(context = context))
            .setDescription(context.getString(R.string.credential_manager_create_password_description))
            .setPasswordCredentialCount(credentialLoader.countPasswords())
            .setPublicKeyCredentialCount(credentialLoader.countPasskeys())
            .build()
    }

    private fun buildGetPasskeyEntry(
        context: Context,
        passkey: SummaryObject.Passkey,
        username: Username?,
        option: BeginGetPublicKeyCredentialOption,
    ): PublicKeyCredentialEntry {
        val passkeyDisplayName = getSuggestionTitle(passkey, context)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val biometricPromptData = biometricAuthProvider.getBiometricPromptData(username)
            PublicKeyCredentialEntry(
                context = context,
                username = passkeyDisplayName,
                pendingIntent = pendingIntentForGet(
                    context,
                    passkey.id,
                    biometricPromptData?.cryptoObject != null,
                ),
                beginGetPublicKeyCredentialOption = option,
                biometricPromptData = biometricPromptData,
                icon = getEntryLogo(context = context),
                displayName = passkeyDisplayName,
            )
        } else {
            PublicKeyCredentialEntry.Builder(
                context,
                passkeyDisplayName,
                pendingIntentForGet(context, passkey.id, false),
                option,
            ).setLastUsedTime(passkey.locallyViewedDate)
                .setIcon(getEntryLogo(context = context))
                .setDisplayName(passkeyDisplayName)
                .build()
        }
    }

    private fun buildGetPasswordEntry(
        context: Context,
        username: Username?,
        login: SummaryObject.Authentifiant,
        option: BeginGetPasswordOption,
    ) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        val biometricPromptData = biometricAuthProvider.getBiometricPromptData(username)
        PasswordCredentialEntry(
            context = context,
            username = getSuggestionTitle(login),
            pendingIntent = pendingIntentForGet(
                context,
                login.id,
                biometricPromptData?.cryptoObject != null,
            ),
            beginGetPasswordOption = option,
            biometricPromptData = biometricPromptData,
            icon = getEntryLogo(context = context),
            lastUsedTime = login.locallyViewedDate,
            displayName = login.loginForUi,
        )
    } else {
        PasswordCredentialEntry.Builder(
            context = context,
            username = getSuggestionTitle(login),
            pendingIntent = pendingIntentForGet(context, login.id, false),
            beginGetPasswordOption = option,
        ).setLastUsedTime(login.locallyViewedDate)
            .setIcon(getEntryLogo(context = context))
            .setDisplayName(login.loginForUi)
            .build()
    }

    @VisibleForTesting
    fun getSuggestionTitle(passkey: SummaryObject.Passkey, context: Context): String =
        passkey.userDisplayName?.takeUnless { it.isEmpty() }
            ?: passkey.rpId?.takeUnless { it.isEmpty() }
            ?: context.getString(R.string.credential_manager_unknown)

    @VisibleForTesting
    fun getSuggestionTitle(login: SummaryObject.Authentifiant): String = login.loginForUi.orEmpty()

    private fun pendingIntentForCreate(
        context: Context,
        cryptoObjectRequired: Boolean,
    ): PendingIntent {
        val intent = Intent(CREATE_INTENT).apply {
            setPackage(context.packageName)
            putExtra(ARG_CRYPTO_OBJECT_REQUIRED, cryptoObjectRequired)
        }
        return PendingIntent.getActivity(
            context,
            Instant.now().toEpochMilli().toInt(),
            intent,
            (PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT),
        )
    }

    private fun pendingIntentForGet(
        context: Context,
        credentialId: String,
        cryptoObjectRequired: Boolean,
    ): PendingIntent {
        val intent = Intent(GET_INTENT).apply {
            setPackage(context.packageName)
            putExtra(ARG_CREDENTIAL_ID, credentialId)
            putExtra(ARG_CRYPTO_OBJECT_REQUIRED, cryptoObjectRequired)
        }
        return PendingIntent.getActivity(
            context,
            Instant.now().toEpochMilli().toInt(),
            intent,
            (PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT),
        )
    }

    private fun getEntryLogo(context: Context) =
        when (currentPlatform) {
            CredentialManagerPlatform.ANDROID -> buildMicroLogomarkIcon(context)
            CredentialManagerPlatform.WEAR_OS -> buildRoundLogomarkIcon(context)
        } ?: Icon.createWithResource(context, R.drawable.vd_logo_dashlane_micro_logomark)

    private fun buildMicroLogomarkIcon(context: Context) =
        AppCompatResources.getDrawable(context, R.drawable.vd_logo_dashlane_micro_logomark)
            ?.apply {
                DrawableCompat.wrap(this)
                DrawableCompat.setTint(this, getColor(context, R.color.oddity_brand))
            }
            ?.toBitmap()
            ?.run { Icon.createWithBitmap(this) }
            ?.apply { setTintBlendMode(BlendMode.DST) }

    private fun buildRoundLogomarkIcon(context: Context) =
        AppCompatResources.getDrawable(context, R.drawable.round_dashlane_logomark)
            ?.apply {
                DrawableCompat.wrap(this)
            }
            ?.toBitmap()
            ?.run { Icon.createWithBitmap(this) }
            ?.apply { setTintBlendMode(BlendMode.DST) }

    companion object {
        private const val CREATE_INTENT = "com.dashlane.credentialmanager.CREATE_CREDENTIAL_MANAGER"
        private const val GET_INTENT = "com.dashlane.credentialmanager.GET_CREDENTIAL_MANAGER"
        const val ARG_CREDENTIAL_ID = "arg_credential_id"
        const val ARG_CRYPTO_OBJECT_REQUIRED = "arg_crypto_object_required"
    }
}