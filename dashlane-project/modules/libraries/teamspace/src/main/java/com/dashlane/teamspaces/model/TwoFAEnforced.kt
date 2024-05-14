package com.dashlane.teamspaces.model

enum class TwoFAEnforced(val value: String) {
    DISABLED("disabled"),
    NEW_DEVICE("newDevice"),
    LOGIN("login")
}