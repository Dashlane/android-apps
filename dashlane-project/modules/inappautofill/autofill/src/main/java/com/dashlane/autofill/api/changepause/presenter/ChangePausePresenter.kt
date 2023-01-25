package com.dashlane.autofill.api.changepause.presenter

import com.dashlane.autofill.api.changepause.ChangePauseContract
import com.dashlane.autofill.api.changepause.model.ChangePauseModel
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject



class ChangePausePresenter @Inject constructor(
    private val dataProvider: ChangePauseContract.DataProvider
) : ChangePauseContract.Presenter, ChangePauseContract.DataProvider.Responses {
    private var view: ChangePauseContract.View? = null
    private var viewCoroutineScope: CoroutineScope? = null

    init {
        dataProvider.bindResponses(this)
    }

    private var actionDoneAfterView: Boolean = false

    override fun setView(view: ChangePauseContract.View, viewCoroutineScope: CoroutineScope) {
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
                it.updatePause(actionsSourcesModel)
            } else {
                it.startLoading()
                dataProvider.loadPause()
            }
        }
    }

    override fun onTogglePause() {
        updateView {
            it.startLoading()
            dataProvider.togglePause()
        }
    }

    override fun openPauseDialog(autoFillFormSource: AutoFillFormSource) {
        updateView {
            it.stopLoading()
            it.openPauseDialog(autoFillFormSource)
        }
    }

    override fun resumedPause(pauseModel: ChangePauseModel) {
        updateView {
            it.stopLoading()
            it.resumeAutofill(pauseModel)
        }
    }

    override fun updatePause(pauseModel: ChangePauseModel) {
        updateView {
            it.stopLoading()
            it.updatePause(pauseModel)
        }
    }

    override fun errorOnLoadPause() {
        updateView {
            it.stopLoading()
        }
    }

    override fun errorOnTogglePause() {
        updateView {
            it.stopLoading()
            it.showErrorOnToggle()
        }
    }

    

    private fun updateView(block: suspend (ChangePauseContract.View) -> Unit = {}) {
        val view = this.view ?: return
        val viewScope = this.viewCoroutineScope ?: return

        viewScope.launch(Dispatchers.Main) {
            block(view)
        }
    }
}
