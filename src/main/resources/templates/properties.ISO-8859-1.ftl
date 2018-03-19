<#assign modelName=model.name?replace(":", "_")>

<#macro renderClasses classes classType>
<#list classes as class>
<#assign className=class.name?replace(":", "_")>
${modelName}.${classType}.${className}.title=${class.title!""}
${modelName}.${classType}.${className}.description=${class.description!""}
<@renderProps class.properties "property"/>
<@renderProps class.associations "association"/>

</#list>
</#macro>


<#macro renderProps properties propType>
<#list properties as property>
<#assign propName=property.name?replace(":","_")>
${modelName}.${propType}.${propName}.title=${property.title!""}
${modelName}.${propType}.${propName}.description=${property.description!""}
</#list>
</#macro>


<#macro renderConstraints constraints>
<#list constraints as constraint>
<#if constraint.type = "LIST">
<#assign constraintName=constraint.name?replace(":", "_")>
<#list constraint.parameters as parameter>
<#if parameter.name="allowedValues">
<#list parameter.listValue as value>
listconstraint.${constraintName}.${value?replace(" ", "\\u0020")}=
</#list>
</#if>
</#list>

</#if>
</#list>
</#macro>
<#--

-->
# ${model.name} model labels

${modelName}.description=${model.description!""}

# types
<@renderClasses model.types "type"/>

# aspects
<@renderClasses model.aspects "aspect"/>

# constraints lists
<@renderConstraints model.constraints/>
