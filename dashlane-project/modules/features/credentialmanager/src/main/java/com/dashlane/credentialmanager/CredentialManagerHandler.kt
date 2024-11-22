package com.dashlane.credentialmanager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BlendMode
import android.graphics.drawable.Icon
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
import com.dashlane.credentialmanager.model.PasskeyCreationOptions
import com.dashlane.credentialmanager.model.PasskeyRequestOptions
import com.dashlane.credentialsmanager.R
import com.dashlane.util.toBitmap
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.summary.SummaryObject
import com.google.gson.Gson
import java.time.Instant
import javax.inject.Inject

interface CredentialManagerHandler {
    fun handleCreateCredential(context: Context, request: BeginCreateCredentialRequest): List<CreateEntry>
    fun handleGetCredentials(context: Context, request: BeginGetCredentialRequest): List<CredentialEntry>
}

@RequiresApi(34)
class CredentialManagerHandlerImpl @Inject constructor(
    private val credentialLoader: CredentialLoader,
    private val credentialManagerLogger: CredentialManagerLogger
) : CredentialManagerHandler {
    override fun handleCreateCredential(context: Context, request: BeginCreateCredentialRequest): List<CreateEntry> {
        return when (request) {
            is BeginCreatePasswordCredentialRequest -> listOf(
                CreateEntry.Builder(
                    context.getString(R.string.credential_manager_create_password),
                    pendingIntentForCreate(context)
                ).setIcon(buildMicroLogomarkIcon(context = context))
                    .setDescription(context.getString(R.string.credential_manager_create_password_description))
            )
            is BeginCreatePublicKeyCredentialRequest -> {
                val passkeyCreationOptions = Gson().fromJson(request.requestJson, PasskeyCreationOptions::class.java)
                if (matchExcludedCredentials(passkeyCreationOptions)) {
                    return emptyList()
                }
                credentialManagerLogger.logSuggestPasskeyCreate(request.callingAppInfo)
                listOf(
                    CreateEntry.Builder(
                        context.getString(R.string.credential_manager_create_passkey),
                        pendingIntentForCreate(context)
                    ).setIcon(buildMicroLogomarkIcon(context = context))
                        .setDescription(context.getString(R.string.credential_manager_create_passkey_description))
                )
            }
            else -> listOf()
        }.map {
            it.setPasswordCredentialCount(credentialLoader.countPasswords())
                .setPublicKeyCredentialCount(credentialLoader.countPasskeys())
                .build()
        }
    }

    private fun matchExcludedCredentials(options: PasskeyCreationOptions): Boolean {
        if (options.excludeCredentials.isNullOrEmpty()) {
            return false
        }
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

    override fun handleGetCredentials(context: Context, request: BeginGetCredentialRequest): List<CredentialEntry> =
        request.beginGetCredentialOptions.flatMap { option ->
            when (option) {
                is BeginGetPasswordOption -> request.callingAppInfo?.let { callingAppInfo ->
                    credentialLoader.loadPasswordCredentials(callingAppInfo.packageName).map { login ->
                        PasswordCredentialEntry.Builder(
                            context,
                            getSuggestionTitle(login),
                            pendingIntentForGet(context, login.id),
                            option
                        ).setLastUsedTime(login.locallyViewedDate)
                            .setIcon(buildMicroLogomarkIcon(context = context))
                            .setDisplayName(login.loginForUi)
                            .build()
                    }
                } ?: listOf()
                is BeginGetPublicKeyCredentialOption -> {
                    val passkeyRequestOptions = Gson().fromJson(option.requestJson, PasskeyRequestOptions::class.java)
                    credentialLoader.loadPasskeyCredentials(
                        passkeyRequestOptions.rpId,
                        passkeyRequestOptions.allowCredentials ?: listOf()
                    ).map { passkey ->
                        val passkeyDisplayName = getSuggestionTitle(passkey, context)
                        PublicKeyCredentialEntry.Builder(
                            context,
                            passkeyDisplayName,
                            pendingIntentForGet(context, passkey.id),
                            option
                        ).setLastUsedTime(passkey.locallyViewedDate)
                            .setIcon(buildMicroLogomarkIcon(context = context))
                            .setDisplayName(passkeyDisplayName)
                            .build()
                    }.also {
                        credentialManagerLogger.logSuggestPasskeyLogin(request.callingAppInfo, it.size)
                    }
                }
                else -> {
                    throw UnsupportedOperationException("Unknown option: $option")
                }
            }
        }

    @VisibleForTesting
    fun getSuggestionTitle(passkey: SummaryObject.Passkey, context: Context): String =
        passkey.userDisplayName?.takeUnless { it.isEmpty() }
            ?: passkey.rpId?.takeUnless { it.isEmpty() }
            ?: context.getString(R.string.credential_manager_unknown)

    @VisibleForTesting
    fun getSuggestionTitle(login: SummaryObject.Authentifiant): String = login.loginForUi.orEmpty()

    private fun pendingIntentForCreate(context: Context): PendingIntent {
        val intent = Intent(CREATE_INTENT).setPackage(context.packageName)

        return PendingIntent.getActivity(
            context,
            Instant.now().toEpochMilli().toInt(),
            intent,
            (PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        )
    }

    private fun pendingIntentForGet(context: Context, credentialId: String): PendingIntent {
        val intent = Intent(GET_INTENT).setPackage(context.packageName)
        intent.putExtra(ARG_CREDENTIAL_ID, credentialId)

        return PendingIntent.getActivity(
            context,
            Instant.now().toEpochMilli().toInt(),
            intent,
            (PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        )
    }

    private fun buildMicroLogomarkIcon(context: Context) =
        AppCompatResources.getDrawable(context, R.drawable.vd_logo_dashlane_micro_logomark)
            ?.apply {
                DrawableCompat.wrap(this)
                DrawableCompat.setTint(this, getColor(context, R.color.oddity_brand))
            }
            ?.toBitmap()
            ?.run { Icon.createWithBitmap(this) }
            ?.apply { setTintBlendMode(BlendMode.DST) }
            ?: Icon.createWithResource(context, R.drawable.vd_logo_dashlane_micro_logomark)

    companion object {
        private const val CREATE_INTENT = "com.dashlane.credentialmanager.CREATE_CREDENTIAL_MANAGER"
        private const val GET_INTENT = "com.dashlane.credentialmanager.GET_CREDENTIAL_MANAGER"
        const val ARG_CREDENTIAL_ID = "arg_credential_id"
    }
}