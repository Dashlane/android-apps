package com.dashlane.item.delete

import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DeleteVaultItemPresenter :
    BasePresenter<DeleteVaultItemContract.DataProvider, DeleteVaultItemContract.View>(),
    DeleteVaultItemContract.Presenter {

    lateinit var coroutineScope: CoroutineScope

    override fun deleteItem(itemId: String) {
        coroutineScope.launch {
            if (provider.deleteItem(itemId)) {
                view.itemDeleted()
            } else {
                view.deleteError()
            }
        }
    }
}