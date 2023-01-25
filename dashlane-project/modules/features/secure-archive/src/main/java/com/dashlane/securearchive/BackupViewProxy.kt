package com.dashlane.securearchive

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.securearchive.databinding.ActivityBackupBinding
import com.dashlane.util.UriUtils
import com.dashlane.util.getThemeAttrColor
import com.dashlane.util.setCurrentPageView
import kotlinx.coroutines.flow.collect
import java.io.File

internal class BackupViewProxy(
    private val activity: AppCompatActivity,
    private val binding: ActivityBackupBinding,
    private val viewModel: BackupViewModelContract
) {
    init {
        when (val operation = viewModel.operation) {
            is BackupOperation.Import -> {
                activity.setCurrentPageView(AnyPage.IMPORT_BACKUPFILE)
                configureViewForImport(operation.uri)
            }
            is BackupOperation.Export -> configureViewForExport()
        }

        binding.primaryCta.setOnClickListener { viewModel.onBackupLaunch(binding.editText.text.toString()) }
        binding.secondaryCta.setOnClickListener { viewModel.onBackupCancel() }
        binding.editText.run {
            doAfterTextChanged {
                viewModel.onPasswordChanged()
            }
            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        viewModel.onBackupLaunch(binding.editText.text.toString())
                        true
                    }
                    else -> false
                }
            }

            requestFocus()
        }

        activity.lifecycleScope.launchWhenStarted {
            viewModel.state.collect {
                setState(it)
            }
        }
    }

    private fun setState(state: BackupViewState) {
        when (state) {
            BackupViewState.Idle -> {
                binding.inputLayout.error = null
                showProgress(false)
            }
            BackupViewState.Processing -> showProgress(true)
            is BackupViewState.Success -> {
                showProgress(false)
                setResultOkAndFinish(
                    isSuccessful = true,
                    count = state.count,
                    isShared = false
                )
            }
            BackupViewState.InvalidPasswordError -> {
                showProgress(false)
                showInputError()
            }
            is BackupViewState.UnhandledError -> {
                showProgress(false)
                setResultOkAndFinish(isSuccessful = false)
            }
            is BackupViewState.FallbackToFileIntent -> {
                showProgress(false)
                val intent = getIntentToShareArchive(activity, state.cacheFile)
                activity.startActivity(
                    Intent.createChooser(
                        intent,
                        activity.resources.getString(
                            R.string.backup_export_sharing_fallback_title
                        )
                    )
                )

                setResultOkAndFinish(
                    isSuccessful = true,
                    count = state.count,
                    isShared = true
                )
            }
            BackupViewState.Cancelled -> {
                activity.setResult(Activity.RESULT_CANCELED)
                activity.finish()
            }
        }
    }

    private fun showProgress(shown: Boolean) {
        binding.primaryCta.isEnabled = !shown
        binding.primaryCta.setTextColor(
            if (shown) Color.TRANSPARENT else activity.getThemeAttrColor(
                R.attr.colorOnSecondary
            )
        )
        binding.editText.isEnabled = !shown
        binding.progress.visibility = if (shown) View.VISIBLE else View.GONE
    }

    private fun showInputError() {
        binding.inputLayout.error = activity.getString(R.string.backup_failure_invalid_password)
    }

    private fun getIntentToShareArchive(context: Context, file: File): Intent {
        val contentUri: Uri = FileProvider.getUriForFile(context, "com.dashlane.fileprovider", file)

        return Intent().apply {
            action = Intent.ACTION_SEND
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(contentUri, context.contentResolver.getType(contentUri))
            putExtra(Intent.EXTRA_STREAM, contentUri)
        }
    }

    private fun setResultOkAndFinish(
        isSuccessful: Boolean,
        count: Int = 0,
        isShared: Boolean = false
    ) {
        val data = BackupActivityIntents.newData(activity.intent, isSuccessful, isShared, count)
        activity.setResult(Activity.RESULT_OK, data)
        activity.finish()
    }

    private fun configureViewForExport() = configureView(
        title = activity.getString(R.string.backup_export_title),
        message = null,
        primaryCta = activity.getString(R.string.backup_export_cta_primary),
        secondaryCta = activity.getString(R.string.backup_export_cta_secondary),
    )

    private fun configureViewForImport(uri: Uri) = configureView(
        title = activity.getString(
            R.string.backup_import_title,
            UriUtils.getFileDisplayName(activity, uri).orEmpty()
        ),
        message = activity.getString(R.string.backup_import_message),
        primaryCta = activity.getString(R.string.backup_import_cta_primary),
        secondaryCta = activity.getString(R.string.backup_import_cta_secondary),
    )

    private fun configureView(
        title: String,
        message: String?,
        primaryCta: String,
        secondaryCta: String
    ) {
        this.binding.title.text = title
        if (message != null) {
            this.binding.message.text = message
            this.binding.message.visibility = View.VISIBLE
        }
        this.binding.primaryCta.text = primaryCta
        this.binding.secondaryCta.text = secondaryCta
    }
}