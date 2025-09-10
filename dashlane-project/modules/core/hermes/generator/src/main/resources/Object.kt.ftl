package com.dashlane.hermes.generated.definitions

<#list imports as import>
import ${import}
</#list>
import javax.annotation.processing.Generated

/**
 * ${documentation}
 *
<#list params as param>
 * @property ${param.name}
 *  ${param.documentation}
</#list>
 */
<#if deprecated>
@Suppress("kotlin:S1133")
@Deprecated("")
</#if>
@Generated
data class ${className}(
    <#list params as param>
    val ${param.name}: ${param.type},
    </#list>
) : TrackingLog.Object {
    override fun collect(collector: TrackingLog.Collector) {
        <#list params as param>
        collector["${param.key}"] = ${param.name}
        </#list>
    }
}