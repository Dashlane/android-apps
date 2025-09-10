<#switch eventCategory>
  <#case "ANONYMOUS">
package com.dashlane.hermes.generated.events.anonymous
    <#break>
  <#default>
package com.dashlane.hermes.generated.events.user
</#switch>

<#list imports as import>
import ${import}
</#list>
import javax.annotation.processing.Generated

/**
${documentation}
<#if params?has_content>
 *
</#if>
<#list params as param>
 * @property ${param.name}
<#if param.documentation?has_content>
 *  ${param.documentation}
</#if>
</#list>
 */
<#if deprecated>
@Suppress("kotlin:S1133")
@Deprecated("")
</#if>
<#if params?has_content>
@Generated
data class ${className}(
    <#list params as param>
    val ${param.name}: ${param.type},
    </#list>
) : TrackingLog {
<#else>
@Generated
class ${className} : TrackingLog {
</#if>
    override val name
        get() = "${eventName}"

    override val category
        get() = TrackingLog.Category.${eventCategory}

    override val priority
        get() = ${eventPriority?c}

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        <#list params as param>
        collector["${param.key}"] = ${param.name}
        </#list>
    }
}