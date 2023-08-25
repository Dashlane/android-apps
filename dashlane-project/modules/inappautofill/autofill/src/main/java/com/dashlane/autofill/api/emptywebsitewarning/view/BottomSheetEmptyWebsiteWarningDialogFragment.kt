package com.dashlane.autofill.api.emptywebsitewarning.view

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.emptywebsitewarning.EmptyWebsiteWarningContract
import com.dashlane.autofill.api.emptywebsitewarning.dagger.DaggerEmptyWebsiteWarningComponent
import com.dashlane.autofill.api.emptywebsitewarning.dagger.EmptyWebsiteWarningComponent
import com.dashlane.autofill.api.internal.AutofillApiComponent
import com.dashlane.ui.configureBottomSheet
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class BottomSheetEmptyWebsiteWarningDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var viewProxy: EmptyWebsiteWarningContract.ViewProxy

    @Inject
    lateinit var presenter: EmptyWebsiteWarningContract.Presenter

    @Inject
    lateinit var dataProvider: EmptyWebsiteWarningContract.DataProvider

    private lateinit var component: EmptyWebsiteWarningComponent

    private lateinit var website: String
    private lateinit var itemId: String

    private val hostActivity: EmptyWebsiteWarningActivity
        get() = (this.activity as EmptyWebsiteWarningActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initActivityComponent()
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Theme_Dashlane_Transparent_Cancelable)

        val arguments = arguments ?: return
        itemId = arguments.getString(ITEM_UID) ?: return
        website = arguments.getString(CURRENT_WEBSITE) ?: return
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return viewProxy.createView(inflater, container)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState) as BottomSheetDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as BottomSheetDialog).configureBottomSheet()
    }

    override fun onResume() {
        super.onResume()
        viewProxy.updateView(this.requireContext(), website, itemId)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        viewProxy.onCancel()
    }

    private fun initActivityComponent() {
        val application = this.activity?.application ?: return

        component =
            DaggerEmptyWebsiteWarningComponent.factory().create(
                hostActivity,
                AutofillApiComponent(application)
            )
        component.inject(this)
    }

    companion object {

        private const val ITEM_UID = "itemUid"
        private const val CURRENT_WEBSITE = "currentWebsite"

        fun create(website: String, itemId: String): BottomSheetEmptyWebsiteWarningDialogFragment =
            BottomSheetEmptyWebsiteWarningDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(CURRENT_WEBSITE, website)
                    putString(ITEM_UID, itemId)
                }
            }
    }
}
