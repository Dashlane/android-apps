package com.dashlane.autofill.api.changepause

import com.dashlane.autofill.api.changepause.model.ChangePauseModel
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import kotlinx.coroutines.CoroutineScope

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
        fun setView(view: View, viewCoroutineScope: CoroutineScope)
        fun onResume()
        fun onTogglePause()
    }

    

    interface DataProvider {
        fun bindResponses(responses: Responses)
        fun currentState(): ChangePauseModel?
        fun loadPause()
        fun togglePause()

        interface Responses {
            fun openPauseDialog(autoFillFormSource: AutoFillFormSource)
            fun updatePause(pauseModel: ChangePauseModel)
            fun resumedPause(pauseModel: ChangePauseModel)
            fun errorOnLoadPause()
            fun errorOnTogglePause()
        }
    }
}
