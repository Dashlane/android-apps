package com.dashlane.vault.textfactory.list

import android.content.res.Resources
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.model.titleForList
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.R
import javax.inject.Inject

class AuthentifiantListTextFactory @Inject constructor(
    private val resources: Resources,
) : DataIdentifierListTextFactory<SummaryObject.Authentifiant> {
    override fun getTitle(item: SummaryObject.Authentifiant): StatusText = StatusText(item.titleForList.orEmpty())

    override fun getDescription(item: SummaryObject.Authentifiant, default: StatusText): StatusText {
        return when {
            item.isPasswordEmpty -> StatusText(
                resources.getString(R.string.incomplete_reason_missing_password_list_line_2),
                true
            )
            item.login.isSemanticallyNull() && item.email.isSemanticallyNull() -> default
            else -> {
                val email = item.email
                val login = item.login
                StatusText(if (login.isNotSemanticallyNull()) login!! else email!!)
            }
        }
    }
}
