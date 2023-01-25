package com.dashlane.autofill.api.pause.dagger

import com.dashlane.autofill.api.pause.AskPauseContract
import com.dashlane.autofill.api.pause.view.BottomSheetAskPauseDialogFragment
import com.dashlane.autofill.api.pause.view.BottomSheetAskPauseViewProxy
import com.dashlane.util.Toaster
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Named



@Module
@InstallIn(ActivityComponent::class)
class BottomSheetAskPauseDialogFragmentModule {
    @BottomSheetAskPauseDialogFragmentScope
    @Provides
    fun providesBottomSheetFormSourcePauseViewProxy(
        bottomSheetAskPauseDialogFragment: BottomSheetAskPauseDialogFragment,
        presenter: AskPauseContract.Presenter,
        toaster: Toaster,
        @Named("openInDashlane") openInDashlane: Boolean
    ): BottomSheetAskPauseViewProxy {
        val viewProxy = BottomSheetAskPauseViewProxy(bottomSheetAskPauseDialogFragment, presenter, toaster, openInDashlane)
        presenter.setView(viewProxy, bottomSheetAskPauseDialogFragment.activity as CoroutineScope)
        return viewProxy
    }
}
