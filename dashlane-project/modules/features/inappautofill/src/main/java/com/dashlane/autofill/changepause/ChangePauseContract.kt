package com.dashlane.autofill.changepause

import com.dashlane.autofill.changepause.model.ChangePauseModel
import com.dashlane.autofill.formdetector.model.AutoFillFormSource

interface ChangePauseContract {

    interface View {
        fun updatePause(pauseModel: ChangePauseModel)
        fun openPauseDialog(autoFillFormSource: AutoFillFormSource)
        fun resumeAutofill(pauseModel: ChangePauseModel)
        fun showErrorOnToggle()

        fun startLoading()
        fun stopLoading()
    }

    interface Presenter {
        fun setView(view: View)
        fun onResume(autoFillFormSource: AutoFillFormSource)
        fun onTogglePause(autoFillFormSource: AutoFillFormSource)
    }

    interface DataProvider {
        fun bindResponses(responses: Responses)
        fun currentState(): ChangePauseModel?
        fun loadPause(autoFillFormSource: AutoFillFormSource)
        fun togglePause(autoFillFormSource: AutoFillFormSource)

        interface Responses {
            fun openPauseDialog(autoFillFormSource: AutoFillFormSource)
            fun updatePause(pauseModel: ChangePauseModel)
            fun resumedPause(pauseModel: ChangePauseModel)
            fun errorOnLoadPause()
            fun errorOnTogglePause()
        }
    }
}
