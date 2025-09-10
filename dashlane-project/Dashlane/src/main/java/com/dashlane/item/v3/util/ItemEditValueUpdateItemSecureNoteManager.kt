package com.dashlane.item.v3.util

import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.item.v3.data.SecureNoteFormData
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class ItemEditValueUpdateItemSecureNoteManager @Inject constructor() :
    ItemEditValueUpdateItemManager<SecureNoteFormData, SyncObject.SecureNote> {
        private val editedFields = mutableSetOf<Field>()

        override fun updateWithData(
            data: Data<SecureNoteFormData>,
            initialVaultItem: VaultItem<SyncObject.SecureNote>,
        ): Pair<VaultItem<SyncObject.SecureNote>, Set<Field>> {
            editedFields.clear()

            val updatedItem = initialVaultItem.copy(
                syncObject = initialVaultItem.syncObject
                    .copyForUpdatedName(data.commonData.name)
                    .copyForUpdatedContent(data.formData.content)
                    .copyForUpdatedSecured(data.formData.secured)
                    .copyForUpdatedType(data.formData.secureNoteType)
                    .copyForUpdatedTeamspace(data.commonData.space ?: TeamSpace.Personal),
            )
            return updatedItem to editedFields
        }

        private fun SyncObject.SecureNote.copyForUpdatedName(value: String?): SyncObject.SecureNote {
            return if (value.orEmpty() == this.title.orEmpty()) {
                this
            } else {
                editedFields += Field.TITLE
                this.copy { title = value }
            }
        }

        private fun SyncObject.SecureNote.copyForUpdatedContent(value: String?): SyncObject.SecureNote {
            return if (value.orEmpty() == this.content.orEmpty()) {
                this
            } else {
                editedFields += Field.CONTENT
                this.copy { content = value }
            }
        }

        private fun SyncObject.SecureNote.copyForUpdatedSecured(value: Boolean): SyncObject.SecureNote {
            return if (value == this.secured) {
                this
            } else {
                this.copy { secured = value }
            }
        }

        private fun SyncObject.SecureNote.copyForUpdatedType(value: SyncObject.SecureNoteType): SyncObject.SecureNote {
            return if (value == this.type) {
                this
            } else {
                editedFields += Field.COLOR
                this.copy { type = value }
            }
        }

        private fun SyncObject.SecureNote.copyForUpdatedTeamspace(value: TeamSpace): SyncObject.SecureNote {
            val spaceId = this.spaceId ?: TeamSpace.Personal.teamId
            return if (value.teamId == spaceId) {
                this
            } else {
                this.copy { this.spaceId = value.teamId }
            }
        }
    }