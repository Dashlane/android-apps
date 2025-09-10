package com.dashlane.item.v3.itemtype.wifi.builder

import com.dashlane.item.v3.itemtype.wifi.WiFiFormData
import com.dashlane.item.v3.util.Field
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class WiFiHiddenFieldsBuilder @Inject constructor() {
    operator fun invoke(data: WiFiFormData): Set<Field> =
        setOf(
            Field.WIFI_PASSPHRASE.takeIf { data.isPassphraseEmpty },
            Field.WIFI_NOTE.takeIf { data.note.isBlank() },
        ).filterNotNull().toSet()
}