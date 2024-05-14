package com.dashlane.ui.adapter

import com.dashlane.hermes.generated.definitions.AnyPage

fun ItemListContext.toAnyPage(): AnyPage? = when (this.container) {
    ItemListContext.Container.ALL_ITEMS -> AnyPage.ITEM_ALL_LIST
    ItemListContext.Container.IDS_LIST -> AnyPage.ITEM_ID_LIST
    ItemListContext.Container.PAYMENT_LIST -> AnyPage.ITEM_PAYMENT_LIST
    ItemListContext.Container.PERSONAL_INFO_LIST -> AnyPage.ITEM_PERSONAL_INFO_LIST
    ItemListContext.Container.CREDENTIALS_LIST -> AnyPage.ITEM_CREDENTIAL_LIST
    ItemListContext.Container.SECURE_NOTE_LIST -> AnyPage.ITEM_SECURE_NOTE_LIST
    ItemListContext.Container.SEARCH -> AnyPage.SEARCH
    ItemListContext.Container.CSV_IMPORT,
    ItemListContext.Container.PASSWORD_HEALTH,
    ItemListContext.Container.SHARING,
    ItemListContext.Container.PASSKEYS_LIST,
    ItemListContext.Container.NONE -> null
}