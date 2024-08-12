package com.dashlane.item.v3.util

import com.dashlane.authenticator.Otp
import com.dashlane.autofill.LinkedServicesHelper
import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.FormData
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.util.isValidEmail
import com.dashlane.util.obfuscated.matchesNullAsEmpty
import com.dashlane.util.obfuscated.toSyncObfuscatedValue
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.urlForUI
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class ItemEditValueUpdateManagerImpl @Inject constructor(
    private val linkedServicesHelper: LinkedServicesHelper
) : ItemEditValueUpdateManager {
    override val editedFields: MutableSet<Field> = mutableSetOf()
    override fun updateWithData(
        formData: FormData,
        initialVaultItem: VaultItem<SyncObject>
    ): VaultItem<SyncObject>? {
        
        editedFields.clear()
        return when (formData) {
            is CredentialFormData -> initialVaultItem.copy(
                syncObject = (initialVaultItem.syncObject as SyncObject.Authentifiant)
                    .copyForUpdateEmailAndLogin(formData.login, formData.email)
                    .copyForUpdatedSecondaryLogin(formData.secondaryLogin)
                    .copyForUpdatedPassword(formData.password?.value?.toString())
                    .copyForUpdateOtp(formData.otp)
                    .copyForUpdatedName(formData.name)
                    .copyForUpdatedNote(formData.note)
                    .copyForUpdatedTeamspace(formData.space ?: TeamSpace.Personal)
                    .copyForUpdatedWebsite(formData.url)
                    .copyForLinkedWebsites(formData.linkedServices)
            )
            else -> null
        }
    }

    private fun SyncObject.Authentifiant.copyForUpdateEmailAndLogin(
        login: String?,
        email: String?
    ): SyncObject.Authentifiant {
        val oldLogin = this.login.orEmpty()
        val oldEmail = this.email.orEmpty()

        return when {
            email.isValidEmail() -> {
                
                updateEmailAndLogin(email, oldEmail, login, oldLogin)
            }
            login.isValidEmail() -> {
                
                if (email != oldLogin) editedFields += Field.LOGIN
                if (login != oldEmail) editedFields += Field.EMAIL
                this.copy {
                    this.email = login
                    this.login = email
                }
            }
            login.isNullOrEmpty() && !email.isNullOrEmpty() -> {
                
                if (email != oldLogin) editedFields += Field.LOGIN
                if (oldEmail.isNotEmpty()) editedFields += Field.EMAIL
                this.copy {
                    this.email = null
                    this.login = email
                }
            }
            else -> {
                
                updateEmailAndLogin(email, oldEmail, login, oldLogin)
            }
        }
    }

    private fun SyncObject.Authentifiant.updateEmailAndLogin(
        email: String?,
        oldEmail: String,
        login: String?,
        oldLogin: String
    ): SyncObject.Authentifiant {
        if (email != oldEmail) editedFields += Field.EMAIL
        if (login != oldLogin) editedFields += Field.LOGIN
        return this.copy {
            this.email = email
            this.login = login
        }
    }

    private fun SyncObject.Authentifiant.copyForUpdatedSecondaryLogin(value: String?): SyncObject.Authentifiant {
        return if (value == this.secondaryLogin.orEmpty()) {
            this
        } else {
            editedFields += Field.SECONDARY_LOGIN
            this.copy { secondaryLogin = value }
        }
    }

    private fun SyncObject.Authentifiant.copyForUpdatedPassword(value: String?): SyncObject.Authentifiant {
        return if (this.password.matchesNullAsEmpty(value)) {
            this
        } else {
            editedFields += Field.PASSWORD
            this.copy {
                password = value.toSyncObfuscatedValue()
            }
        }
    }

    private fun SyncObject.Authentifiant.copyForUpdateOtp(value: Otp?): SyncObject.Authentifiant {
        return if (this.otpSecret.matchesNullAsEmpty(value?.secret ?: "") &&
            this.otpUrl.matchesNullAsEmpty(value?.url ?: "")
        ) {
            this
        } else if (value?.isStandardOtp() == false &&
            this.otpUrl.matchesNullAsEmpty(
                value.url ?: ""
            ) && this.otpSecret.isNullOrEmpty()
        ) {
            this
        } else {
            editedFields += Field.OTP_SECRET
            this.copy {
                otpUrl = value?.url?.toSyncObfuscatedValue() ?: SyncObfuscatedValue("")
                otpSecret = if (value?.isStandardOtp() == true) {
                    value.secret?.toSyncObfuscatedValue()
                } else {
                    null
                } ?: SyncObfuscatedValue("")
            }
        }
    }

    private fun SyncObject.Authentifiant.copyForUpdatedNote(value: String?): SyncObject.Authentifiant {
        return if (value == this.note.orEmpty()) {
            this
        } else {
            editedFields += Field.NOTE
            this.copy { note = value }
        }
    }

    private fun SyncObject.Authentifiant.copyForUpdatedTeamspace(value: TeamSpace): SyncObject.Authentifiant {
        val spaceId = this.spaceId ?: TeamSpace.Personal.teamId
        return if (value.teamId == spaceId) {
            this
        } else {
            this.copy { this.spaceId = value.teamId }
        }
    }

    private fun SyncObject.Authentifiant.copyForUpdatedName(value: String): SyncObject.Authentifiant {
        return if (value == this.title.orEmpty()) {
            this
        } else {
            editedFields += Field.TITLE
            this.copy { title = value }
        }
    }

    private fun SyncObject.Authentifiant.copyForUpdatedWebsite(value: String?): SyncObject.Authentifiant {
        return if (value == this.urlForUI().orEmpty()) {
            this
        } else {
            editedFields += Field.URL
            this.copy {
                url = value
                userSelectedUrl = value
                useFixedUrl = true
            }
        }
    }

    private fun SyncObject.Authentifiant.copyForLinkedWebsites(value: CredentialFormData.LinkedServices): SyncObject.Authentifiant {
        val newLinkedServices = linkedServicesHelper.replaceAllLinkedDomains(
            linkedServices,
            value.addedByUserDomains
        )
        val newLinkedServicesWithApps = linkedServicesHelper.replaceAllLinkedAppsByUser(
            newLinkedServices,
            value.addedByUserApps + value.addedByDashlaneApps
        )
        return if (newLinkedServices == this.linkedServices) {
            this
        } else {
            if (newLinkedServices.associatedAndroidApps != this.linkedServices?.associatedAndroidApps) {
                editedFields += Field.ASSOCIATED_APPS_LIST
            } else if (newLinkedServices.associatedDomains != this.linkedServices?.associatedDomains) {
                editedFields += Field.ASSOCIATED_WEBSITES_LIST
            }
            this.copy {
                linkedServices = newLinkedServicesWithApps
            }
        }
    }
}