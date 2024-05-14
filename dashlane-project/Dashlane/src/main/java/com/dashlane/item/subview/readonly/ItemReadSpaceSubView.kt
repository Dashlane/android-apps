package com.dashlane.item.subview.readonly

import com.dashlane.teamspaces.model.TeamSpace

class ItemReadSpaceSubView(
    override var value: TeamSpace,
    val values: List<TeamSpace>
) : ItemReadValueSubView<TeamSpace>()