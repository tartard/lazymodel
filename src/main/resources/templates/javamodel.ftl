<#assign modelName=model.name?replace(":", "_")>
<#assign savedNameSpaces={}>

<#macro constantize string>
<#assign array = string?split("(?=\\p{Upper})", "r")>
${(array?join("_"))?upper_case}</#macro>

<#macro qname qname type>
<#assign qnameParts=qname?split(":")>
    public static final QName ${type}_<@constantize qnameParts[1] /> = QName.createQName(${savedNameSpaces[qnameParts[0]]}, "${qnameParts[1]}");
</#macro>

<#macro renderClasses classes classType>
<#list classes as class>
<@qname qname=class.name type=classType/>
<@renderProps class.properties "PROP"/>
<@renderProps class.associations "ASSOC"/>

</#list>
</#macro>


<#macro renderProps properties propType>
<#list properties as property>
<@qname qname=property.name type=propType/>
</#list>
</#macro>


<#macro renderConstraints constraints>
<#list constraints as constraint>
<#if constraint.type = "LIST">
<@qname qname=constraint.name type="CONSTRAINT"/>
</#if>
</#list>
</#macro>


<#macro renderNameSpaces nameSpaces>
<#list nameSpaces as nameSpace>
<#assign prefix = nameSpace.prefix?upper_case>
<#assign savedNameSpaces = savedNameSpaces + { nameSpace.prefix : "${prefix}_MODEL_URI"}>
    public static final String ${prefix}_MODEL_URI = "${nameSpace.uri}";
    public static final String ${prefix}_MODEL_PREFIX = "${nameSpace.prefix}";

</#list>
</#macro>
<#--

-->
package ;

import org.alfresco.service.namespace.QName;

/**
 * ${modelName} Model Constants.
 *
 *
 */
public class ${modelName} {

    // Namespaces
    <@renderNameSpaces model.namespaces/>

    // Types
    <@renderClasses model.types "TYPE"/>

    // Aspects
    <@renderClasses model.aspects "ASPECT"/>

    // Constraints
    <@renderConstraints model.constraints/>
}