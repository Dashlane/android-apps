package com.dashlane.ui.dialogs.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.dashlane.announcements.AnnouncementCenter
import com.dashlane.databinding.ProgressDialogBinding
import dagger.hilt.android.AndroidEntryPoint
import java.lang.ref.WeakReference
import javax.inject.Inject

@AndroidEntryPoint
class WaiterDialogFragment : DialogFragment() {

    @Inject
    lateinit var announcementCenter: AnnouncementCenter

    private lateinit var progressDialogBinding: ProgressDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        progressDialogBinding = ProgressDialogBinding.inflate(inflater, container, false)
        progressDialogBinding.question.text = arguments?.getString(ARGS_DESCRIPTION)
        return progressDialogBinding.root
    }

    override fun onResume() {
        super.onResume()
        
        announcementCenter.disable()
    }

    override fun onStop() {
        super.onStop()
        
        announcementCenter.restorePreviousStateIfDisabled()
    }

    fun updateWaiterDescription(message: String?) {
        progressDialogBinding.question.text = message
    }

    override fun onDestroyView() {
        dialog?.setDismissMessage(null)
        super.onDestroyView()
    }

    companion object {
        private var lastDialog = WeakReference<WaiterDialogFragment>(null)
        private val TAG: String = WaiterDialogFragment::class.java.name
        private const val ARGS_TITLE = "ARGS_TITLE"
        private const val ARGS_DESCRIPTION = "ARGS_DESCRIPTION"

        private fun newInstance(title: String, question: String): WaiterDialogFragment {
            val fragment = WaiterDialogFragment()
            val args = Bundle()
            args.putString(ARGS_TITLE, title)
            args.putString(ARGS_DESCRIPTION, question)
            fragment.arguments = args
            return fragment
        }

        fun showWaiter(cancelable: Boolean, title: String, question: String, fm: FragmentManager) {
            var dialog = fm.findFragmentByTag(TAG) as WaiterDialogFragment?
            if (dialog == null) {
                dialog = newInstance(title, question)
                dialog.isCancelable = cancelable
                dialog.show(fm, TAG)
                lastDialog = WeakReference<WaiterDialogFragment>(dialog)
            }
        }

        fun dismissWaiter(fm: FragmentManager) {
            val dialog = fm.findFragmentByTag(TAG) as WaiterDialogFragment?
            if (dialog != null) {
                dialog.dismiss()
            } else {
                lastDialog.get()?.dismiss()
            }
            lastDialog = WeakReference(null)
        }

        fun updateWaiterDescription(fm: FragmentManager, cancelable: Boolean, message: String?) {
            val dialog = fm.findFragmentByTag(TAG) as WaiterDialogFragment?
            if (dialog != null) {
                dialog.isCancelable = cancelable
                dialog.updateWaiterDescription(message)
            }
        }
    }
}