package com.dashlane.autofill.pausedautofillsettings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.DialogFragment
import com.dashlane.autofill.api.R
import com.dashlane.autofill.changepause.view.ChangePauseFragment
import com.dashlane.autofill.changepause.view.ChangePauseFragmentContract
import com.dashlane.autofill.pause.model.PauseDurations
import com.dashlane.autofill.pause.view.AskPauseDialogContract
import com.dashlane.autofill.pause.view.BottomSheetAskPauseDialogFragment
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.getParcelableCompat
import com.dashlane.util.userfeatures.UserFeaturesChecker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PausedAutofillActivity :
    DashlaneActivity(),
    AskPauseDialogContract,
    ChangePauseFragmentContract {

    lateinit var autoFillFormSource: AutoFillFormSource

    @Inject
    lateinit var userFeaturesChecker: UserFeaturesChecker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        autoFillFormSource = intent?.extras?.getParcelableCompat(EXTRA_FORM_SOURCE) ?: run {
            finish()
            return
        }

        setContentView(R.layout.activity_revert_actions)

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
            fragmentTransaction.commit()
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
        supportFragmentManager.findFragmentById(R.id.change_pause_fragment)?.onResume()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val EXTRA_FORM_SOURCE = "extra_form_source"

        @JvmStatic
        fun newIntent(
            context: Context,
            autoFillFormSource: AutoFillFormSource
        ): Intent {
            val intent = Intent(context, PausedAutofillActivity::class.java)
            intent.putExtra(EXTRA_FORM_SOURCE, autoFillFormSource)
            return intent
        }
    }
}
