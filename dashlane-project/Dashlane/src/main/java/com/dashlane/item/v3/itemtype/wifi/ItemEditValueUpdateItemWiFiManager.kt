package com.dashlane.item.v3.itemtype.wifi

import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.item.v3.util.ItemEditValueUpdateItemManager
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.util.obfuscated.matchesNullAsEmpty
import com.dashlane.util.obfuscated.toSyncObfuscatedValue
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class ItemEditValueUpdateItemWiFiManager @Inject constructor() :
    ItemEditValueUpdateItemManager<WiFiFormData, SyncObject.WiFi> {
        private val editedFields = mutableSetOf<Field>()

        override fun updateWithData(
            data: Data<WiFiFormData>,
            initialVaultItem: VaultItem<SyncObject.WiFi>,
        ): Pair<VaultItem<SyncObject.WiFi>, Set<Field>> {
            editedFields.clear()
            val updatedItem = initialVaultItem.copy(
                syncObject = initialVaultItem.syncObject
                    .copyForUpdatedSsid(data.formData.ssid)
                    .copyForUpdatedItemName(data.commonData.name)
                    .copyForPassphrase(data.formData.passphrase)
                    .copyForNote(data.formData.note)
                    .copyForUpdatedTeamspace(data.commonData.space ?: TeamSpace.Personal),
            )

            return updatedItem to editedFields
        }

        private fun SyncObject.WiFi.copyForUpdatedItemName(value: String?): SyncObject.WiFi =
            if (this.name.orEmpty() == value.orEmpty()) {
                this
            } else {
                editedFields += Field.NETWORK_NAME
                this.copy {
                    name = value
                }
            }

        private fun SyncObject.WiFi.copyForUpdatedSsid(value: String?): SyncObject.WiFi =
            if (this.ssid.orEmpty() == value.orEmpty()) {
                this
            } else {
                editedFields += Field.SSID
                this.copy {
                    ssid = value
                }
            }

        private fun SyncObject.WiFi.copyForPassphrase(value: String?): SyncObject.WiFi =
            if (this.passphrase.matchesNullAsEmpty(value)) {
                this
            } else {
                editedFields += Field.PASSPHRASE
                this.copy {
                    passphrase = value.toSyncObfuscatedValue()
                }
            }

        private fun SyncObject.WiFi.copyForNote(value: String?): SyncObject.WiFi =
            if (this.note.orEmpty() == value.orEmpty()) {
                this
            } else {
                editedFields += Field.NOTE
                this.copy {
                    note = value
                }
            }

        private fun SyncObject.WiFi.copyForUpdatedTeamspace(value: TeamSpace): SyncObject.WiFi {
            val spaceId = this.spaceId ?: TeamSpace.Personal.teamId
            return if (value.teamId == spaceId) {
                this
            } else {
                this.copy { this.spaceId = value.teamId }
            }
        }
    }