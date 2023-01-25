package com.dashlane.security.darkwebmonitoring

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dashlane.darkweb.DarkWebEmailStatus
import com.dashlane.security.identitydashboard.breach.BreachWrapper

class DarkWebMonitoringAlertViewModel : ViewModel() {
    val darkwebBreaches: MutableLiveData<List<BreachWrapper>> = MutableLiveData()
    val darkwebEmailStatuses: MutableLiveData<List<DarkWebEmailStatus>> = MutableLiveData()
}