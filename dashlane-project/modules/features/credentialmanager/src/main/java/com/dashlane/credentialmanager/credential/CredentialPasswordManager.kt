package com.dashlane.credentialmanager.credential

import android.content.Context
import androidx.annotation.RequiresApi
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CreatePasswordResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.provider.CallingAppInfo
import com.dashlane.credentialmanager.CredentialManagerDAO
import com.dashlane.credentialmanager.model.MissingFieldException
import com.dashlane.ext.application.KnownApplicationProvider
import com.dashlane.util.PackageUtilities
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.formatTitle
import com.dashlane.vault.model.loginForUi
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface CredentialPasswordManager {
    suspend fun createPasswordLogin(
        createPasswordRequest: CreatePasswordRequest,
        callingAppInfo: CallingAppInfo
    ): CreatePasswordResponse

    fun providePasswordLogin(passwordSyncObject: VaultItem<SyncObject.Authentifiant>): PasswordCredential
}

@RequiresApi(34)
class CredentialPasswordManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val databaseAccess: CredentialManagerDAO,
    private val knownApplicationProvider: KnownApplicationProvider,
) : CredentialPasswordManager {
    override suspend fun createPasswordLogin(
        createPasswordRequest: CreatePasswordRequest,
        callingAppInfo: CallingAppInfo
    ): CreatePasswordResponse {
        val website = getWebsiteFromPackageName(callingAppInfo.packageName)
        
        databaseAccess.savePasswordCredential(
            context,
            getLoginTitle(context, website, callingAppInfo.packageName),
            website,
            createPasswordRequest.id,
            createPasswordRequest.password,
            callingAppInfo.packageName,
        )
        return CreatePasswordResponse()
    }

    private fun getWebsiteFromPackageName(packageName: String?): String? {
        return packageName?.let {
            knownApplicationProvider.getKnownApplication(it)?.mainDomain
        }
    }

    private fun getLoginTitle(context: Context, website: String?, packageName: String?): String? {
        if (website != null) {
            return SyncObject.Authentifiant.formatTitle(website)
        } else if (packageName != null) {
            return SyncObject.Authentifiant.formatTitle(
                PackageUtilities.getApplicationNameFromPackage(context, packageName)
            )
        }
        return null
    }

    override fun providePasswordLogin(passwordSyncObject: VaultItem<SyncObject.Authentifiant>): PasswordCredential {
        if (passwordSyncObject.syncObject.loginForUi == null) {
            throw MissingFieldException("Login cannot be null")
        }
        if (passwordSyncObject.syncObject.password == null) {
            throw MissingFieldException("Password cannot be null")
        }
        return PasswordCredential(
            passwordSyncObject.syncObject.loginForUi!!,
            passwordSyncObject.syncObject.password.toString(),
        )
    }
}