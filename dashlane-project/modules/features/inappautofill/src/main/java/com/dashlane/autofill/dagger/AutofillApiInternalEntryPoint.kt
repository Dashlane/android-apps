package com.dashlane.autofill.dagger

import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.FillRequestHandler
import com.dashlane.autofill.SaveRequestHandler
import com.dashlane.autofill.fillresponse.DataSetCreator
import com.dashlane.autofill.internal.AutofillLimiter
import com.dashlane.autofill.request.autofill.database.ItemLoader
import com.dashlane.common.logger.DeveloperInfoLogger
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import dagger.hilt.InstallIn
import dagger.hilt.android.EarlyEntryPoint
import dagger.hilt.components.SingletonComponent

@EarlyEntryPoint
@InstallIn(SingletonComponent::class)
internal interface AutofillApiInternalEntryPoint {
    val fillRequestHandler: FillRequestHandler
    val saveRequestHandler: SaveRequestHandler
    val dataSetCreator: DataSetCreator
    val itemLoader: ItemLoader
    val databaseAccess: AutofillAnalyzerDef.DatabaseAccess
    val preferencesManager: PreferencesManager
    val autofillLimiter: AutofillLimiter
    val sessionManager: SessionManager
    val developerInfoLogger: DeveloperInfoLogger
}
