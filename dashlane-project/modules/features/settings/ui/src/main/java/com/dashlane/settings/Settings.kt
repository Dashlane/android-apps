package com.dashlane.settings

import com.dashlane.settings.Settings.Click
import com.dashlane.settings.Settings.Toggle

sealed interface Settings {
    sealed interface Toggle : Settings

    sealed interface Click : Settings
}

sealed interface Main : Settings

sealed interface Account : Main {
    data object Plan : Click

    data object AccountSummary : Click
}

sealed interface Advanced : Main {
    data object Security : Click

    data object General : Click

    data object Help : Click

    data object AddNewDevice : Click
}

sealed interface MainMisc : Main {
    data object InviteFriend : Click

    data object SendLove : Click

    data object Logout : Click

    data object Version : Click

    data object Debug : Click
}

sealed interface Security : Settings

sealed interface ApplicationLock : Security {
    data object PinLock : Toggle

    data object ChangePin : Click

    data object BiometricUnlock : Toggle

    data object TwoFA : Toggle

    data object BiometricRecovery : Toggle

    data object AccountRecoveryKey : Click

    data object DisableLocal2FA : Toggle

    data object AutoLockOnExit : Toggle

    data object AutoLockTime : Click
}

sealed interface Misc : Security {
    data object Screenshots : Toggle

    data object ClearClipboard : Toggle

    data object Copy2FA : Toggle

    data object ChangeMasterPassword : Click

    data object KeyDerivation : Click

    data object PrivacyAndData : Click

    data object ManageDevices : Click

    data object RichIcons : Toggle
}

sealed interface General : Settings

sealed interface Autofill : General {
    data object EnableAutofill : Toggle

    data object EnableChromeAutofill : Toggle

    data object PausedAutofill : Click

    data object KeyboardAutofill : Toggle

    data object PhishingAlert : Toggle
}

sealed interface Notifications : General {
    data object General : Click

    data object CopyAndPaste : Toggle
}

sealed interface Backup : General {
    data object Export : Click

    data object ExportCsv : Click

    data object Import : Click
}

sealed interface UsageData : General {
    data object EnableUsageData : Toggle
}

sealed interface Help : Settings {
    data object TroubleShooting : Click

    data object UserSupportId : Click

    data object PrivacyPolicy : Click

    data object TermsOfService : Click

    data object Licenses : Click
}