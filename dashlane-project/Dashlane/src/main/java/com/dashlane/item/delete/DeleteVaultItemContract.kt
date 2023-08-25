package com.dashlane.item.delete

import com.skocken.presentation.definition.Base

interface DeleteVaultItemContract {
    interface View : Base.IView {
        fun itemDeleted()
        fun deleteError()
    }

    interface Presenter : Base.IPresenter {
        fun deleteItem(itemId: String)
    }

    interface DataProvider : Base.IDataProvider {

        suspend fun deleteItem(itemId: String): Boolean
    }

    interface Logger {
        fun logItemDeletionConfirmed()
        fun logItemDeletionCanceled()
    }
}