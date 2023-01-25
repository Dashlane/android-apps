package com.dashlane.ui.activities.firstpassword

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.dashlane.R
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.DeviceUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddFirstPasswordActivity : DashlaneActivity() {

    @Inject
    lateinit var presenter: AddFirstPassword.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_first_password)

        val url = intent.extras?.getString(PARAM_URL)
        if (url == null) {
            finish()
            return
        }

        val viewProxy = AddFirstPasswordViewProxy(this)
        presenter.setView(viewProxy)
        presenter.onCreate(url, savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        DeviceUtils.hideKeyboard(this)
        onBackPressed()
        return true
    }

    companion object {
        private const val PARAM_URL = "url"
        fun newIntent(origin: Activity, url: String) =
            Intent(origin, AddFirstPasswordActivity::class.java).apply {
                putExtra(PARAM_URL, url)
            }
    }
}