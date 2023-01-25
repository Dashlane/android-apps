package com.dashlane.loaders.datalists

import android.content.Context
import androidx.loader.content.CursorLoader
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter



abstract class ListLoader(context: Context) : CursorLoader(context) {

    var items: List<DashlaneRecyclerAdapter.ViewTypeProvider>? = null
}