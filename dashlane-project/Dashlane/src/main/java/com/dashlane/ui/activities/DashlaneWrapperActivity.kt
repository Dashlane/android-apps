package com.dashlane.ui.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import com.dashlane.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashlaneWrapperActivity : DashlaneActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashlane_wrapper)
        actionBarUtil.setup()
        navigator.handleDeepLink(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        finish()
    }

    companion object {
        fun startActivity(
            origin: Activity,
            destination: Uri,
            extra: Bundle?
        ) {
            val intent = newIntent(origin, destination, extra)
            origin.startActivity(intent)
        }

        fun startActivityForResult(
            requestCode: Int,
            origin: Activity,
            destination: Uri,
            extras: Bundle?
        ) {
            val intent = newIntent(origin, destination, extras)
            origin.startActivityForResult(intent, requestCode)
        }

        private fun newIntent(
            context: Context,
            destination: Uri,
            extras: Bundle?
        ): Intent {
            val intent = Intent(context, DashlaneWrapperActivity::class.java)
            intent.action = Intent.ACTION_VIEW
            intent.data = destination
            if (extras != null) {
                intent.putExtras(extras)
            }
            return intent
        }
    }
}