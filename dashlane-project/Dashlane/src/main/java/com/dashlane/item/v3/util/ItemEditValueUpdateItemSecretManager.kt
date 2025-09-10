package com.dashlane.item.v3.util

import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.item.v3.data.SecretFormData
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class ItemEditValueUpdateItemSecretManager @Inject constructor() :
    ItemEditValueUpdateItemManager<SecretFormData, SyncObject.Secret> {
        private val editedFields = mutableSetOf<Field>()

        override fun updateWithData(
            data: Data<SecretFormData>,
            initialVaultItem: VaultItem<SyncObject.Secret>,
        ): Pair<VaultItem<SyncObject.Secret>, Set<Field>> {
            editedFields.clear()

            val updatedItem = initialVaultItem.copy(
                syncObject = initialVaultItem.syncObject
                    .copyForUpdatedTitle(data.commonData.name)
                    .copyForUpdatedContent(data.formData.content)
                    .copyForUpdatedSecured(data.formData.secured)
                    .copyForUpdatedTeamspace(data.commonData.space ?: TeamSpace.Personal),
            )
            return updatedItem to editedFields
        }

        private fun SyncObject.Secret.copyForUpdatedTitle(value: String?): SyncObject.Secret {
            return if (value.orEmpty() == this.title.orEmpty()) {
                this
            } else {
                editedFields += Field.TITLE
                this.copy { title = value }
            }
        }

        private fun SyncObject.Secret.copyForUpdatedContent(value: String?): SyncObject.Secret {
            return if (value.orEmpty() == this.content.orEmpty()) {
                this
            } else {
                editedFields += Field.CONTENT
                this.copy { content = value }
            }
        }

        private fun SyncObject.Secret.copyForUpdatedSecured(value: Boolean): SyncObject.Secret {
            return if (value == this.secured) {
                this
            } else {
                this.copy { secured = value }
            }
        }

        private fun SyncObject.Secret.copyForUpdatedTeamspace(value: TeamSpace): SyncObject.Secret {
            val spaceId = this.spaceId ?: TeamSpace.Personal.teamId
            return if (value.teamId == spaceId) {
                this
            } else {
                this.copy { this.spaceId = value.teamId }
            }
        }
    }