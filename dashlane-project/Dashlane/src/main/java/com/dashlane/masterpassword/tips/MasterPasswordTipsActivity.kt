package com.dashlane.masterpassword.tips

import android.os.Bundle
import android.text.Html
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.dashlane.R
import com.dashlane.ui.activities.DashlaneActivity

class MasterPasswordTipsActivity : DashlaneActivity() {

    override var requireUserUnlock = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tips_master_password)

        val titleSection4 = findViewById<TextView>(R.id.tips_section4_title)
        val descriptionSection4 = findViewById<TextView>(R.id.tips_section4_desc)
        val exampleSection4 = findViewById<TextView>(R.id.tips_section4_example)

        if (resources.getBoolean(R.bool.masterpassword_section4_tips_display)) {
            descriptionSection4.text = Html.fromHtml(
                getString(R.string.tips_change_password_section4_desc),
                Html.FROM_HTML_MODE_LEGACY
            )
        } else {
            titleSection4.visibility = View.GONE
            descriptionSection4.visibility = View.GONE
            exampleSection4.visibility = View.GONE
        }

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = resources?.getString(R.string.tips_change_password_activity_label)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}