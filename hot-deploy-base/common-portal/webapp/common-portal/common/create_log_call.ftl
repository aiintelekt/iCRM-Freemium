<#include "component://admin-portal/webapp/admin-portal/global/ofbizFormMacros.ftl"/>
<#if request.getRequestURI().contains("logCall")>
<div class="page-header border-b">
  <#if logTask?exists && logTask?has_content && logTask == "Log Email">
     <h1 class="float-left">Log Email</h1>
  <#else>
     <h1 class="float-left">Log Call</h1>
  </#if>
</div>
<#else>
<div id="callLogModal" class="modal fade" role="dialog">
<div class="modal-dialog">
<!-- Modal content-->
<div class="modal-content">
<div class="modal-header">
  <h4 class="modal-title">Call Log</h4>
  <button type="reset" class="close" data-dismiss="modal">&times;</button>
</div>
<div class="modal-body">
<div class="">
</#if>
<form method="post" action="logTask" id="logTaskFormCall" class="form-horizontal" name="logTaskFormCall" novalidate="novalidate" data-toggle="validator">
  <#if requestParameters.donePageCallLog?exists && requestParameters.donePageCallLog?has_content>
     <#assign requestURI = "${requestParameters.donePageCallLog}"/>
  <#else>
  <#assign requestURI = "viewContact"/> 
  <#if request.getRequestURI().contains("viewLead")>
    <#assign requestURI = "viewLead"/>
  <#elseif request.getRequestURI().contains("viewAccount")>
    <#assign requestURI = "viewAccount"/>
  <#elseif request.getRequestURI().contains("viewCustomer")>
    <#assign requestURI = "viewCustomer"/>
  </#if>
  </#if>
  <input type="hidden" name="partyId" value="${internalPartyId?if_exists}"/>
  <input type="hidden" name="donePage" value="${requestURI?if_exists}"/>
  <input type="hidden" id="activeTabLogCall" name="activeTab" value="opportunites"/>
  <input type="hidden" value="${workEffortPurposeTypeId?if_exists}" name="workEffortPurposeTypeId">
  <input type="hidden" value="${internalPartyId?if_exists}" name="internalPartyId">
  <input type="hidden" value="${fromPartyId?if_exists}" name="fromPartyId">
  <input type="hidden" value="Y" name="outbound">
  <input type="hidden" id="duration" size="25" value="1:00" name="duration" class="inputBox"> 
  <input type="hidden" name="actualStartDate" value="" form="logTaskForm">
  <div class="row padding-r">
    <#if request.getRequestURI().contains("logCall")>
      <div class="col-md-6 col-sm-6">
    <#else>
      <div class="col-md-12 col-sm-12">
    </#if>
      
       <@inputRow 
       id="workEffortName"
       label=uiLabelMap.subject
       placeholder=uiLabelMap.Subject
       value=""
       required=true
       />
       <div class="form-group row">
         <label  class="col-form-label col-sm-4">${uiLabelMap.message}</label>
         <div class="col-sm-7">
           <textarea class="form-control" id="content" name="content"></textarea>
         </div>
       </div>
      
    </div>
  </div>
  <div class="clearfix"></div>
  
  <#if request.getRequestURI().contains("logCall")>
    <div class="col-md-6 col-sm-6">
  <#else>
    <div class="col-md-12 col-sm-12">
  </#if>
    <div class="form-group row">
      <div class="offset-sm-4 col-sm-9">
        <#--<button type="reset" class="btn btn-sm btn-primary navbar-dark mt">Create</button>-->
        <@submit class="btn btn-sm btn-primary navbar-dark mt-2 ml-1" label="Submit"/>
        <#--<button type="submit" class="btn btn-sm btn-primary navbar-dark mt" data-dismiss="modal">Clear</button>-->
      </div>
    </div>
  </div>
</form>

</div>
</div>
<div class="modal-footer">
  <button type="submit" class="btn btn-sm btn-primary navbar-dark" data-dismiss="modal">Close</button>
</div>
</div>
</div>
</div>

<#include "component://common-portal/webapp/common-portal/common/create_email_log.ftl"/>