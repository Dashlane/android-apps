package com.dashlane.ui.screens.settings.item

import android.content.Context
import com.dashlane.ui.common.compose.components.socialmedia.DashlaneSocialMedia

interface SettingSocialMediaLinks : SettingItem {
    override val title: String
        get() = ""
    override val description: String?
        get() = null

    override fun isEnable() = true
    override fun isVisible() = true
    override fun onClick(context: Context) {
        
    }

    val socialMediaList: List<DashlaneSocialMedia>
}