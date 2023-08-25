package com.dashlane.item.subview.readonly

import com.dashlane.authenticator.Otp

class ItemAuthenticatorReadSubView(
    val title: String,
    val domain: String,
    override var value: Otp
) : ItemReadValueSubView<Otp>()