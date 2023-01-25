package com.dashlane.dagger.sync

import com.dashlane.core.xmlconverter.DataIdentifierSharingXmlConverter
import com.dashlane.crashreport.CrashReporterManager
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDataRepository
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.sharing.SharingSyncCommunicator
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.useractivity.RacletteLogger
import com.dashlane.util.JsonSerialization
import com.dashlane.util.userfeatures.UserFeaturesChecker

interface SyncSingletonComponent {
    val sessionManager: SessionManager
    val dataIdentifierSharingXmlConverter: DataIdentifierSharingXmlConverter
    val sharingSyncCommunicator: SharingSyncCommunicator
    val jsonSerialization: JsonSerialization
    val userDataRepository: UserDataRepository
    val userDatabaseRepository: UserDatabaseRepository
    val dataSaver: DataSaver
    val crashReporterManager: CrashReporterManager
    val racletteLogger: RacletteLogger
    val userFeaturesChecker: UserFeaturesChecker
}