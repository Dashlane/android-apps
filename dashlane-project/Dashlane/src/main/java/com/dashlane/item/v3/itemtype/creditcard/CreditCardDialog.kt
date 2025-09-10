package com.dashlane.item.v3.itemtype.creditcard

import com.dashlane.item.v3.viewmodels.ItemEditDialog

sealed class CreditCardDialog : ItemEditDialog {
    data object NfcReaderSucceed : CreditCardDialog()

    data object NfcReaderFailed : CreditCardDialog()
}