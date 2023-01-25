package com.dashlane.darkweb.ui.result

import android.content.Intent
import android.content.Intent.makeMainSelectorActivity
import android.os.Bundle
import android.text.Html
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.dashlane.darkweb.registration.ui.R
import com.dashlane.darkweb.ui.setup.DarkWebSetupMailActivity
import com.dashlane.useractivity.log.inject.UserActivityComponent

class DarkWebSetupResultActivity : AppCompatActivity() {
    private var logger: DarkWebSetupResultLoggerImpl? = null
    private val emailAppIntent = makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_EMAIL).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val usageLogRepository = UserActivityComponent(this).currentSessionUsageLogRepository
        logger = DarkWebSetupResultLoggerImpl(usageLogRepository)
        logger?.logShow()

        setContentView(R.layout.activity_darkweb_setup_result)

        
        val mail = intent.getStringExtra(DarkWebSetupMailActivity.INTENT_SIGN_UP_MAIL)
        val formatString = String.format(getString(R.string.darkweb_setup_result_body_no_color), mail)

        
        findViewById<TextView>(R.id.view_darkweb_result_body).text =
            Html.fromHtml(formatString, Html.FROM_HTML_MODE_LEGACY)

        findViewById<Button>(R.id.view_darkweb_result_email_confirmed).setOnClickListener {
            onEmailConfirmedClick()
        }

        findViewById<Button>(R.id.view_darkweb_result_open_email_app).apply {
            val canOpenApp = emailAppIntent.resolveActivity(packageManager) != null
            setVisible(canOpenApp)
            if (canOpenApp) setOnClickListener {
                logger?.logOpenApp()
                startActivity(emailAppIntent)
            }
        }
        ViewCompat.setAccessibilityHeading(findViewById(R.id.view_darkweb_result_title)!!, true)
    }

    private fun onEmailConfirmedClick() {
        logger?.logClose()
        setResult(RESULT_OK)
        finish()
    }
}
