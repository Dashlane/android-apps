package com.dashlane.darkweb.ui.setup

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dashlane.darkweb.registration.ui.R
import com.dashlane.darkweb.registration.ui.databinding.ActivityDarkwebSetupMailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DarkWebSetupMailActivity : AppCompatActivity() {
    private val viewModel by viewModels<DarkWebSetupMailViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_darkweb_setup_mail)
        val binding = ActivityDarkwebSetupMailBinding.bind(findViewById(R.id.view_root))
        DarkWebSetupMailViewProxy(this, binding, viewModel)
    }

    companion object {
        const val INTENT_SIGN_UP_MAIL = "input_mail"
        const val ORIGIN_KEY = "origin"
        const val ORIGIN_ADD_ADDRESS = "add_address"
    }
}
