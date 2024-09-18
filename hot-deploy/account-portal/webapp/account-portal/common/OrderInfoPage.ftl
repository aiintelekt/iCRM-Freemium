<#--
 * Copyright (c) Open Source Strategies, Inc.
 * 
 * Opentaps is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Opentaps is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Opentaps.  If not, see <http://www.gnu.org/licenses/>.
-->
<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<#include "component://admin-portal/webapp/admin-portal/global/ofbizFormMacros.ftl"/>
<#if OrderByIdViewList?exists>
	<div class="row">
		<div id="main" role="main" class="pd-btm-title-bar">
		<div class="col-md-12 col-lg-12 col-sm-12 dash-panel">	    
		    <div class="card-head margin-adj">
		        <h2>
		             Order :  ${orderId!}
		        </h2>
		     </div>   
	     </div>
	    
	     <div class='dash-panel'>
			<table width="100%" border="0" cellpadding="1" cellspacing="0">
				<tbody>
					<tr>
					    <td align="left" valign="top" width="15%">
					     <div class="tabletext" >&nbsp;<b>Order Date</b></div>
					   	</td>
					    <td width="5" >&nbsp;</td>
					    <td align="left" valign="top" width="80%">
					          <div class="tabletext" >
					              <span>${OrderByIdViewList.orderDate?if_exists}</span>             
					          </div>
					    </td>
					</tr>
					<tr>
					  	<td align="left" valign="top" width="15%">
				      		<div class="tabletext" >&nbsp;</div>
				      	</td>
				    </tr>
				    <tr>
					    <td align="left" valign="top" width="15%">
					      <div class="tabletext" >&nbsp;<b>Status</b></div>
					    </td>
					    <td width="5" >&nbsp;</td>
					    <td align="left" valign="top" width="80%">
					    	<div class="tabletext">${OrderByIdViewList.statusDesc?if_exists}</div>
					    </td>
					</tr>
					<tr>
					  	<td align="left" valign="top" width="15%">
				      		<div class="tabletext" >&nbsp;</div>
				      	</td>
				    </tr>
				    <tr>
					  	<td align="left" valign="top" width="15%">
				      		<div class="tabletext" >&nbsp;</div>
				      	</td>
				    </tr>
				    <tr>
					    <td align="left" valign="top" width="15%">
					      <div class="tabletext" >&nbsp;<b>Delivery Date</b></div>
					    </td>
					    <td width="5" >&nbsp;</td>
					    <td align="left" valign="top" width="80%">
					    	<div class="tabletext">${OrderByIdViewList.deliveryDate?if_exists}</div>
					    </td>
					</tr>
					<tr>
					  	<td align="left" valign="top" width="15%">
				      		<div class="tabletext" >&nbsp;</div>
				      	</td>
				    </tr>
				    <tr>
					    <td align="left" valign="top" width="15%">
					      <div class="tabletext" >&nbsp;<b>Sales Channel</b></div>
					    </td>
					    <td width="5" >&nbsp;</td>
					    <td align="left" valign="top" width="80%">
					    	<div class="tabletext">${OrderByIdViewList.salesChannel?if_exists}</div>
					    </td>
					</tr>
				</tbody>
			</table>
		</div>	
	</div>
</div>

<#--<form name="orderPdfActionf122" target="_blank" method="get"/>
<#frameSection title="order #${orderId} ${externalOrder?if_exists} ">
    <table width="100%" border="0" cellpadding="1" cellspacing="0">
      <@infoRow title="Order Date" content=OrderByIdViewList.orderDate?if_exists />
      <@infoSepBar/>
      <@infoRow title="Status" content=OrderByIdViewList.statusDesc?if_exists />
         <@infoSepBar/>
         <@infoRow title="Sales Channel" content=OrderByIdViewList.salesChannel?default("N/A")/>
         <@infoSepBar/>
         <@infoRow title="Trans Num#" content=OrderByIdViewList.transactionNumber?default("N/A")/>
	     <@infoRow title="Reg Num#" content=OrderByIdViewList.registerNumber?if_exists/>
      	 <@infoRow title="Trans Type#" content=OrderByIdViewList.transactionType?if_exists/>
       	 <@infoRow title="Cashier Num#" content=OrderByIdViewList.cashierNumber?if_exists/>
	  	 <@infoSepBar/>
     </form>
    </table>

</@frameSection>-->

</#if> 
