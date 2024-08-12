package com.dashlane.ui.screens.settings

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.dashlane.R
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.util.DiffUtilComparator
import com.dashlane.ui.screens.settings.item.SettingSocialMediaLinks
import com.dashlane.ui.common.compose.components.socialmedia.SocialMediaBar
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder
import java.net.URL

class SettingSocialMediaLinkInRecyclerView(val settingItem: SettingSocialMediaLinks, val onSocialMediaClicked: (URL) -> Unit) :
    DashlaneRecyclerAdapter.ViewTypeProvider,
    DiffUtilComparator<SettingSocialMediaLinkInRecyclerView> {

    override fun getViewType() = VIEW_TYPE

    override fun isItemTheSame(item: SettingSocialMediaLinkInRecyclerView): Boolean = isContentTheSame(item)
    override fun isContentTheSame(item: SettingSocialMediaLinkInRecyclerView): Boolean = settingItem == item.settingItem

    class ViewHolder(view: View) : EfficientViewHolder<SettingSocialMediaLinkInRecyclerView>(view) {
        private val root: FrameLayout
            get() = view.findViewById(R.id.setting_social_medias_root)

        override fun updateView(context: Context, item: SettingSocialMediaLinkInRecyclerView?) {
            val socialMedias = item?.settingItem?.socialMediaList
            if (socialMedias.isNullOrEmpty()) {
                setVisibility(R.id.setting_social_medias_root, View.GONE)
                return
            }

            root.apply {
                removeAllViews()
                addView(
                    ComposeView(context).apply {
                        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                        setContent {
                            DashlaneTheme {
                                SocialMediaBar(
                                    onClick = item.onSocialMediaClicked,
                                    items = socialMedias
                                )
                            }
                        }
                    }
                )
            }
        }

        override fun getOnClickListener(adapterHasListener: Boolean) = View.OnClickListener {}
    }

    companion object {
        val VIEW_TYPE: DashlaneRecyclerAdapter.ViewType<SettingSocialMediaLinkInRecyclerView> =
            DashlaneRecyclerAdapter.ViewType(
                R.layout.list_social_media_footer,
                ViewHolder::class.java
            )
    }
}