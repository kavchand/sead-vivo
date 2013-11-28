<#--
Copyright (c) 2013, Cornell University
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    * Neither the name of Cornell University nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
-->

<#-- Macros for form controls -->

<#-- 
    Macro: optionGroups
    
    Output: a sequence of option groups with options.
    
    Input: a map of option groups to lists of Option objects.
    
    Usage: <@optionGroups groups=myOptionGroups />
-->
<#macro optionGroups groups>
    <#list groups?keys as group>
        <optgroup label="${group}">
            <@options opts=groups[group] />
        </optgroup>
    </#list>
</#macro>

<#---------------------------------------------------------------------------->

<#-- 
    Macro: options
    
    Output: a sequence of options.
    
    Input: a list of Option objects.
    
    Usage: <@options opts=myOptions />
-->
<#macro options opts>
    <#list opts as opt>
        <option value="${opt.value}"<#if opt.selected> selected="selected"</#if>>${opt.body}</option> 
    </#list>
</#macro>

<#---------------------------------------------------------------------------->

<#-- 
    Macro: hiddenInputs
    
    Output hidden inputs from a map of names to values.
    
    Input: a map of strings (names) to strings (values). May be null.
    
    Usage: <@hiddenInputs inputs />
-->
<#macro hiddenInputs inputs="">
    <#if inputs?has_content>
        <#list inputs?keys as name>
            <input type="hidden" name="${name}" value="${inputs[name]}" />
        </#list>
    </#if>
</#macro>