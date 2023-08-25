package com.dashlane.autofill.api.viewallaccounts.presenter

import com.dashlane.autofill.api.viewallaccounts.AutofillApiViewAllAccountsContract
import com.dashlane.autofill.api.viewallaccounts.model.AuthentifiantsSearchAndFilterDataProvider
import com.dashlane.autofill.api.viewallaccounts.view.AuthentifiantSearchViewTypeProviderFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthentifiantsSearchAndFilterPresenter(
    private val dataProvider: AuthentifiantsSearchAndFilterDataProvider,
    private val mainCoroutineDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val backgroundCoroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AutofillApiViewAllAccountsContract.Presenter {

    private var view: AutofillApiViewAllAccountsContract.View? = null
    private var viewCoroutineScope: CoroutineScope? = null
    private var isLoading = false

    override fun setView(
        view: AutofillApiViewAllAccountsContract.View,
        viewCoroutineScope: CoroutineScope
    ) {
        this.view = view
        this.viewCoroutineScope = viewCoroutineScope
    }

    override fun filterAuthentifiants(query: String) {
        val view = this.view ?: return
        view.showLoading()
        if (!isLoading) {
            isLoading = true
            loadAndUpdateAuthentifiants(query)
            isLoading = false
        }
    }

    override fun selectedAuthentifiant(
        wrapperItem: AuthentifiantSearchViewTypeProviderFactory.AuthentifiantWrapperItem
    ) {
        val view = this.view ?: return

        val result = try {
            dataProvider.getAuthentifiant(wrapperItem.getAuthentifiant().id)
        } catch (e: AuthentifiantsSearchAndFilterDataProvider.LoadAuthentifiantsError) {
            view.onError()
            return
        }
        view.onSelected(result, wrapperItem.itemListContext)
    }

    override fun noSelection() {
        val view = this.view ?: return
        view.onNothingSelected()
    }

    private fun loadAndUpdateAuthentifiants(query: String?) {
        val coroutineScope = viewCoroutineScope ?: return
        val view = this.view ?: return
        coroutineScope.launch(mainCoroutineDispatcher) {
            try {
                val result = withContext(backgroundCoroutineDispatcher) {
                    dataProvider.getMatchedAuthentifiantsFromQuery(query)
                }
                view.hideLoading()
                view.onUpdateAuthentifiants(result, query)
            } catch (e: AuthentifiantsSearchAndFilterDataProvider.LoadAuthentifiantsError) {
                showOnError(view)
            } catch (e: AuthentifiantsSearchAndFilterDataProvider.FilterAuthentifiantsError) {
                showOnError(view)
            }
        }
    }

    private fun showOnError(view: AutofillApiViewAllAccountsContract.View) {
        view.hideLoading()
        view.onError()
    }
}
