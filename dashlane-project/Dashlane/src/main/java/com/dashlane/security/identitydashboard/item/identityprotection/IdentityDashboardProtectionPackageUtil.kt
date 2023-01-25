package com.dashlane.security.identitydashboard.item.identityprotection

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.style.BulletSpan
import androidx.annotation.StringRes



internal fun SpannableStringBuilder.toBulletPointSpannable(bulletSpan: BulletSpan): SpannableStringBuilder {
    this.setSpan(bulletSpan, 0, this.length, 0)
    return this
}



internal fun Context.getSpannableStringBuilder(@StringRes stringId: Int):
        SpannableStringBuilder {
    return SpannableStringBuilder(this.getString(stringId))
}

internal fun Context.getSpannableStringBuilder(@StringRes stringId: Int, string1: String):
        SpannableStringBuilder {
    return SpannableStringBuilder(this.getString(stringId, string1))
}

internal fun Context.getSpannableStringBuilder(@StringRes stringId: Int, string1: String, string2: String):
        SpannableStringBuilder {
    return SpannableStringBuilder(this.getString(stringId, string1, string2))
}