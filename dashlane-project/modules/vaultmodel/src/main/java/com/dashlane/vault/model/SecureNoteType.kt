package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObject

fun SyncObject.SecureNoteType.getLabelId(): Int {
    return when (this) {
        SyncObject.SecureNoteType.EARTH -> R.string.earth
        SyncObject.SecureNoteType.GRAY -> R.string.gray
        SyncObject.SecureNoteType.PINK -> R.string.pink
        SyncObject.SecureNoteType.BLUE -> R.string.blue
        SyncObject.SecureNoteType.CRIMSON -> R.string.crimson
        SyncObject.SecureNoteType.PURPLE -> R.string.purple
        SyncObject.SecureNoteType.YELLOW -> R.string.yellow
        SyncObject.SecureNoteType.GREEN -> R.string.green
        SyncObject.SecureNoteType.NO_TYPE -> R.string.no_color
        SyncObject.SecureNoteType.ORANGE -> R.string.orange
    }
}

fun SyncObject.SecureNoteType.getColorId(): Int {
    return when (this) {
        SyncObject.SecureNoteType.EARTH -> R.color.securenote_color_brown
        SyncObject.SecureNoteType.GRAY -> R.color.securenote_color_gray
        SyncObject.SecureNoteType.PINK -> R.color.securenote_color_pink
        SyncObject.SecureNoteType.BLUE -> R.color.securenote_color_blue
        SyncObject.SecureNoteType.CRIMSON -> R.color.securenote_color_red
        SyncObject.SecureNoteType.PURPLE -> R.color.securenote_color_purple
        SyncObject.SecureNoteType.YELLOW -> R.color.securenote_color_yellow
        SyncObject.SecureNoteType.GREEN -> R.color.securenote_color_green
        SyncObject.SecureNoteType.NO_TYPE -> R.color.securenote_color_gray
        SyncObject.SecureNoteType.ORANGE -> R.color.securenote_color_orange
    }
}

fun SyncObject.SecureNoteType.getUsageLogLabel(): String {
    return when (this) {
        SyncObject.SecureNoteType.EARTH -> "Brown"
        SyncObject.SecureNoteType.GRAY -> "Gray"
        SyncObject.SecureNoteType.PINK -> "Pink"
        SyncObject.SecureNoteType.BLUE -> "Blue"
        SyncObject.SecureNoteType.CRIMSON -> "Red"
        SyncObject.SecureNoteType.PURPLE -> "Purple"
        SyncObject.SecureNoteType.YELLOW -> "Yellow"
        SyncObject.SecureNoteType.GREEN -> "Green"
        SyncObject.SecureNoteType.NO_TYPE -> "No color"
        SyncObject.SecureNoteType.ORANGE -> "Orange"
    }
}

fun getSecureNoteTypeDeprecatedDatabaseOrder(): List<SyncObject.SecureNoteType> {
    return listOf(
        SyncObject.SecureNoteType.NO_TYPE,
        SyncObject.SecureNoteType.BLUE,
        SyncObject.SecureNoteType.PURPLE,
        SyncObject.SecureNoteType.PINK,
        SyncObject.SecureNoteType.CRIMSON,
        SyncObject.SecureNoteType.EARTH,
        SyncObject.SecureNoteType.GREEN,
        SyncObject.SecureNoteType.ORANGE,
        SyncObject.SecureNoteType.YELLOW,
        SyncObject.SecureNoteType.GRAY
    )
}
