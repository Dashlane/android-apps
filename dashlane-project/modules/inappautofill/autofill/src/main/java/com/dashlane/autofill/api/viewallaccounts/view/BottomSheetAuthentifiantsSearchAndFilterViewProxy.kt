package com.dashlane.autofill.api.viewallaccounts.view

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.navigation.getAutofillBottomSheetNavigator
import com.dashlane.autofill.api.viewallaccounts.AutofillApiViewAllAccountsContract
import com.dashlane.autofill.api.viewallaccounts.presenter.AuthentifiantsSearchAndFilterPresenter
import com.dashlane.search.MatchedSearchResult
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.util.DeviceUtils
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.skocken.efficientadapter.lib.adapter.EfficientAdapter
import javax.inject.Inject



class BottomSheetAuthentifiantsSearchAndFilterViewProxy @Inject constructor(
    private val bottomSheetDialogFragment: BottomSheetAuthentifiantsSearchAndFilterDialogFragment,
    private val authentifiantSearchViewTypeProviderFactory: AuthentifiantSearchViewTypeProviderFactory,
    private val presenter: AuthentifiantsSearchAndFilterPresenter
) : AutofillApiViewAllAccountsContract.View,
    TextWatcher {

    private var progressBar: ProgressBar? = null
    private var listView: RecyclerView? = null
    private var searchText: TextInputEditText? = null
    private var fab: FloatingActionButton? = null

    private lateinit var heightPeekExpanded: Pair<Int?, Int?>

    private val authentifiantClickListener =
        EfficientAdapter.OnItemClickListener<DashlaneRecyclerAdapter.ViewTypeProvider> { _, _, item, _ ->
            item?.let {
                it as AuthentifiantSearchViewTypeProviderFactory.AuthentifiantWrapperItem
                presenter.selectedAuthentifiant(it)
            }
        }
    private val adapter = DashlaneRecyclerAdapter<DashlaneRecyclerAdapter.ViewTypeProvider>()

    fun createView(inflater: LayoutInflater, container: ViewGroup?): View? {
        val contentView =
            inflater.cloneInContext(bottomSheetDialogFragment.requireActivity()).inflate(
                R.layout.bottom_sheet_authentifiant_list_dialog_fragment, container,
                false
            )
        setView(contentView)

        return contentView
    }

    @SuppressWarnings("findbugs:RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    private fun setView(contentView: View) {
        fab = contentView.findViewById(R.id.fab) as? FloatingActionButton
        searchText =
            contentView.findViewById(R.id.bottom_sheet_authentifiant_search) as? TextInputEditText
        listView =
            contentView.findViewById(R.id.bottom_sheet_authentifiant_listview) as? RecyclerView
        progressBar = contentView.findViewById(R.id.view_progress) as? ProgressBar

        adapter.onItemClickListener = authentifiantClickListener
        listView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == SCROLL_STATE_DRAGGING) {
                    DeviceUtils.hideKeyboard(recyclerView)
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                
            }
        })

        heightPeekExpanded = getHeightForPeek() to getHeightForExpanded()

        setupViewHeight(contentView)

        val linearLayoutManager = GridLayoutManager(bottomSheetDialogFragment.context, 1)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        listView?.layoutManager = linearLayoutManager
        listView?.adapter = adapter
        searchText?.addTextChangedListener(this)
        fab?.setOnClickListener {
            val bottomSheetNavigator = bottomSheetDialogFragment.getAutofillBottomSheetNavigator()
            getBottomSheetAuthentifiantListDialogResponse()?.onNavigateToCreateAuthentifiant(
                bottomSheetNavigator
            )
        }
    }

    private fun setupViewHeight(view: View) {
        heightPeekExpanded.second?.let { eh ->
            val layoutParams =
                ConstraintLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, eh)
            view.layoutParams = layoutParams
        }
    }

    private fun getHeightForExpanded(): Int? {
        return bottomSheetDialogFragment.activity?.resources?.displayMetrics?.heightPixels?.let { (it / 4) * 3 }
    }

    private fun getHeightForPeek(): Int? {
        return bottomSheetDialogFragment.activity?.resources?.displayMetrics?.heightPixels?.let { (it / 3) * 1 }
    }

    fun loadContent() {
        presenter.filterAuthentifiants(getQuery())
    }

    fun cancel() {
        presenter.noSelection()
    }

    override fun afterTextChanged(text: Editable?) {
        presenter.filterAuthentifiants(text?.toString() ?: "")
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        
    }

    private fun populateItems(searchResult: List<MatchedSearchResult>, query: String?) {
        val totalCount = searchResult.size
        val baseListContext = ItemListContext(
            container = ItemListContext.Container.SEARCH,
            section = ItemListContext.Section.SEARCH_RESULT
        )
        adapter.populateItems(
            searchResult.mapIndexed { index, item ->
                authentifiantSearchViewTypeProviderFactory.create(
                    item,
                    baseListContext.copy(position = index, count = totalCount),
                    query
                )
            }
        )
        listView?.scrollToPosition(0)
    }

    override fun onNothingSelected() {
        getBottomSheetAuthentifiantListDialogResponse()?.onAuthentifiantDialogResponse(
            authentifiant = null,
            itemListContext = ItemListContext(sectionCount = adapter.itemCount),
            searchQuery = getQuery()
        )
    }

    override fun onSelected(
        authentifiant: VaultItem<SyncObject.Authentifiant>,
        itemListContext: ItemListContext
    ) {
        getBottomSheetAuthentifiantListDialogResponse()?.onAuthentifiantDialogResponse(
            authentifiant,
            itemListContext,
            searchQuery = getQuery()
        )
    }

    private fun getBottomSheetAuthentifiantListDialogResponse(): SearchAuthentifiantDialogResponse? {
        return bottomSheetDialogFragment.activity as? SearchAuthentifiantDialogResponse
    }

    override fun getQuery(): String {
        return searchText?.text?.toString() ?: ""
    }

    override fun onUpdateAuthentifiants(authentifiants: List<MatchedSearchResult>, query: String?) {
        populateItems(authentifiants, query)
    }

    override fun onError() {
        val authentifiants: List<MatchedSearchResult> = emptyList()
        populateItems(authentifiants, null)
    }

    override fun showLoading() {
        listView?.visibility = View.INVISIBLE
        progressBar?.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        listView?.visibility = View.VISIBLE
        progressBar?.visibility = View.GONE
    }
}
