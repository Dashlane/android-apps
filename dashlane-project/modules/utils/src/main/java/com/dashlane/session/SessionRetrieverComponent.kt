package com.dashlane.session

import android.content.Context
import com.dashlane.createaccount.component.SessionRetrieverApplication



interface SessionRetrieverComponent {
    val sessionManager: SessionManager

    companion object {
        operator fun invoke(context: Context) =
            (context.applicationContext as SessionRetrieverApplication).component
    }
}