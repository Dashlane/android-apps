package com.dashlane.hermes.generated.definitions

<#list imports as import>
import ${import}
</#list>
import javax.annotation.processing.Generated

/**
 * ${documentation}
 */
@Generated
data class ItemId(
    val id: String,
) : TrackingLog.DataObject {
    companion object {
        const val REGEX_ITEM_ID =
            "\\s?[{]?[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[89ABab]" +
                "[0-9A-Fa-f]{3}-[0-9A-Fa-f]{12}[}]?[\\s]?|[A-Z]+(_[A-Z]+)*_userId*\$"
        const val FALLBACK_ID = "{00000000-0000-0000-a000-000000000000}"
    }

    /**
     * Filter out vault item ids that are malformed and don't follow the format expected by Data.
     * Based on the following regex defined by Data
     *
     * Data Regex as of May 22:
     *  - \s?[{]?[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[89ABab][0-9A-Fa-f]{3}-[0-9A-Fa-f]{12}[}]?[\\s]?|[A-Z]+(_[A-Z]+)*_userId*$
     */
    override val code: String = run {
        val match =
            Regex(REGEX_ITEM_ID)
                .matches(id)
        return@run if (match) {
            id
        } else {
            FALLBACK_ID
        }
    }
}