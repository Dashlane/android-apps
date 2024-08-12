package com.dashlane.ui.thumbnail

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.storage.userdata.RichIconsSettingProvider
import com.dashlane.url.icon.v2.UrlDomainIcon
import com.dashlane.url.icon.v2.UrlDomainIconRepository
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThumbnailDomainIconViewModel @Inject constructor(
    private val urlDomainIconRepository: UrlDomainIconRepository,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val richIconsSettingProvider: RichIconsSettingProvider
) : ViewModel() {
    private val _urlDomain = MutableStateFlow<UrlDomainState?>(null)
    val urlDomainIcon: Flow<UrlDomainState?> = _urlDomain.asStateFlow()

    fun fetchIcon(urlDomain: String?) {
        if (!richIconsSettingProvider.richIcons) return
        val domain = urlDomain?.toUrlDomainOrNull() ?: return

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                urlDomainIconRepository.getIcons(domain).collect {
                    _urlDomain.emit(it.toUrlDomainState())
                }
            }
        }
    }

    private fun UrlDomainIcon.toUrlDomainState(): UrlDomainState =
        UrlDomainState(
            url = url,
            color = backgroundColor?.let { Color(it) }
        )
}

data class UrlDomainState(
    val url: String?,
    val color: Color?
)