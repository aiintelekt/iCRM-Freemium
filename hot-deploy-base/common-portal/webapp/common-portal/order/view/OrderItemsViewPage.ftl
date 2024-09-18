<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.9.0/jquery.min.js"> </script>
<script type="text/javascript">
    var reReservationWidgetParameters = new Object();
</script>
<#if Static["org.fio.homeapps.util.DataUtil"].isPhoneCampaignEnabled(delegator)?if_exists!="Y" || !Static["org.fio.homeapps.util.DataUtil"].isPhoneCampaignEnabled(delegator)??>
	<#assign hideNotRelatedPhone = true>
<#else>
	<#assign hideNotRelatedPhone = false>
</#if>
<#if OrderByIdViewList12?exists>
<#macro sectionSepBar>
<tr>
    <td colspan="6"></td>
    <td colspan="2">
        <hr class="sepbar"/>
    </td>
</tr>
</#macro>
<div class="dash-panel" style="margin: 15px 0px 0px 0px !important;">
    <h4 class="orderitem-title">Order Items</h4>
</div>
<div class='dash-panel' style="margin: 15px 0px 0px 0px !important;">
    <form name="createReturnAction1" url="createReturnFromOrder" orderId="${orderId}" />
    <input type="hidden" value="${orderId}" id="orderId" />
    <table width="100%" border="0" cellpadding="0" cellspacing="0">
        <tbody>
            <tr align="left" valign="bottom">
                <#assign requestURI = request.getRequestURI()/>
                <#assign isClientPortal = false />
                <#if requestURI.contains("/client-portal/")>
                <#assign isClientPortal = true />
                </#if>
                <td width="<#if hideNotRelatedPhone>30%<#else>50%</#if>" align="left">
                    <div class="tableheadtext">
                        <h4>Product</h4>
                    </div>
                </td>
                
                <td width="10%" align="left">
                    <div class="tableheadtext">
                        <h4>Line Sequence</h4>
                    </div>
                </td>
                
                <#if hideNotRelatedPhone> 
                <td width="10%" align="left">
                    <div class="tableheadtext">
                        <h4>SR Number</h4>
                    </div>
                </td> 
                <td width="10%" align="left">
                    <div class="tableheadtext">
                        <h4>Customer PO</h4>
                    </div>
                </td>
                </#if>
                <td width="5%" align="center">
                    <div class="tableheadtext">
                        <h4>Quantity</h4>
                    </div>
                </td>
                <#if !(isClientPortal)>
                <td width="10%" align="right">
                    <div class="tableheadtext">
                        <h4>Unit Price</h4>
                    </div>
                </td>
                <td width="10%" align="right">
                    <div class="tableheadtext">
                        <h4>Sell Price</h4>
                    </div>
                </td>
                <#--  <td width="10%" align="right">
                    <div class="tableheadtext">
                        <h4>Adjustments</h4>
                    </div>
                </td>-->
                <td width="10%" align="right">
                    <div class="tableheadtext">
                        <h4>Extended Sell Price</h4>
                    </div>
                </td>
                </#if>
                <td width="5%">&nbsp;</td>
            </tr>
            <#list OrderByIdViewList12 as item>   
            <tr>
                <td colspan="10">
                    <hr class="sepbar">
                </td>
            </tr>
            <tr>
                <#if item.productName?exists>
                <td align="left" colspan="1" valign="top">${item.skuNumber!} - ${item.productName?if_exists}</td>
                <#else>
                <td align="left" colspan="1" valign="top">${item.skuNumber!}</td>
                </#if>
                
                <td align="left" colspan="1" valign="top">
                    <div class="tabletext" >${item.sequenceNumber?if_exists}</div>
                </td>
                <#if hideNotRelatedPhone> 
                <td align="left" colspan="1" valign="top">
                    <div class="tabletext" >${StringUtil.wrapString(item.srNumber?if_exists)}</div>
                </td>
                <td align="left" colspan="1" valign="top">
                    <div class="tabletext" >${item.purchaseOrder?if_exists}</div>
                </td>
                </#if>
                <td align="right" valign="top" nowrap="nowrap">
                    <table>
                        <tbody>
                            <tr valign="top">
                                <td>
                                    <div class="tabletext">${item.quantitySold?default(0)}</div>
                                </td>
                                    <#-- 
                                    <div class="tabletext">Cancelled:${item.numberOfItemsCancelled?default(0)}</div>
                                    <div class="tabletext">Net:${item.netQa?if_exists}</div>
                                </td>
                                -->
                                <#-- 
                                <td>
                                    <div class="tabletext">Shortfalled:${item.shortFalled?default(0)}</div>
                                    <div class="tabletext">Reserved:${item.reserved?default(0)}</div>
                                    <div class="tabletext">Shipped:${item.numberOfItemsShipped?default(0)}</div>
                                    <div class="tabletext">Outstanding:${item.Outstanding?default(0)}</div>
                                    <div class="tabletext">Invoiced:${item.invoiced?default(0)}</div>
                                    <div class="tabletext">Returned:${item.numberOfReturns?default(0)}</div>
                                </td>
                                -->
                            </tr>
                        </tbody>
                    </table>
                </td>
                <#if !(isClientPortal)>
                <td align="right" valign="top" >
                    <@ofbizCurrency amount=item.get("unitCostActual")?default(0.00) isoCode=currencyUomId/>
                </td>
                <td align="right" valign="top" >
                    <@ofbizCurrency amount=item.get("unitRetail")?default(0.00) isoCode=currencyUomId/>
                </td>
                <#-- <td align="right" valign="top">
                    <@ofbizCurrency amount=item.get("totalAdjustmentNonTaxAndDiscount")?default(0.00) isoCode=currencyUomId/>
                </td> -->
                <td align="right" valign="top" >
                    <@ofbizCurrency amount=item.get("subTotal")?default(0.00) isoCode=currencyUomId/>
                </td>
                </#if>
                <td>&nbsp;</td>
                <td align="right" valign="top">
                    &nbsp;
                </td>
            </tr>
            <tr>
                <td style="padding-top : 10px; padding-bottom : 10px;" colspan="3">
                </td>
            </tr>
            <tr>
                <td align="right" colspan="2">
                    <div class="tabletext" style="font-size: xx-small;" >
                        <b><i>Adjustment</i>:</b>
                        <b>Discount</b>:
                        <#if item.couponNumber?exists >${item.couponNumber}:</#if>
                        <#if item.discTransactionType?exists >${item.discTransactionType})</#if>
                    </div>
				</td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
                <td align="right">
                    <div class="tabletext" style="font-size: xx-small;" >
                        <@ofbizCurrency amount=item.get("totalAdjustmentNonTaxAndDiscount")?default(0.00) isoCode=currencyUomId/>        
                    </div>
                </td>
                <#-- 
                <td>&nbsp;</td>
                <td>&nbsp;</td>
                <td align="right">
                    <div class="tabletext" style="font-size: xx-small;" >
                        <@ofbizCurrency amount=item.get("totalAdjustmentNonTaxAndDiscount")?default(0.00) isoCode=currencyUomId/>        
                    </div>
                </td>
                -->
            </tr>
            </#list>
            <#if OrderByIdViewList12?exists>    
            <tr>
                <td colspan="9" >
                    <hr class="sepbar">
                </td>
            </tr>
            <tr>
                <td align="right" colspan="7">
                    <div class="tabletext"><b>Total Other Order Adjustments</b></div>
                </td>
                <td align="right" nowrap="nowrap">
                    <div class="tabletext"><#if totalAmount?has_content><@ofbizCurrency amount="${totalAdjustments?if_exists}" isoCode=OrderByIdViewList.currencyUom/><#else>$0.00</#if></div>
                </td>
            </tr>
            <tr>
                <td align="right" colspan="7">
                    <div class="tabletext"><b>Items Sub Total</b></div>
                </td>
                <td align="right" nowrap="nowrap">
                    <div class="tabletext"><#if totalAmount?has_content><@ofbizCurrency amount="${totalAmount?if_exists}" isoCode=OrderByIdViewList.currencyUom/><#else>$0.00</#if></div>
                </td>
            </tr>
            <tr>
                <td align="right" colspan="7">
                    <div class="tabletext"><b> Total Sales Tax</b></div>
                </td>
                <td align="right" nowrap="nowrap">
                    <div class="tabletext"><#if totalTaxAmount?has_content><@ofbizCurrency amount="${totalTaxAmount?if_exists}" isoCode=OrderByIdViewList.currencyUom/><#else>$0.00</#if></div>
                </td>
            </tr>
            <#--
            <tr>
                <td align="right" colspan="5">
                    <div class="tabletext"><b>Order Total Shipping And Handling</b></div>
                </td>
                <td align="right" nowrap="nowrap">
                    <div class="tabletext"><@ofbizCurrency amount=OrderByIdViewList.openAmount  isoCode=OrderByIdViewList.currencyUom/></div>
                </td>
            </tr>
            -->
            <@sectionSepBar/>
            <#-- 
            <tr>
                <td align="right" colspan="5">
                    <div class="tabletext"><b>Sales Tax</b></div>
                </td>
                <td align="right" nowrap="nowrap">
                    <div class="tabletext"><@ofbizCurrency amount="${totalTaxAmount?if_exists}" /></div>
                </td>
            </tr>
            -->
            <#-- grand total -->
            <tr>
                <td align="right" colspan="7">
                    <div class="tabletext"><b>Order Total</b></div>
                </td>
                <td align="right" nowrap="nowrap">
                    <div class="tabletext"><#if orderTotal?has_content><@ofbizCurrency amount="${orderTotal?if_exists}" /></#if></div>
                </td>
            </tr>
            <#--
            <tr>
                <td align="right" colspan="5">
                    <div class="tabletext"><b>Total Due</b></div>
                </td>
                <td align="right" nowrap="nowrap">
                    <div class="tabletext"><@ofbizCurrency amount=OrderByIdViewList.openAmount isoCode=OrderByIdViewList.currencyUom/></div>
                </td>
            </tr>
            -->
            </#if>
    </table>
    </#if>
</div>