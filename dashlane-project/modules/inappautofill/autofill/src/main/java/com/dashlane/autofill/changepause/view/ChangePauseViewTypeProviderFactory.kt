package com.dashlane.autofill.changepause.view

import com.dashlane.autofill.changepause.model.ChangePauseModel
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter

interface ChangePauseViewTypeProviderFactory {
    fun create(pauseModel: ChangePauseModel): PauseSetting
    interface PauseSetting : DashlaneRecyclerAdapter.ViewTypeProvider {
        val pauseUntilString: String
        val isPaused: Boolean
    }
}