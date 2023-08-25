package com.dashlane.autofill.api.unlinkaccount.dagger

import androidx.lifecycle.lifecycleScope
import com.dashlane.autofill.api.unlinkaccount.UnlinkAccountsContract
import com.dashlane.autofill.api.unlinkaccount.view.LinkedAccountViewTypeProviderFactory
import com.dashlane.autofill.api.unlinkaccount.view.UnlinkAccountsFragment
import com.dashlane.autofill.api.unlinkaccount.view.UnlinkAccountsViewProxy
import com.dashlane.util.Toaster
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
class UnlinkAccountsFragmentModule {
    @UnlinkAccountsFragmentScope
    @Provides
    fun providesUnlinkAccountsViewProxy(
        fragment: UnlinkAccountsFragment,
        presenter: UnlinkAccountsContract.Presenter,
        toaster: Toaster,
        linkedAccountViewTypeProviderFactory: LinkedAccountViewTypeProviderFactory
    ): UnlinkAccountsViewProxy {
        val viewProxy = UnlinkAccountsViewProxy(
            fragment,
            presenter,
            toaster,
            linkedAccountViewTypeProviderFactory
        )
        presenter.setView(viewProxy, fragment.lifecycleScope)
        return viewProxy
    }
}
