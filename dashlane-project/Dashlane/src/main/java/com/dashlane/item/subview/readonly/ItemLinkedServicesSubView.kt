package com.dashlane.item.subview.readonly

import com.dashlane.R
import com.dashlane.item.subview.Action
import com.dashlane.item.subview.ItemSubViewImpl
import com.dashlane.xml.domain.SyncObject

class ItemLinkedServicesSubView(
    val linkedWebsites: List<SyncObject.Authentifiant.LinkedServices.AssociatedDomains>?,
    val linkedApps: List<SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps>?,
    val action: Action,
    override var value: String
) : ItemReadValueTextSubView("", value)

class EmptyLinkedServicesSubView : ItemSubViewImpl<String>() {
    override var value: String = ""
    override var topMargin: Int = R.dimen.spacing_empty
}