package com.dashlane.ui.activities.firstpassword.faq

import android.os.Bundle
import android.widget.ImageButton
import com.dashlane.R
import com.dashlane.ui.activities.DashlaneActivity

class FAQFirstPasswordActivity : DashlaneActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faq_add_first_password)
        findViewById<ImageButton>(R.id.close_button).setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.no_animation, R.anim.slide_out_bottom)
    }
}