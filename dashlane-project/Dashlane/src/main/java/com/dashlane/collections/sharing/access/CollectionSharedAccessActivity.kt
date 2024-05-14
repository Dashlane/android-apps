package com.dashlane.collections.sharing.access

import android.os.Bundle
import androidx.activity.compose.setContent
import com.dashlane.R
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.SnackbarUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CollectionSharedAccessActivity : DashlaneActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DashlaneTheme {
                CollectionSharedAccessScreen(
                    userInteractionListener = object : UserInteractionListener {
                        override fun onCloseClicked() = finish()
                        override fun onRevokeFailed() {
                            showRevokeFailed()
                        }

                        override fun onRevokeSuccess() {
                            showRevokeSuccess()
                        }
                    }
                ).also {
                    totalMemberCountBeforeRevoke = 0
                }
            }
        }
    }

    private fun showRevokeSuccess() = SnackbarUtils.showSnackbar(
        this,
        getString(R.string.collection_shared_access_revoke_success_text)
    )

    private fun showRevokeFailed() = SnackbarUtils.showSnackbar(
        this,
        getString(R.string.collection_shared_access_revoke_error_text)
    )
}

internal interface UserInteractionListener {
    fun onCloseClicked()
    fun onRevokeSuccess()
    fun onRevokeFailed()
}