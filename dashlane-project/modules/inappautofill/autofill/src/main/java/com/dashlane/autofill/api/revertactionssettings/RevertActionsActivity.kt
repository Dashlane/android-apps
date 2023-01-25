package com.dashlane.autofill.api.revertactionssettings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.changepause.AutofillApiChangePauseComponent
import com.dashlane.autofill.api.changepause.AutofillApiChangePauseLogger
import com.dashlane.autofill.api.changepause.view.ChangePauseFragment
import com.dashlane.autofill.api.changepause.view.ChangePauseFragmentContract
import com.dashlane.autofill.api.pause.model.PauseDurations
import com.dashlane.autofill.api.pause.view.AskPauseDialogContract
import com.dashlane.autofill.api.pause.view.BottomSheetAskPauseDialogFragment
import com.dashlane.autofill.api.unlinkaccount.view.UnlinkAccountsFragment
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.userfeatures.UserFeaturesChecker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject



@AndroidEntryPoint
class RevertActionsActivity : DashlaneActivity(),
    AskPauseDialogContract,
    ChangePauseFragmentContract {

    private val changePauseComponent: AutofillApiChangePauseComponent
        get() = AutofillApiChangePauseComponent(this)

    private val autofillApiChangePauseLogger: AutofillApiChangePauseLogger
        get() = changePauseComponent.autofillApiChangePauseLogger

    lateinit var autoFillFormSource: AutoFillFormSource

    @Inject
    lateinit var userFeaturesChecker: UserFeaturesChecker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        autoFillFormSource = intent?.extras?.getParcelable(EXTRA_FORM_SOURCE) ?: run {
            finish()
            return
        }

        setContentView(R.layout.activity_revert_actions)
        setUnlinkAccountHeader()

        actionBarUtil.setup()
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = getString(R.string.autofill_revert_actions_title)
        }

        if (savedInstanceState == null) {
            val fragmentTransaction = supportFragmentManager
                .beginTransaction()
                .add(R.id.change_pause_fragment, ChangePauseFragment.newInstance(autoFillFormSource))
            if (!userFeaturesChecker.has(UserFeaturesChecker.FeatureFlip.LINKED_WEBSITES_IN_CONTEXT)) {
                fragmentTransaction.add(
                    R.id.unlink_accounts_fragment,
                    UnlinkAccountsFragment.newInstance(autoFillFormSource)
                )
            }
            fragmentTransaction.commit()
        }
    }

    private fun setUnlinkAccountHeader() {
        if (!userFeaturesChecker.has(UserFeaturesChecker.FeatureFlip.LINKED_WEBSITES_IN_CONTEXT)) {
            val unlinkAccountHeader: TextView = findViewById(R.id.tv_linked_accounts)
            unlinkAccountHeader.setText(R.string.autofill_revert_actions_unlink_account_header)
        }
    }

    override fun updateActionBarTitle(title: String) {
        supportActionBar?.title = title
    }

    override fun openPauseFormSourceDialog() {
        openBottomSheetAskPauseDialog()
    }

    private fun openBottomSheetAskPauseDialog() {
        var dialog = supportFragmentManager.findFragmentByTag(BottomSheetAskPauseDialogFragment.PAUSE_DIALOG_TAG)
        if (dialog == null) {
            dialog = BottomSheetAskPauseDialogFragment.buildFragment(true)
            dialog.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Theme_Dashlane_Transparent_Cancelable)
            dialog.show(supportFragmentManager, BottomSheetAskPauseDialogFragment.PAUSE_DIALOG_TAG)
        }
    }

    override fun getPausedFormSource(): AutoFillFormSource {
        return autoFillFormSource
    }

    override fun onPauseFormSourceDialogResponse(pauseDurations: PauseDurations?) {
        pauseDurations?.let {
            autofillApiChangePauseLogger.pauseFormSource(autoFillFormSource, it)
        }
        supportFragmentManager.findFragmentById(R.id.change_pause_fragment)?.onResume()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val EXTRA_ORIGIN = "origin"
        private const val EXTRA_FORM_SOURCE = "extra_form_source"
        private const val DEFAULT_LOG_ORIGIN = "settings"

        @JvmStatic
        fun newIntent(
            context: Context,
            autoFillFormSource: AutoFillFormSource,
            origin: String?
        ): Intent {
            val intent = Intent(context, RevertActionsActivity::class.java)
            intent.putExtra(EXTRA_ORIGIN, origin ?: DEFAULT_LOG_ORIGIN)
            intent.putExtra(EXTRA_FORM_SOURCE, autoFillFormSource)

            return intent
        }
    }
}
