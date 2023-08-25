package com.dashlane.ui.activities.debug

import android.os.Bundle
import com.dashlane.R
import com.dashlane.ui.activities.DashlaneActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DebugActivity : DashlaneActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, DebugFragment())
            .commit()
    }
}