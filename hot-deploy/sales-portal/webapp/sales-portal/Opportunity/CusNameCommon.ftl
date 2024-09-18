<#include "component://admin-portal/webapp/admin-portal/global/ofbizFormMacros.ftl"/> 
         
               <#assign salesOpportunityId = '${requestParameters.salesOpportunityId?if_exists}'>   
               <#assign workEffortId = '${requestParameters.workEffortId?if_exists}'>     
               <#assign customerId = "">     
               <#assign customerCin = "">    
               <#assign customerName = "">  
               <#assign roleTypeId = "">    		
               <#if salesOpportunityId?has_content>
               		<#assign customerList = (Static["org.ofbiz.entity.util.EntityQuery"].use(delegator).select("partyId","roleTypeId").from("SalesOpportunityRole").where("salesOpportunityId",salesOpportunityId).queryFirst())?if_exists />
				    <#if customerList?has_content>
				   		<#assign customerId = "${customerList.partyId?if_exists}">
						<#assign roleTypeId = "${customerList.roleTypeId?if_exists}">
		    		</#if>
                   <#if customerId?has_content>  
                   		<#assign personList = (Static["org.ofbiz.entity.util.EntityQuery"].use(delegator).select("firstName","middleName","lastName","nationalId").from("Person").where("partyId",customerId).queryOne())?if_exists />
						<#assign cinList = (Static["org.ofbiz.entity.util.EntityQuery"].use(delegator).select("idValue").from("PartyIdentification").where("partyId",customerId,"partyIdentificationTypeId","CIF").queryOne())?if_exists />
						<#if personList?has_content>
							<#assign nationalId = "${personList.nationalId?if_exists}">  
							<#assign customerName = "${personList.firstName?if_exists} ${personList.middleName?if_exists} ${personList.lastName?if_exists}">   
						</#if>
						<#if cinList?has_content>  
							<#assign customerCin = "${cinList.idValue?if_exists}">
						</#if>
						<#if roleTypeId?has_content>
							<#if roleTypeId == "NON_CRM">
								<#assign nonCrmName = "${customerName?if_exists}">
							<#elseif roleTypeId == "PROSPECT">	
								<#assign prospectName = "${customerName?if_exists}">
							<#elseif roleTypeId == "CUSTOMER">	
								<#assign cinName = "${customerName?if_exists}">
							</#if>
						</#if>
						
                   </#if> 
               </#if>
                  <@inputHidden name="customerCin" id="customerCin" value="${customerCin?if_exists}"/>
                  <@inputHidden name="customerId" id="customerId" value="${customerId?if_exists}"/>
                   		<#if customerId?has_content>
                        	<h2><a href="#" >${cinName?if_exists} ${prospectName?if_exists} ${nonCrmName?if_exists}</a></h2>
                  		<#else>
                  		 	<h2><a href="#" id="custName"></a></h2>
                  		</#if>
                        <a href="" class="text-dark left-icones" data-toggle="modal" data-target="#viewAlerts" title="View Customer Alerts"><i class="fa fa-bell-o custicons" aria-hidden="true"></i></a>
                        <a href="" class="text-dark left-icones" data-toggle="modal" data-target="#createAlert" title="Add Customer Alerts"><img src="/bootstrap/images/add-customer-alert.png" class="cust-icon" width="21" height="22"></a>
                        <a href="/sr-portal/control/addservicerequest" title="Add Service Request" class="text-dark left-icones"><i class="fa fa-plus-square custicons" aria-hidden="true"></i></a>
                        <a href="<@ofbizUrl>newOpportunity<#if salesOpportunityId?has_content>?salesOpportunityId=${requestParameters.salesOpportunityId?if_exists}</#if></@ofbizUrl>" title="Add Opportunity" class="text-dark left-icones"><img src="/bootstrap/images/add-opportunities.png" class="cust-icon" width="21" height="22"></a>
                        <a href="#" title="Add Activity" id="dropdown09" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" class="text-drak left-icones"><i class="fa fa-calendar" aria-hidden="true"></i></a>
          <div class="dropdown-menu" aria-labelledby="cust-icon"></a>
           <@headerH4
             title="Add Activities"
             />
           <a class="dropdown-item" href="/sales-portal/control/addTask?salesOpportunityId=${requestParameters.salesOpportunityId?if_exists}"><i class="fa fa-clipboard" aria-hidden="true"></i> Task</a>
           <a class="dropdown-item" href="/sales-portal/control/addPhoneCall?salesOpportunityId=${requestParameters.salesOpportunityId?if_exists}"><i class="fa fa-phone" aria-hidden="true"></i> Phone Call</a>
           <a class="dropdown-item" href="/sales-portal/control/addEmail?salesOpportunityId=${requestParameters.salesOpportunityId?if_exists}"><i class="fa fa-envelope" aria-hidden="true"></i> Email</a>
           <a class="dropdown-item" href="/sales-portal/control/addAppointment?salesOpportunityId=${requestParameters.salesOpportunityId?if_exists}"><i class="fa fa-calendar" aria-hidden="true"></i> Appointment</a>
           <a class="dropdown-item" href="/sales-portal/control/addOthers?salesOpportunityId=${requestParameters.salesOpportunityId?if_exists}"><i class="fa fa-square" aria-hidden="true"></i> Others</a>
          </div>
         
          <ul class="text-right">
           <li><div class="checkbox bglight"><label>Phone <i id="phone" class="fa fa-times fa-1 text-danger" aria-hidden="true"></i></label></div></li>
           <li><div class="checkbox bglight"><label>Email <i id="email" class="fa fa-times fa-1 text-danger" aria-hidden="true"></i></label></div></li>
           <li><div class="checkbox bglight"><label>Postal <i id="address" class="fa fa-times fa-1 text-danger" aria-hidden="true"></i></label></div></li>
           <li><div class="checkbox bglight"><label>SMS <i id="sms" class="fa fa-times fa-1 text-danger" aria-hidden="true"></i></label></div></li>

          <li> <a href="#" data-toggle="tooltip" data-placement="top"  title="" class="text-dark mr-3" data-original-title="View Service Requests"> <span class="rounded-circle badge badge-secondary position-absolute" id='operSrCount'></span><i class="fa fa-plus-square custicons fa-1" aria-hidden="true"></i></a></li>
          <li> <a href="/sales-portal/control/findOpportunity" title="View Opportunities" class="text-dark mr-2"><span class="rounded-circle badge badge-secondary position-absolute" id='opportunitiesCount'></span><img src="/bootstrap/images/add-opportunities.png" class="cust-icon" width="21" height="22"></a></li>
          <li id='mailImg'><i class="fa fa-envelope fa-1" aria-hidden="true"></i> <a id='mail' href="#" class="ml-2 mr-2 text-dark" data-toggle="modal" data-target="#viewMail">  </a></li>
          <li id='mobileImg'><i class="fa fa-phone fa-1" aria-hidden="true"></i> (+65) <a id='mobile' href="#" class="mr-2 text-dark" aria-hidden="true" data-toggle="modal" data-target="#viewCall">  </a> </li>
          </ul> 
          
          <#include "component://sales-portal/webapp/sales-portal/teleSales/viewMail.ftl">
          <#include "component://sales-portal/webapp/sales-portal/teleSales/viewCall.ftl">
          <#include "component://sales-portal/webapp/sales-portal/teleSales/createAlert.ftl">
     	  <#include "component://sales-portal/webapp/sales-portal/teleSales/viewAlerts.ftl"> 
     	  
     	  
     	  
     	  
     	  <script>   
	$(document).ready(function() {
	   opportunityCommunicationEvents();
	});

	function opportunityCommunicationEvents() {
		var result = null;
		var salesOpportunityId = document.getElementById('salesOpportunityId').value;
		var partyId = document.getElementById('customerId').value;
	 	var workEffortId = '${workEffortId!}';
		if(salesOpportunityId !=null && salesOpportunityId != "" && salesOpportunityId != 'undefined' && partyId != ""){
		    $.ajax({
		        type: "POST",
		        url: "getOpportunityCommunicationInfo",
		        async: false,
		        data: {"partyId": partyId},
		        success: function(data) {
		            result=data[0];
		            $.each(result, function(name, val) {
			            if(name !=null && name != "" && name != 'undefined'){
			            	if(name == "phoneSolicitation"){
			            		if(val !=null && val != "" && val != 'undefined'){
				            		if(val == "Y"){
				            			document.querySelector("i#phone").setAttribute('class','fa fa-check fa-1 text-success');
				            			document.querySelector("i#sms").setAttribute('class','fa fa-check fa-1 text-success');
				            		}else{
				            			document.querySelector("i#phone").setAttribute('class','fa fa-times fa-1 text-danger');
				            			document.querySelector("i#sms").setAttribute('class','fa fa-times fa-1 text-danger');
				            		}
			            		}else{
				            		document.querySelector("i#phone").setAttribute('class','fa fa-times fa-1 text-danger');
				            		document.querySelector("i#sms").setAttribute('class','fa fa-times fa-1 text-danger');
				            	}
			            	}
			            	if(name == "emailSolicitation"){
			            		if(val !=null && val != "" && val != 'undefined'){
				            		if(val == "Y"){
				            			document.querySelector("i#email").setAttribute('class','fa fa-check fa-1 text-success');
				            		}else{
				            			document.querySelector("i#email").setAttribute('class','fa fa-times fa-1 text-danger');
				            		}
			            		}else{
				            		document.querySelector("i#email").setAttribute('class','fa fa-times fa-1 text-danger');
				            	}
			            	}
			            	if(name == "addressSolicitation"){
			            		if(val !=null && val != "" && val != 'undefined'){
				            		if(val == "Y"){
				            			document.querySelector("i#address").setAttribute('class','fa fa-check fa-1 text-success');
				            		}else{
				            			document.querySelector("i#address").setAttribute('class','fa fa-times fa-1 text-danger');
				            		}
			            		}else{
				            		document.querySelector("i#address").setAttribute('class','fa fa-times fa-1 text-danger');
				            	}
			            	}
			            	if(name == "emailAddr"){
			            		if(val !=null && val != "" && val != 'undefined'){
				            		$("#mail").text(val);
			            		}else{
				            		$("#mailImg").remove();
				            	}
			            	}
			            	if(name == "phoneNumber"){
			            		if(val !=null && val != "" && val != 'undefined'){
				            		 $("#mobile").text(val);
			            		}else{
				            		$("#mobileImg").remove();
				            	}
			            	}
			            	if(name == "operSrCount"){
			            		if(val !=null && val != "" && val != 'undefined'){
				            		 $("#operSrCount").text(val);
			            		}else if(val == 0){
			            			$("#operSrCount").text(val);
			            		}
			            	}
			            	if(name == "opportunitiesCount"){
			            		if(val !=null && val != "" && val != 'undefined'){
				            		 $("#opportunitiesCount").text(val);
			            		}else if(val == 0){
			            			$("#opportunitiesCount").text(val);
			            		}
			            	}
			            }
			        });
		        },error: function(data) {
		        	result=data;
					showAlert("error", "Error occured while fetching Party Communication Data!");
				}
		    });
	    } 
	    
	    else if(workEffortId !=null && workEffortId != "" && workEffortId != 'undefined'){
		    $.ajax({
		        type: "POST",
		        url: "getActivityCommunicationInfo",
		        async: false,
		        data: {"workEffortId": workEffortId},
		        success: function(data) {
		            result=data[0];
		            $.each(result, function(name, val) {
			            if(name !=null && name != "" && name != 'undefined'){
			            	if(name == "phoneSolicitation"){
			            		if(val !=null && val != "" && val != 'undefined'){
				            		if(val == "Y"){
				            			document.querySelector("i#phone").setAttribute('class','fa fa-check fa-1 text-success');
				            			document.querySelector("i#sms").setAttribute('class','fa fa-check fa-1 text-success');
				            		}else{
				            			document.querySelector("i#phone").setAttribute('class','fa fa-times fa-1 text-danger');
				            			document.querySelector("i#sms").setAttribute('class','fa fa-times fa-1 text-danger');
				            		}
			            		}else{
				            		document.querySelector("i#phone").setAttribute('class','fa fa-times fa-1 text-danger');
				            		document.querySelector("i#sms").setAttribute('class','fa fa-times fa-1 text-danger');
				            	}
			            	}
			            	if(name == "emailSolicitation"){
			            		if(val !=null && val != "" && val != 'undefined'){
				            		if(val == "Y"){
				            			document.querySelector("i#email").setAttribute('class','fa fa-check fa-1 text-success');
				            		}else{
				            			document.querySelector("i#email").setAttribute('class','fa fa-times fa-1 text-danger');
				            		}
			            		}else{
				            		document.querySelector("i#email").setAttribute('class','fa fa-times fa-1 text-danger');
				            	}
			            	}
			            	if(name == "addressSolicitation"){
			            		if(val !=null && val != "" && val != 'undefined'){
				            		if(val == "Y"){
				            			document.querySelector("i#address").setAttribute('class','fa fa-check fa-1 text-success');
				            		}else{
				            			document.querySelector("i#address").setAttribute('class','fa fa-times fa-1 text-danger');
				            		}
			            		}else{
				            		document.querySelector("i#address").setAttribute('class','fa fa-times fa-1 text-danger');
				            	}
			            	}
			            	if(name == "emailAddr"){
			            		if(val !=null && val != "" && val != 'undefined'){
				            		$("#mail").text(val);
			            		}else{
				            		$("#mailImg").remove();
				            	}
			            	}
			            	if(name == "phoneNumber"){
			            		if(val !=null && val != "" && val != 'undefined'){
				            		 $("#mobile").text(val);
			            		}else{
				            		$("#mobileImg").remove();
				            	}
			            	}
			            	if(name == "operSrCount"){
			            		if(val !=null && val != "" && val != 'undefined'){
				            		 $("#operSrCount").text(val);
			            		}else if(val == 0){
			            			$("#operSrCount").text(val);
			            		}
			            	}
			            	if(name == "opportunitiesCount"){
			            		if(val !=null && val != "" && val != 'undefined'){
				            		 $("#opportunitiesCount").text(val);
			            		}else if(val == 0){
			            			$("#opportunitiesCount").text(val);
			            		}
			            	}
			            	if(name == "custName"){
			            		if(val !=null && val != "" && val != 'undefined'){
				            		 $("#custName").text(val);
			            		}else if(val == 0){
			            			$("#custName").text(val);
			            		}
			            	}
			            }
			        });
		        },error: function(data) {
		        	result=data;
					showAlert("error", "Error occured while fetching Party Communication Data!");
				}
		    });
	    } 
	    
	}
</script>
	