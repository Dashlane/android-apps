package com.dashlane.autofill.api.unlinkaccount.presenter

import com.dashlane.autofill.api.unlinkaccount.UnlinkAccountsContract
import com.dashlane.autofill.api.unlinkaccount.model.LinkedAccountsModel
import com.dashlane.vault.summary.SummaryObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class UnlinkAccountsPresenter @Inject constructor(
    private val dataProvider: UnlinkAccountsContract.DataProvider
) : UnlinkAccountsContract.Presenter, UnlinkAccountsContract.DataProvider.Responses {
    private var view: UnlinkAccountsContract.View? = null
    private var viewCoroutineScope: CoroutineScope? = null

    init {
        dataProvider.bindResponses(this)
    }

    private var actionDoneAfterView: Boolean = false

    override fun setView(view: UnlinkAccountsContract.View, viewCoroutineScope: CoroutineScope) {
        this.view = view
        this.viewCoroutineScope = viewCoroutineScope
        actionDoneAfterView = false
    }

    override fun onResume() {
        updateView {
            val actionDoneAfterView = this.actionDoneAfterView
            this.actionDoneAfterView = true
            val actionsSourcesModel = dataProvider.currentState()

            if (actionsSourcesModel != null && !actionDoneAfterView) {
                if (actionsSourcesModel.processing) {
                    it.startLoading()
                } else {
                    it.stopLoading()
                }
                it.updateLinkedAccounts(actionsSourcesModel.linkedAccounts)
            } else {
                it.startLoading()
                dataProvider.loadLinkedAccounts()
            }
        }
    }

    override fun onRefresh() {
        updateView {
            it.startLoading()
            dataProvider.loadLinkedAccounts()
        }
    }

    override fun onLinkedAccountsItemClick(position: Int) {
        updateView {
            it.startLoading()
            dataProvider.unlinkAccount(position)
        }
    }

    override fun updateLinkedAccounts(linkedAccountsModel: LinkedAccountsModel) {
        updateView {
            if (linkedAccountsModel.processing) {
                it.startLoading()
            } else {
                it.stopLoading()
            }
            it.updateLinkedAccounts(linkedAccountsModel.linkedAccounts)
        }
    }

    override fun unlinkedAccount(account: SummaryObject.Authentifiant, linkedAccountsModel: LinkedAccountsModel) {
        updateView {
            it.unlinkAccount(account, linkedAccountsModel.autoFillFormSourceTitle)
            if (linkedAccountsModel.processing) {
                it.startLoading()
            } else {
                it.stopLoading()
            }
            it.updateLinkedAccounts(linkedAccountsModel.linkedAccounts)
        }
    }

    override fun errorOnLoadLinkedAccounts() {
        updateView {
            it.stopLoading()
            it.showErrorOnLoadLinkedAccounts()
        }
    }

    override fun errorOnUnlinkAccount() {
        updateView {
            it.stopLoading()
            it.showErrorOnUnlinkAccount()
        }
    }

    private fun updateView(block: suspend (UnlinkAccountsContract.View) -> Unit = {}) {
        val view = this.view ?: return
        val viewScope = this.viewCoroutineScope ?: return

        viewScope.launch(Dispatchers.Main) {
            block(view)
        }
    }
}
