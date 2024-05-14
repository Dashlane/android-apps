package com.dashlane.autofill.changepause.presenter

import com.dashlane.autofill.changepause.ChangePauseContract
import com.dashlane.autofill.changepause.model.ChangePauseModel
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.util.inject.qualifiers.FragmentLifecycleCoroutineScope
import com.dashlane.util.inject.qualifiers.MainCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangePausePresenter @Inject constructor(
    private val dataProvider: ChangePauseContract.DataProvider,
    @FragmentLifecycleCoroutineScope
    private val coroutineScope: CoroutineScope,
    @MainCoroutineDispatcher
    private val mainDispatcher: CoroutineDispatcher
) : ChangePauseContract.Presenter, ChangePauseContract.DataProvider.Responses {
    private var view: ChangePauseContract.View? = null

    init {
        dataProvider.bindResponses(this)
    }

    private var actionDoneAfterView: Boolean = false

    override fun setView(view: ChangePauseContract.View) {
        this.view = view
        actionDoneAfterView = false
    }

    override fun onResume(autoFillFormSource: AutoFillFormSource) {
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
                dataProvider.loadPause(autoFillFormSource)
            }
        }
    }

    override fun onTogglePause(autoFillFormSource: AutoFillFormSource) {
        updateView {
            it.startLoading()
            dataProvider.togglePause(autoFillFormSource)
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
        coroutineScope.launch(mainDispatcher) {
            block(view)
        }
    }
}
