package com.dashlane.ui.common.compose.components.socialmedia

import androidx.annotation.StringRes
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.iconography.IconTokens
import com.dashlane.ui.commoncomposecomponents.R
import java.net.URL

enum class DashlaneSocialMedia(
    @StringRes val titleId: Int,
    val iconToken: IconToken,
    val url: URL,
) {
    FACEBOOK(
        titleId = R.string.facebook,
        iconToken = IconTokens.socialFacebookFilled,
        url = URL("https://www.facebook.com/GetDashlane")
    ),
    TWITTER(
        titleId = R.string.twitter,
        iconToken = IconTokens.socialTwitterFilled,
        url = URL("https://x.com/dashlane")
    ),
    YOUTUBE(
        titleId = R.string.youtube,
        iconToken = IconTokens.socialYoutubeFilled,
        url = URL("https://www.youtube.com/@Dashlane")
    ),
    INSTAGRAM(
        titleId = R.string.instagram,
        iconToken = IconTokens.socialInstagramFilled,
        url = URL("https://www.instagram.com/dashlane")
    ),
    REDDIT(
        titleId = R.string.reddit,
        iconToken = IconTokens.socialRedditFilled,
        url = URL("https://www.reddit.com/r/Dashlane")
    ),
    LINKEDIN(
        titleId = R.string.linkedin,
        iconToken = IconTokens.socialLinkedinFilled,
        url = URL("https://www.linkedin.com/company/dashlane")
    )
}