package com.dashlane.ui.screens.settings

sealed class Use2faSettingState {
    abstract val enabled: Boolean
    abstract val visible: Boolean
    abstract val checked: Boolean
    abstract val loaded: Boolean

    object Loading : Use2faSettingState() {
        override val enabled: Boolean
            get() = true
        override val visible: Boolean
            get() = true
        override val checked: Boolean
            get() = false
        override val loaded: Boolean
            get() = false
    }

    object Unavailable : Use2faSettingState() {
        override val enabled: Boolean
            get() = false
        override val visible: Boolean
            get() = false
        override val checked: Boolean
            get() = false
        override val loaded: Boolean
            get() = true
    }

    data class Available(
        override val enabled: Boolean,
        override val checked: Boolean
    ) : Use2faSettingState() {
        override val visible: Boolean
            get() = true
        override val loaded: Boolean
            get() = true
    }
}