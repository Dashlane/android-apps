package com.dashlane.ui.activities.fragments.list.wrapper

import com.dashlane.vault.summary.SummaryObject

open class VaultItemDoubleWrapper<D : SummaryObject>(val originalItemWrapper: VaultItemWrapper<D>) :
    VaultItemWrapper<D> by originalItemWrapper