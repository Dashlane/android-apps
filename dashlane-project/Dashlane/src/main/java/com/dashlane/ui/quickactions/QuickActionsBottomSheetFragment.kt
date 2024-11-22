package com.dashlane.ui.quickactions

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dashlane.R
import com.dashlane.events.AppEvents
import com.dashlane.lock.LockManager
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.ui.ExpandedBottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class QuickActionsBottomSheetFragment : ExpandedBottomSheetDialogFragment() {

    @Inject
    lateinit var genericDataQuery: GenericDataQuery

    @Inject
    lateinit var dataProvider: QuickActionsDataProvider

    @Inject
    lateinit var quickActionsLogger: QuickActionsLogger

    @Inject
    lateinit var lockManager: LockManager

    @Inject
    lateinit var appEvents: AppEvents

    private var originPage: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_quick_actions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemId = QuickActionsBottomSheetFragmentArgs.fromBundle(requireArguments()).itemId
        val itemListContext = QuickActionsBottomSheetFragmentArgs.fromBundle(requireArguments()).itemListContext
        originPage = QuickActionsBottomSheetFragmentArgs.fromBundle(requireArguments()).originPage

        val presenter = QuickActionsPresenter()
        presenter.setProvider(dataProvider)
        val summary = dataProvider.getVaultItem(itemId)
        if (summary == null) {
            dismiss()
        } else {
            val viewProxy = QuickActionsViewProxy(
                fragment = this,
                item = summary,
                quickActionsLogger = quickActionsLogger,
                itemListContext = itemListContext,
                lockManager = lockManager,
                dataQuery = genericDataQuery,
                originPage = originPage
            )
            viewProxy.setPresenter(presenter)
            presenter.setView(viewProxy)

            
            presenter.getActions(itemId, itemListContext)
            presenter.getItemDetails(itemId)
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        quickActionsLogger.logCloseQuickActions(originPage)
    }
}