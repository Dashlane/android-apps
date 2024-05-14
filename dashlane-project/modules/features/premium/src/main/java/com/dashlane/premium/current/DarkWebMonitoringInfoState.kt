package com.dashlane.premium.current

internal sealed class DarkWebMonitoringInfoState {

    object Initial : DarkWebMonitoringInfoState()

    object Displaying : DarkWebMonitoringInfoState()
}