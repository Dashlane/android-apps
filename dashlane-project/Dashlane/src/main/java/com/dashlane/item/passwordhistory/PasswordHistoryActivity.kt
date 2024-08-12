package com.dashlane.item.passwordhistory

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.R
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.item.passwordhistory.PasswordHistoryViewModel.PasswordHistoryState
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.clipboard.ClipboardCopy
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@Deprecated("This version of password history is deprecated, please check with the team before any modification.")
@AndroidEntryPoint
class PasswordHistoryActivity : DashlaneActivity() {
    val viewModel by viewModels<PasswordHistoryViewModel>()

    @Inject
    lateinit var clipboardCopy: ClipboardCopy

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uid = intent.getStringExtra(ITEM_UID_EXTRA)!!

        setContent {
            val uiState by viewModel.historyState.collectAsStateWithLifecycle()

            LaunchedEffect(key1 = uiState) {
                when (uiState) {
                    PasswordHistoryState.Init -> viewModel.reloadForItemUid(uid = uid)
                    PasswordHistoryState.Error -> finishWithoutRevert(isError = true)
                    PasswordHistoryState.Success -> {} 
                    is PasswordHistoryState.Loaded -> Unit
                }
            }

            DashlaneTheme {
                PasswordHistoryScreenOld(
                    state = uiState,
                    onRevertClick = { selectedPassword ->
                        viewModel.restorePassword(
                            vaultItemUid = uid,
                            selectedPasswordHistory = selectedPassword
                        )
                        finishWithResult()
                        true
                    },
                    onCopyClick = {
                        clipboardCopy.copyToClipboard(
                            data = it.password,
                            sensitiveData = true,
                            feedback = R.string.feedback_copy_password
                        )
                        true
                    },
                    onBackNavigationClick = { finishWithoutRevert(isError = false) },
                )
            }
        }
    }

    private fun finishWithoutRevert(isError: Boolean) {
        val resultIntent = if (isError) {
            Intent().apply { putExtra(FINISHED_WITH_ERROR_EXTRA, true) }
        } else {
            null
        }
        setResult(Activity.RESULT_CANCELED, resultIntent)
        finishActivity(REQUEST_CODE)
        finish()
    }

    private fun finishWithResult() {
        setResult(Activity.RESULT_OK)
        finishActivity(REQUEST_CODE)
        finish()
    }

    companion object {
        const val FINISHED_WITH_ERROR_EXTRA: String = "finished_with_error"
        const val ITEM_UID_EXTRA = "item_uid"
        const val REQUEST_CODE = 89623954
    }
}