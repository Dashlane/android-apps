package com.dashlane.security.darkwebmonitoring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.darkweb.DarkWebEmailStatus
import com.dashlane.security.identitydashboard.breach.BreachWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DarkWebMonitoringAlertViewModel @Inject constructor(
    private val provider: DarkWebMonitoringDataProvider
) : ViewModel() {

    private val stateFlow = MutableStateFlow<DarkWebStatus?>(null)
    val breachesState = stateFlow.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val breaches = async { provider.getDarkwebBreaches() }
            val emailStatuses = async { provider.getDarkwebEmailStatuses() }
            stateFlow.emit(
                DarkWebStatus(
                    breaches = breaches.await(),
                    emailStatus = emailStatuses.await(),
                )
            )
        }
    }
}

data class DarkWebStatus(
    val breaches: List<BreachWrapper>,
    val emailStatus: List<DarkWebEmailStatus>?,
)