package com.dashlane.item.subview.readonly

import java.time.LocalDate

open class ItemReadValueDateSubView(
    val hint: String,
    override var value: LocalDate?,
    var formattedDate: String?
) : ItemReadValueSubView<LocalDate?>()