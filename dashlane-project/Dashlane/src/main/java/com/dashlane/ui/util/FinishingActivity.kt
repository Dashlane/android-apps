package com.dashlane.ui.util

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dashlane.util.startActivity

class FinishingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finishAffinity()
    }

    companion object {

        @JvmStatic
        fun finishApplication(context: Context) = context.startActivity<FinishingActivity>()
    }
}