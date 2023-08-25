package com.dashlane.item.subview.readonly

import com.dashlane.teamspaces.model.Teamspace

class ItemReadSpaceSubView(
    override var value: Teamspace,
    val values: List<Teamspace>
) : ItemReadValueSubView<Teamspace>()