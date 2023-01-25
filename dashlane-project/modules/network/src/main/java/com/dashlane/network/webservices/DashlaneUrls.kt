package com.dashlane.network.webservices

import com.dashlane.network.BuildConfig



object DashlaneUrls {
    const val URL_WEBSERVICES = BuildConfig.URL_WEBSERVICES
    const val URL_API = BuildConfig.URL_API

    const val COUNTRY = "/1/country/get"
    const val PREMIUM_STATUS = "/3/premium/status"
    const val FEATURE_FLIPPING = "/1/features/getForUser"
    const val PUSH_NOTIFICATION = "/1/devices/setPushNoficationID"
    const val USER_NUMBER_DEVICES = "/1/devices/numberOfDevices"
    const val HAS_DESKTOP_DEVICE = "/1/devices/hasDesktopDevice"
    const val VERIFY_RECEIPT = "/3/premium/verifyReceipt"
    const val GET_SHARING_LINK = "/1/invites/getSharingLink"
    const val SPACE_DELETED = "/1/teamPlans/spaceDeleted"
    const val GET_TOKEN = "/6/authentication/getToken"
    const val OTP_PHONE_LOST = "/6/authentication/otpphonelost"
}
