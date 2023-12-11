package com.dashlane.m2w

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.dashlane.ui.M2xIntentFactory
import javax.inject.Inject

class M2wActivityResultContract @Inject constructor(
    private val m2xIntentFactory: M2xIntentFactory
) : ActivityResultContract<String, Boolean>() {

    override fun createIntent(context: Context, input: String): Intent =
        m2xIntentFactory.buildM2xConnect()

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
        intent != null && M2wIntentCoordinator.isM2wCompleted(intent)
}