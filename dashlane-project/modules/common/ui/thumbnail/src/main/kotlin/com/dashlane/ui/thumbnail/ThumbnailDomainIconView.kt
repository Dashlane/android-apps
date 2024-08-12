package com.dashlane.ui.thumbnail

import android.content.Context
import android.util.AttributeSet
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.dashlane.design.component.compat.view.ThumbnailView
import com.dashlane.design.component.compat.view.ThumbnailViewType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.jvm.Throws

class ThumbnailDomainIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ThumbnailView(context, attrs, defStyleAttr) {

    init {
        thumbnailType = ThumbnailViewType.VAULT_ITEM_DOMAIN_ICON.value
    }

    private var job: Job? = null

    var domainUrl: String? = null
        set(value) {
            field = value

            if (isAttachedToWindow) {
                collectUrlDomainIcon()
            }
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        collectUrlDomainIcon()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        job?.cancel()
    }

    private fun collectUrlDomainIcon() {
        job?.cancel()
        domainUrl?.let { url ->
            try {
                val viewModel = getViewModelForDomain(url)
                job = GlobalScope.launch {
                    viewModel.urlDomainIcon.collect {
                        thumbnailUrl = it?.url
                        color = it?.color?.toArgb() ?: -1
                    }
                }
                viewModel.fetchIcon(domainUrl)
            } catch (e: IllegalArgumentException) {
                    "Could not get ViewModel from current view tree. You may be trying to display a Thumbnail after the view was " +
                            "detached."
                )
            }
        } ?: run {
            thumbnailUrl = null
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun getViewModelForDomain(urlDomain: String): ThumbnailDomainIconViewModel {
        return ViewModelProvider(requireNotNull(findViewTreeViewModelStoreOwner())).get(
            key = "domain:$urlDomain",
            ThumbnailDomainIconViewModel::class.java
        )
    }
}
