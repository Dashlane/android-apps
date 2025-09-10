package com.dashlane.ui.screens.fragments.userdata.sharing.itemselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockPrompt
import com.dashlane.sharing.itemselection.NewShareItemSelectionScreen
import com.dashlane.sharing.itemselection.NewShareItemSelectionViewModel.Companion.UNLOCK_EVENT_CODE
import com.dashlane.ui.activities.fragments.AbstractContentFragment

class NewShareItemSelectionFragment : AbstractContentFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                DashlaneTheme {
                    NewShareItemSelectionScreen(
                        onLockRequest = {
                            lockManager.showLockActivityForReason(
                                context = context,
                                reason = LockEvent.Unlock.Reason.WithCode(UNLOCK_EVENT_CODE, null),
                                lockPrompt = LockPrompt.ForSharing,
                            )
                        },
                        goToNewShare = { selectedPasswords, selectedNotes, selectedSecrets ->
                            navigator.goToPeopleSelectionFromNewShareItemSelection(
                                selectedPasswords = selectedPasswords,
                                selectedNotes = selectedNotes,
                                selectedSecrets = selectedSecrets,
                            )
                        },
                    )
                }
            }
        }
    }
}