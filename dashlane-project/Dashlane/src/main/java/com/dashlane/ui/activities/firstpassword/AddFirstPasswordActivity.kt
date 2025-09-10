package com.dashlane.ui.activities.firstpassword

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.navigation.Navigator
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.firstpassword.autofilldemo.AutofillDemoActivity
import com.dashlane.ui.activities.firstpassword.ui.AddFirstPasswordScreen
import com.dashlane.util.DeviceUtils
import com.dashlane.xml.domain.SyncObfuscatedValue
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddFirstPasswordActivity : DashlaneActivity() {
    @Inject
    lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            DashlaneTheme {
                AddFirstPasswordScreen(
                    viewModel = hiltViewModel(),
                    navigateUp = ::finish,
                    goToAutofillDemo = ::goToAutofillDemo,
                    goToHomeOrChecklist = ::goToHomeOrChecklist,
                    finish = ::finish,
                )
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        DeviceUtils.hideKeyboard(this)
        onBackPressed()
        return true
    }

    private fun goToAutofillDemo(
        url: String,
        savedLogin: String,
        savedPassword: SyncObfuscatedValue,
    ) {
        val intent = AutofillDemoActivity.newIntent(
            origin = this,
            url = url,
            login = savedLogin,
            password = savedPassword,
        )
        startActivity(intent)
        finish()
    }

    private fun goToHomeOrChecklist() {
        navigator.goToPersonalPlanOrHome()
        finish()
    }

    companion object {
        internal const val PARAM_URL = "url"

        fun newIntent(origin: Activity, url: String) =
            Intent(origin, AddFirstPasswordActivity::class.java).apply {
                putExtra(PARAM_URL, url)
            }
    }
}