package com.dashlane.ui.activities.firstpassword.autofilldemo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.dashlane.R
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.CurrentPageViewLogger
import com.dashlane.util.getThemeAttrColor
import com.dashlane.util.setContentTint
import com.dashlane.xml.domain.SyncObfuscatedValue
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AutofillDemoActivity : DashlaneActivity(), CurrentPageViewLogger.Owner {

    @Inject
    lateinit var presenter: AutofillDemo.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_autofill_demo)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)!!
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        toolbar.setContentTint(getThemeAttrColor(R.attr.colorOnBackground))

        val url = intent.extras?.getString(PARAM_URL)
        val login = intent.extras?.getString(PARAM_LOGIN)
        val password = intent.extras?.getString(PARAM_PASSWORD)
        if (url == null || login == null || password == null) {
            finish()
            return
        }

        val viewProxy = AutofillDemoViewProxy(this)
        presenter.setView(viewProxy)
        presenter.onCreate(url, login, password, savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.onSaveInstanceState(outState)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        private const val PARAM_URL = "url"
        private const val PARAM_LOGIN = "login"
        private const val PARAM_PASSWORD = "password"
        fun newIntent(origin: Activity, url: String, login: String, password: SyncObfuscatedValue) =
            Intent(origin, AutofillDemoActivity::class.java).apply {
                putExtra(PARAM_URL, url)
                putExtra(PARAM_LOGIN, login)
                putExtra(PARAM_PASSWORD, password.toString())
            }
    }
}