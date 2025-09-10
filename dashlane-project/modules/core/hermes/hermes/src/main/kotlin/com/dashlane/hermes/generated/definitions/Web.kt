package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class Web(
    val utmLastClickPathname: String? = null,
    val utmLastClickContent: String? = null,
    val everflowTransactionId: String? = null,
    val utmLastClickReferrer: String? = null,
    val utmCampaign: String? = null,
    val utmLastClickVisitTimestamp: Int? = null,
    val utmLastClickSource: String? = null,
    val utmMedium: String? = null,
    val utmLastClickTerm: String? = null,
    val utmTerm: String? = null,
    val gclid: String? = null,
    val hasCookie: Boolean,
    val heapIdentity: String? = null,
    val utmLastClickCampaign: String? = null,
    val utmPathname: String? = null,
    val utmReferrer: String? = null,
    val utmVisitTimestamp: Int? = null,
    val utmSource: String? = null,
    val utmContent: String? = null,
    val utmLastClickMedium: String? = null,
) : TrackingLog.Object {
    override fun collect(collector: TrackingLog.Collector) {
        collector["utm_last_click_pathname"] = utmLastClickPathname
        collector["utm_last_click_content"] = utmLastClickContent
        collector["everflow_transaction_id"] = everflowTransactionId
        collector["utm_last_click_referrer"] = utmLastClickReferrer
        collector["utm_campaign"] = utmCampaign
        collector["utm_last_click_visit_timestamp"] = utmLastClickVisitTimestamp
        collector["utm_last_click_source"] = utmLastClickSource
        collector["utm_medium"] = utmMedium
        collector["utm_last_click_term"] = utmLastClickTerm
        collector["utm_term"] = utmTerm
        collector["gclid"] = gclid
        collector["has_cookie"] = hasCookie
        collector["heap_identity"] = heapIdentity
        collector["utm_last_click_campaign"] = utmLastClickCampaign
        collector["utm_pathname"] = utmPathname
        collector["utm_referrer"] = utmReferrer
        collector["utm_visit_timestamp"] = utmVisitTimestamp
        collector["utm_source"] = utmSource
        collector["utm_content"] = utmContent
        collector["utm_last_click_medium"] = utmLastClickMedium
    }
}