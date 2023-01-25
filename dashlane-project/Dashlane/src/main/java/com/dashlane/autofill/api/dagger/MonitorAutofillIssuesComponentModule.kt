package com.dashlane.autofill.api.dagger

import com.dashlane.autofill.api.monitorlog.AutofillConfiguration
import com.dashlane.autofill.api.monitorlog.InAppAutofillConfiguration
import com.dashlane.autofill.api.monitorlog.MonitorAutofillDeviceInfoServiceImpl
import com.dashlane.autofill.api.monitorlog.MonitorAutofillDeviceInfoService
import com.dashlane.autofill.api.monitorlog.MonitorAutofillIssues
import com.dashlane.autofill.api.monitorlog.MonitorAutofillIssuesImpl
import com.dashlane.autofill.api.monitorlog.MonitorAutofillIssuesLogger
import com.dashlane.autofill.core.SessionMonitorAutofillIssuesLogger
import dagger.Binds
import dagger.Module



@Module
internal abstract class MonitorAutofillIssuesComponentModule {
    @Binds
    abstract fun bindsMonitorAutofillDeviceInfoService(impl: MonitorAutofillDeviceInfoServiceImpl): MonitorAutofillDeviceInfoService

    @Binds
    abstract fun bindsMonitorAutofillIssuesLogger(impl: SessionMonitorAutofillIssuesLogger): MonitorAutofillIssuesLogger

    @Binds
    abstract fun bindsGlobalAutofillConfiguration(impl: InAppAutofillConfiguration): AutofillConfiguration

    @Binds
    abstract fun bindsMonitorAutofill(impl: MonitorAutofillIssuesImpl): MonitorAutofillIssues
}