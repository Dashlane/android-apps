package com.dashlane.autofill.pause.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dashlane.autofill.api.R
import com.dashlane.autofill.pause.AskPauseContract
import com.dashlane.autofill.pause.model.PauseDurations
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.ui.BottomSheetHeightConfig
import com.dashlane.ui.bottomSheetDialog
import com.dashlane.ui.configureBottomSheet
import com.dashlane.util.Toaster

class BottomSheetAskPauseViewProxy(
    private val bottomSheetDialogFragment: BottomSheetAskPauseDialogFragment,
    private val presenter: AskPauseContract.Presenter,
    private val toaster: Toaster,
    private val openInDashlane: Boolean
) : AskPauseContract.View {

    private lateinit var pauseTitleLogo: ImageView
    private lateinit var pauseTitle: AppCompatTextView
    private lateinit var pauseForOneHourButton: Button
    private lateinit var pauseForOneDayButton: Button
    private lateinit var pausePermanentButton: Button

    fun createView(inflater: LayoutInflater, container: ViewGroup?): View? {
        val contentView = inflater.inflate(
            R.layout.bottom_sheet_form_source_pause_dialog_fragment,
            container,
            false
        )
        setView(contentView)

        return contentView
    }

    fun onDialogCreated() {
        bottomSheetDialogFragment.bottomSheetDialog?.configureBottomSheet(
            BottomSheetHeightConfig(HEIGHT_RATIO, HEIGHT_RATIO)
        )
    }

    private fun setView(contentView: View) {
        pauseTitleLogo = contentView.findViewById(R.id.dashlogo)
        pauseTitle = contentView.findViewById(R.id.title) as AppCompatTextView
        pauseForOneHourButton = contentView.findViewById(R.id.pause_for_one_hour) as Button
        pauseForOneDayButton = contentView.findViewById(R.id.pause_for_one_day) as Button
        pausePermanentButton = contentView.findViewById(R.id.pause_permanent) as Button

        if (openInDashlane) {
            pauseTitleLogo.visibility = View.GONE
        } else {
            pauseTitleLogo.visibility = View.VISIBLE
        }
        pauseForOneHourButton.setOnClickListener {
            getPausedFormSource()?.let {
                presenter.onOneHourPauseButtonClick(it)
            }
        }
        pauseForOneDayButton.setOnClickListener {
            getPausedFormSource()?.let {
                presenter.onOneDayPauseButtonClick(it)
            }
        }
        pausePermanentButton.setOnClickListener {
            getPausedFormSource()?.let {
                presenter.onPermanentPauseButtonClick(it)
            }
        }

        val linearLayoutManager = GridLayoutManager(bottomSheetDialogFragment.context, 1)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
    }

    override fun showPauseTitle(title: String) {
        pauseTitle.text = title
    }

    override fun showPauseMessage(
        message: String,
        pauseDurations: PauseDurations
    ) {
        toaster.show(message, Toast.LENGTH_SHORT)
        bottomSheetDialogFragment.dismiss()
        getFormSourceChoosePauseDialogResponse()?.onPauseFormSourceDialogResponse(pauseDurations)
    }

    override fun showPauseErrorMessage() {
        toaster.show(R.string.autofill_do_not_show_again_pause_error_message, Toast.LENGTH_SHORT)
        bottomSheetDialogFragment.dismiss()
        getFormSourceChoosePauseDialogResponse()?.onPauseFormSourceDialogResponse(null)
    }

    private fun getFormSourceChoosePauseDialogResponse(): AskPauseDialogContract? {
        return bottomSheetDialogFragment.activity as? AskPauseDialogContract
    }

    fun onResume() {
        val pausedFormSource =
            getFormSourceChoosePauseDialogResponse()?.getPausedFormSource() ?: return
        presenter.onResume(pausedFormSource, openInDashlane)
    }

    fun onCancel() {
        getFormSourceChoosePauseDialogResponse()?.onPauseFormSourceDialogResponse(null)
    }

    private fun getPausedFormSource(): AutoFillFormSource? {
        return getFormSourceChoosePauseDialogResponse()?.getPausedFormSource()
    }

    companion object {
        private const val HEIGHT_RATIO: Float = 3F / 7F
    }
}
