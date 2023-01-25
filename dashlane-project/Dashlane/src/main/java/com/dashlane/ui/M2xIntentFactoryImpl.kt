package com.dashlane.ui

import android.content.Context
import android.content.Intent
import com.dashlane.m2w.M2wIntentCoordinator
import com.dashlane.m2w.M2wIntroActivity
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@Reusable
class M2xIntentFactoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : M2xIntentFactory {

    override fun buildM2xIntro(origin: String): Intent? =
        Intent(context, M2wIntroActivity::class.java).apply {
            M2wIntentCoordinator.putIntroActivityExtras(
                this,
                origin
            )
        }

    override fun buildM2xConnect(origin: String): Intent =
        M2wIntentCoordinator.createConnectActivityIntent(context, origin)
}