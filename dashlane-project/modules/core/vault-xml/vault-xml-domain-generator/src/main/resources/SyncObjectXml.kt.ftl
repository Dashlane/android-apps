package com.dashlane.xml.domain.properties

import javax.annotation.processing.Generated

@Generated
object ${className}Xml {
    <#list properties as property>
    const val ${property.keyName} = "${property.keyValue}"
    </#list>
    <#list innerTypes>

    <#items as innerType>
    @Generated
    object ${innerType.className}Xml {
        <#list innerType.properties as property>
        const val ${property.keyName} = "${property.keyValue}"
        </#list>
    }
    </#items>
    </#list>
}
