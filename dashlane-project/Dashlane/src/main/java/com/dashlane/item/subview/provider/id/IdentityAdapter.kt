package com.dashlane.item.subview.provider.id

import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import java.time.LocalDate

interface IdentityAdapter<T : SyncObject> {
    fun fullName(item: VaultItem<T>): String?

    fun withFullName(item: VaultItem<T>, fullName: String?): VaultItem<T>

    fun gender(item: VaultItem<T>): SyncObject.Gender?

    fun withGender(item: VaultItem<T>, gender: SyncObject.Gender?): VaultItem<T>

    fun birthDate(item: VaultItem<T>): LocalDate?

    fun withBirthDate(item: VaultItem<T>, birthDate: LocalDate?): VaultItem<T>

    fun linkedIdentity(item: VaultItem<T>): String?

    fun withLinkedIdentity(item: VaultItem<T>, identity: String?): VaultItem<T>
}