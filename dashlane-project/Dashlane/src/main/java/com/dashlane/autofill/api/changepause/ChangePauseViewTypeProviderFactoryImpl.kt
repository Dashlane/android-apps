package com.dashlane.autofill.api.changepause

import com.dashlane.R
import com.dashlane.autofill.api.changepause.model.ChangePauseModel
import com.dashlane.autofill.api.changepause.view.ChangePauseViewTypeProviderFactory
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import javax.inject.Inject

class ChangePauseViewTypeProviderFactoryImpl @Inject constructor() : ChangePauseViewTypeProviderFactory {

    override fun create(pauseModel: ChangePauseModel): ChangePauseViewTypeProviderFactory.PauseSetting {
        return Wrapper(pauseModel.subtitle, pauseModel.isPaused)
    }

    private class Wrapper(
        override val pauseUntilString: String,
        override val isPaused: Boolean
    ) : ChangePauseViewTypeProviderFactory.PauseSetting {

        override fun getViewType(): DashlaneRecyclerAdapter.ViewType<*> {
            return DashlaneRecyclerAdapter.ViewType(
                R.layout.list_item_setting_checkbox,
                ChangePauseHolder::class.java
            )
        }
    }
}
