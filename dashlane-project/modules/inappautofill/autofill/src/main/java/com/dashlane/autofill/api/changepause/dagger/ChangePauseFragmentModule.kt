package com.dashlane.autofill.api.changepause.dagger

import androidx.lifecycle.lifecycleScope
import com.dashlane.autofill.api.changepause.ChangePauseContract
import com.dashlane.autofill.api.changepause.view.ChangePauseFragment
import com.dashlane.autofill.api.changepause.view.ChangePauseViewProxy
import com.dashlane.autofill.api.changepause.view.ChangePauseViewTypeProviderFactory
import com.dashlane.util.Toaster
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent



@Module
@InstallIn(ActivityComponent::class)
class ChangePauseFragmentModule {
    @ChangePauseFragmentScope
    @Provides
    fun providesChangePauseViewProxy(
        fragment: ChangePauseFragment,
        presenter: ChangePauseContract.Presenter,
        changePauseViewTypeProviderFactory: ChangePauseViewTypeProviderFactory,
        toaster: Toaster
    ): ChangePauseViewProxy {
        val viewProxy = ChangePauseViewProxy(
            fragment,
            presenter,
            changePauseViewTypeProviderFactory,
            toaster
        )
        presenter.setView(viewProxy, fragment.lifecycleScope)
        return viewProxy
    }
}
