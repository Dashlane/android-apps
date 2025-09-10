package com.dashlane.xml.domain

import com.dashlane.xml.XmlData
import com.dashlane.xml.asCollectionValueOrNull
import com.dashlane.xml.asObjectOrNull
import com.dashlane.xml.domain.SyncObject.Authentifiant.TrustedUrlGroup
import com.dashlane.xml.domain.SyncObject.Collection.VaultItems
import com.dashlane.xml.domain.SyncObject.DataChangeHistory.ChangeSet
import com.dashlane.xml.domain.SyncObject.Passkey.CloudPasskey
import com.dashlane.xml.domain.SyncObject.Passkey.PrivateKey
import com.dashlane.xml.toCollectionNode
import com.dashlane.xml.toObjectNode
import javax.annotation.processing.Generated

@Generated
fun XmlData.asTrustedUrlGroupOrNull(): TrustedUrlGroup? =
    asCollectionValueOrNull()?.let { TrustedUrlGroup(it) }

@Generated
fun TrustedUrlGroup.toCollectionNode(): XmlData.CollectionNode =
    data.toCollectionNode()

@Generated
fun XmlData.asVaultItemsOrNull(): VaultItems? =
    asCollectionValueOrNull()?.let { VaultItems(it) }

@Generated
fun VaultItems.toCollectionNode(): XmlData.CollectionNode =
    data.toCollectionNode()

@Generated
fun XmlData.asChangeSetOrNull(): ChangeSet? =
    asObjectOrNull(SyncObjectXmlName.CHANGE_SET, ::ChangeSet)

@Generated
fun ChangeSet.toObjectNode(): XmlData.ObjectNode =
    data.toObjectNode(SyncObjectXmlName.CHANGE_SET)

@Generated
fun XmlData.asCloudPasskeyOrNull(): CloudPasskey? =
    asCollectionValueOrNull()?.let { CloudPasskey(it) }

@Generated
fun CloudPasskey.toCollectionNode(): XmlData.CollectionNode =
    data.toCollectionNode()

@Generated
fun XmlData.asPrivateKeyOrNull(): PrivateKey? =
    asCollectionValueOrNull()?.let { PrivateKey(it) }

@Generated
fun PrivateKey.toCollectionNode(): XmlData.CollectionNode =
    data.toCollectionNode()