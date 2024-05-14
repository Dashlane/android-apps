package com.dashlane.autofill.generatepassword

import com.dashlane.passwordstrength.PasswordStrengthScore
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject

sealed class GeneratePasswordState {
    abstract val data: GeneratePasswordData

    data class Initial(override val data: GeneratePasswordData) : GeneratePasswordState()

    data class PasswordGenerated(override val data: GeneratePasswordData, val password: String) :
        GeneratePasswordState()

    data class PasswordSavedToHistory(
        override val data: GeneratePasswordData,
        val authentifiant: VaultItem<SyncObject.Authentifiant>
    ) : GeneratePasswordState()
}

data class GeneratePasswordData(
    val strengthScore: PasswordStrengthScore? = null,
    val specialMode: GeneratePasswordSpecialMode? = null,
    val lastGeneratedPassword: String? = null
)