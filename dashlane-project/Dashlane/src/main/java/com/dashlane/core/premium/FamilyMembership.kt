package com.dashlane.core.premium

import com.dashlane.util.asOptJSONObjectSequence
import org.json.JSONArray

enum class FamilyMembership {
    REGULAR, ADMIN;

    companion object {
        @JvmStatic
        fun fromJsonArray(array: JSONArray): List<FamilyMembership> =
            array.asOptJSONObjectSequence()
                .map { if (it.getBoolean("isAdmin")) ADMIN else REGULAR }
                .toList()
    }
}
