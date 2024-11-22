package com.dashlane.ui.adapter

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class ItemListContext(
    val container: Container = Container.NONE,
    val section: Section = Section.NONE,
    val indexInContainerSection: Int = -1,
    val sectionCount: Int = -1
) : Parcelable {
    val positionInContainerSection
        get() = indexInContainerSection.takeIf { it > -1 }?.plus(1) ?: -1

    enum class Container(val label: String) {
        ALL_ITEMS("allItems"),
        PASSWORD_HEALTH("passwordHealth"),
        CREDENTIALS_LIST("credentials"),
        PASSKEYS_LIST("passkeys"),
        IDS_LIST("ids"),
        PAYMENT_LIST("payments"),
        PERSONAL_INFO_LIST("personalInfos"),
        SECRETS_LIST("secrets"),
        SECURE_NOTE_LIST("secureNotes"),
        SHARING("sharing"),
        SEARCH("search"),
        CSV_IMPORT("csv-import"),
        NONE("none");

        fun asListContext(section: Section = Section.NONE): ItemListContext {
            return ItemListContext(container = this, section = section)
        }
    }

    enum class Section(val label: String) {
        SUGGESTED("Suggested"),
        MOST_RECENT("MostRecent"),
        ALPHABETICAL("Alphabetical"),
        CATEGORY("Category"),
        SEARCH_RECENT("SearchRecent"),
        SEARCH_RESULT("SearchResult"),
        NONE("");
    }

    fun copy(position: Int, count: Int): ItemListContext = copy(
        container = container,
        section = section,
        indexInContainerSection = position,
        sectionCount = count
    )
}
