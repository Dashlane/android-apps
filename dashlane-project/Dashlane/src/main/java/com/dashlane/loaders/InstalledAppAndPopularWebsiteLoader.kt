package com.dashlane.loaders

import androidx.fragment.app.Fragment
import com.dashlane.ext.application.KnownApplicationProvider
import com.dashlane.ui.util.DataLoaderForUi
import com.dashlane.util.domain.PopularWebsiteUtils.getPopularWebsites

class InstalledAppAndPopularWebsiteLoader(
    private val fragment: Fragment,
    private val listener: Listener,
    private val knownApplicationProvider: KnownApplicationProvider
) : DataLoaderForUi<List<String?>?>(fragment) {
    interface Listener {
        fun onLoadFinished(result: List<String>?)
    }

    override fun loadData(): List<String> {
        val context = fragment.context ?: return ArrayList()
        return getPopularWebsites(context, knownApplicationProvider)
    }

    override fun onPostExecute(t: List<String?>?) {
        listener.onLoadFinished(t?.filterNotNull())
    }
}