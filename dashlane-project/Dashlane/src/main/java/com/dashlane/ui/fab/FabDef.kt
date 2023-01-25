package com.dashlane.ui.fab

import android.os.Bundle
import android.view.View
import com.dashlane.ui.activities.fragments.vault.Filter
import com.skocken.presentation.definition.Base



interface FabDef {
    interface IDataProvider : Base.IDataProvider
    interface IView : Base.IView {
        fun toggleFABMenu(configureViewOnShow: () -> Unit)
        fun setFilter(filter: Filter)
        fun hideFABMenu(animate: Boolean)
        fun showFABMenu(configureView: () -> Unit, animate: Boolean)
        val isFabMenuHolderVisible: Boolean
    }

    interface IPresenter : Base.IPresenter {
        fun onViewCreated(view: View, savedInstanceState: Bundle?)
        fun onDestroyView()
        fun onBackPressedCaught(): Boolean
        fun setFilter(filter: Filter)
    }
}