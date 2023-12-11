package com.dashlane.ui.widgets.compose.urldomainicon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.url.UrlDomain
import com.dashlane.url.icon.UrlDomainIcon
import com.dashlane.url.icon.UrlDomainIconAndroidRepository
import com.dashlane.util.tryOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UrlDomainIconViewModel @Inject constructor(
    private val urlDomainIconAndroidRepository: UrlDomainIconAndroidRepository
) : ViewModel() {
    fun fetchIcon(urlDomain: UrlDomain?) {
        if (urlDomain == null) return
        viewModelScope.launch {
            tryOrNull {
                urlDomainIconAndroidRepository[urlDomain].collect {
                    _urlDomain.emit(it)
                }
            }
        }
    }

    private val _urlDomain = MutableStateFlow<UrlDomainIcon?>(null)
    val urlDomainIcon: Flow<UrlDomainIcon?> = _urlDomain.asStateFlow()
}