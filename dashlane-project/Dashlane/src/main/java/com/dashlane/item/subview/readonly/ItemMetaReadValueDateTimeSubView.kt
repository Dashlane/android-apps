package com.dashlane.item.subview.readonly

import java.time.Instant

data class ItemMetaReadValueDateTimeSubView(
    val header: String,
    override var value: Instant,
    var formattedDate: String
) : ItemReadValueSubView<Instant>()