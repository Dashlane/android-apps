package com.dashlane.m2w

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.dashlane.ui.M2xIntentFactory
import javax.inject.Inject

class M2wActivityResultContract @Inject constructor(
    private val m2xIntentFactory: M2xIntentFactory
) : ActivityResultContract<Unit, M2WResult>() {

    override fun createIntent(context: Context, input: Unit): Intent =
        m2xIntentFactory.buildM2xConnect()

    override fun parseResult(resultCode: Int, intent: Intent?): M2WResult {
        intent ?: return M2WResult.CANCELLED
        return if (M2wIntentCoordinator.isM2wCompleted(intent)) {
            M2WResult.COMPLETED
        } else if (M2wIntentCoordinator.isM2wSkipped(intent)) {
            M2WResult.SKIPPED
        } else {
            M2WResult.CANCELLED
        }
    }
}