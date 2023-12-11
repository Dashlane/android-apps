package com.dashlane.ui.fragments

import com.dashlane.passwordstrength.PasswordStrength
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject

interface PasswordGenerationCallback {
    fun onPasswordGenerated()
    fun passwordSaved(generatedPassword: VaultItem<SyncObject.GeneratedPassword>, strength: PasswordStrength?)
    fun restoreDominantColor(color: Int)
    fun showPreviouslyGenerated()
}