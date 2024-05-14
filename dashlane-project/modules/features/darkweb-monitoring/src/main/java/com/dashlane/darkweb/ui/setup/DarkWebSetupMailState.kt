package com.dashlane.darkweb.ui.setup

internal sealed class DarkWebSetupMailState {
    abstract val mail: String?

    object Idle : DarkWebSetupMailState() {
        override val mail: String? get() = null
    }

    object Canceled : DarkWebSetupMailState() {
        override val mail: String? get() = null
    }

    data class InProgress(override val mail: String) : DarkWebSetupMailState()

    data class Succeed(override val mail: String) : DarkWebSetupMailState()

    sealed class Failed : DarkWebSetupMailState() {
        data class EmptyMail(override val mail: String) : Failed()
        data class LimitReached(override val mail: String) : Failed()
        data class InvalidMail(override val mail: String) : Failed()
        data class Unknown(override val mail: String) : Failed()
    }
}