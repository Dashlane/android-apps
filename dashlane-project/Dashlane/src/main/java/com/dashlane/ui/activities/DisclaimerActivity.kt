package com.dashlane.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import com.dashlane.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DisclaimerActivity : DashlaneActivity() {

    companion object {
        const val EXTRA_DISCLAIMER_RES_ID = "disclaimer_content_res_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disclaimer)

        actionBarUtil.setup()
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = getString(R.string.activity_title_disclaimer)
        }

        val disclaimer = getString(intent.extras!!.getInt(EXTRA_DISCLAIMER_RES_ID, -1))
        findViewById<TextView>(R.id.disclaimer_body).text = disclaimer
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}