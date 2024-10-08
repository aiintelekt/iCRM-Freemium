/*
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
 */

package org.fio.dataimport;

import org.apache.commons.lang.StringUtils;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.datasource.GenericHelperInfo;
import org.ofbiz.entity.jdbc.SQLProcessor;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityQuery;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import javolution.util.FastList;
import javolution.util.FastMap;

public class SupplierImportServices {

    public static String module = SupplierImportServices.class.getName();

    public static Map<String, Object> importSuppliers(DispatchContext dctx, Map<String, ?> context) {

        String organizationPartyId = (String) context.get("organizationPartyId");
        int imported = 0;

        try {
            // then import the tax rates for the counties
            OpentapsImporter supplierImporter = new OpentapsImporter("DataImportSupplier", dctx, new SupplierDecoder(organizationPartyId));
            imported += supplierImporter.runImport(context);
        } catch (GenericEntityException e) {
            return UtilMessage.createAndLogServiceError(e, module);
        }
        Map<String, Object> result = ServiceUtil.returnSuccess();
        result.put("importedRecords", imported);
        return result;
    }
}

// maps DataImportSupplier into a set of opentaps entities that describes the Supplier
class SupplierDecoder implements ImportDecoder {
    public static final String module = SupplierDecoder.class.getName();
    protected String organizationPartyId;

    public SupplierDecoder(String organizationPartyId) {
        this.organizationPartyId = organizationPartyId;
    }

    public List<GenericValue> decode(GenericValue entry, Timestamp importTimestamp, Delegator delegator, LocalDispatcher dispatcher, Object... args) throws Exception {
        List<GenericValue> toBeStored = FastList.newInstance();

        String baseCurrencyUomId =  null;
        //String baseCurrencyUomId = UtilCommon.getOrgBaseCurrency(organizationPartyId, delegator);
        //GenericValue orgAcctgPref = delegator.findByPrimaryKeyCache("PartyAcctgPreference", UtilMisc.toMap("partyId", organizationPartyId));
        GenericValue orgAcctgPref =  EntityQuery.use(delegator).from("PartyAcctgPreference").where("partyId", organizationPartyId).cache().queryFirst();
        if(UtilValidate.isNotEmpty(orgAcctgPref)){
        	 baseCurrencyUomId = orgAcctgPref.getString("baseCurrencyUomId");
        }

        Debug.logInfo("Now processing  supplier name [" + entry.get("supplierName") +"]", module);
        

        /***********************/
        /** update Party data **/
        /***********************/
        GenericValue person1 = null;
        String partyId = null;
        String validParty = null;
        List<GenericValue> partyAttributes = delegator.findByAnd("PartyIdentification",UtilMisc.toMap("partyIdentificationTypeId",entry.getString("source"),"idValue",entry.getString("supplierId")),null,false);
		if(UtilValidate.isNotEmpty(partyAttributes)){
			partyId = partyAttributes.get(0).getString("partyId");
		}
		
		if (UtilValidate.isEmpty(partyId)) {

			person1 = delegator.findOne("PartyGroup", UtilMisc.toMap("partyId", partyId),false);

		}
		
		if(UtilValidate.isEmpty(partyAttributes)){
        Map<String, Object> validateResponse = validateCustomer(entry, delegator);
        if (UtilValidate.isNotEmpty(validateResponse.get("action"))) {
			if ("UPDATE".equals(validateResponse.get("action"))) {
				person1 = delegator.findOne("PartyGroup", UtilMisc.toMap("partyId", validateResponse.get("partyId")),false);
				partyId = (String) validateResponse.get("partyId");
			} else if ("SUCCESS".equals(validateResponse.get("action"))) {

			}
		}
		}
		
        if(UtilValidate.isEmpty(partyId)){
        	GenericValue userLogin = delegator.findOne("UserLogin", false, UtilMisc.toMap("userLoginId","admin"));
        	//update basic details
        	Map<String,Object> input;
        	input = FastMap.newInstance();
        	input.put("partyId", partyId);
        	input.put("groupName", entry.getString("supplierName"));
        	input.put("userLogin", userLogin);
        	input.put("isIncorporated", entry.getString("isIncorporated"));
        	input.put("federalTaxId", entry.getString("federalTaxId"));
        	input.put("requires1099", entry.getString("requires1099"));
        	input.put("userLogin", userLogin);
        	
        	Map<String,Object> result = dispatcher.runSync("updatePartyGroup", input);
        	
        	//update phone contact
        	GenericValue findShippingContactMech = EntityUtil.getFirst(delegator.findByAnd("PartyContactMechPurpose",UtilMisc.toMap("partyId",partyId,"contactMechPurposeTypeId","PRIMARY_PHONE"),null,false));
        	if(UtilValidate.isNotEmpty(findShippingContactMech)){
        		String contactMechId = findShippingContactMech.getString("contactMechId");
        		
        		GenericValue phoneContactMech = delegator.findOne("TelecomNumber", false, UtilMisc.toMap("contactMechId",contactMechId));
        		//phoneContactMech.put("contactMechId", contactMechId);
        		phoneContactMech.put("countryCode", entry.getString("primaryPhoneCountryCode"));
        		phoneContactMech.put("areaCode", entry.getString("primaryPhoneAreaCode"));
        		phoneContactMech.put("contactNumber", entry.getString("primaryPhoneNumber"));
        		phoneContactMech.store();
        		
        		GenericValue partyContactMech = EntityUtil.getFirst(delegator.findByAnd("PartyContactMech",UtilMisc.toMap("partyId",partyId,"contactMechId",contactMechId),null,false));
        		if(UtilValidate.isNotEmpty(partyContactMech)){
        		partyContactMech.put("extension", entry.getString("primaryPhoneExtension"));
        		partyContactMech.store();
        		}
        		
        	}
        	else if (!UtilValidate.isEmpty(entry.getString("primaryPhoneNumber"))) {
                // associate this as PRIMARY_PHONE
                GenericValue contactMech = delegator.makeValue("ContactMech", UtilMisc.toMap("contactMechId", delegator.getNextSeqId("ContactMech"), "contactMechTypeId", "TELECOM_NUMBER"));
                String telecomContactMechId = contactMech.getString("contactMechId");
                GenericValue primaryNumber = UtilImport.makeTelecomNumber(contactMech, entry.getString("primaryPhoneCountryCode"), entry.getString("primaryPhoneAreaCode"), entry.getString("primaryPhoneNumber"), delegator);
                toBeStored.add(contactMech);
                toBeStored.add(primaryNumber);
                toBeStored.add(UtilImport.makeContactMechPurpose("PRIMARY_PHONE", primaryNumber, partyId, importTimestamp, delegator));
                toBeStored.add(delegator.makeValue("PartyContactMech", UtilMisc.toMap("contactMechId", telecomContactMechId, "partyId", partyId, "fromDate", importTimestamp, "extension", entry.getString("primaryPhoneExtension"),"allowSolicitation","Y")));
              //  partySupplementalData.set("primaryTelecomNumberId", telecomContactMechId);
            }
        	
        	//update secondary number
        	
        	//update phone contact
        	/*List<GenericValue> findSecondContactMech = delegator.findByAnd("PartyContactMech",UtilMisc.toMap("partyId",partyId),null,false);
        	if(UtilValidate.isNotEmpty(findSecondContactMech)){
        		for(GenericValue gv : findSecondContactMech){
        			String contactMechId = gv.getString("contactMechId");
            		
            		GenericValue phoneContactMech = delegator.findOne("TelecomNumber", false, UtilMisc.toMap("contactMechId",contactMechId));
            		GenericValue checkPurpose = EntityUtil.getFirst(delegator.findByAnd("PartyContactMechPurpose", UtilMisc.toMap("partyId",partyId,"contactMechId",contactMechId),null,false));
            		if(UtilValidate.isNotEmpty(phoneContactMech) && UtilValidate.isEmpty(checkPurpose)){
                		phoneContactMech.put("countryCode", entry.getString("secondaryPhoneCountryCode"));
                		phoneContactMech.put("areaCode", entry.getString("secondaryPhoneAreaCode"));
                		phoneContactMech.put("contactNumber", entry.getString("secondaryPhoneNumber"));
                		phoneContactMech.store();
            		}
        		}
        	}*/
        	
        	GenericValue findSecondaryContactMech = EntityUtil.getFirst(delegator.findByAnd("PartyContactMechPurpose",UtilMisc.toMap("partyId",partyId,"contactMechPurposeTypeId","PHONE_WORK_SEC"),null,false));
        	if(UtilValidate.isNotEmpty(findSecondaryContactMech)){
        	 String contactMechId = findSecondaryContactMech.getString("contactMechId");
        		GenericValue phoneContactMech = delegator.findOne("TelecomNumber", false, UtilMisc.toMap("contactMechId",contactMechId));
        		phoneContactMech.put("countryCode", entry.getString("secondaryPhoneCountryCode"));
        		phoneContactMech.put("areaCode", entry.getString("secondaryPhoneAreaCode"));
        		phoneContactMech.put("contactNumber", entry.getString("secondaryPhoneNumber"));
        		phoneContactMech.store();
        		
        		GenericValue partyContactMech = EntityUtil.getFirst(delegator.findByAnd("PartyContactMech",UtilMisc.toMap("partyId",partyId,"contactMechId",contactMechId),null,false));
        		if(UtilValidate.isNotEmpty(partyContactMech)){
        		partyContactMech.put("extension", entry.getString("secondaryPhoneExtension"));
        		partyContactMech.store();
        		}
        	}
        	else if (!UtilValidate.isEmpty(entry.getString("secondaryPhoneNumber"))) {
                // this one has no contactmech purpose type
                GenericValue contactMech = delegator.makeValue("ContactMech", UtilMisc.toMap("contactMechId", delegator.getNextSeqId("ContactMech"), "contactMechTypeId", "TELECOM_NUMBER"));
                GenericValue secondaryNumber = UtilImport.makeTelecomNumber(contactMech, entry.getString("secondaryPhoneCountryCode"), entry.getString("secondaryPhoneAreaCode"), entry.getString("secondaryPhoneNumber"), delegator);
                toBeStored.add(contactMech);
                toBeStored.add(secondaryNumber);
                toBeStored.add(UtilImport.makeContactMechPurpose("PHONE_WORK_SEC", secondaryNumber, partyId, importTimestamp, delegator));
                toBeStored.add(delegator.makeValue("PartyContactMech", UtilMisc.toMap("contactMechId", contactMech.get("contactMechId"), "partyId", partyId, "fromDate", importTimestamp, "extension", entry.getString("secondaryPhoneExtension"),"allowSolicitation","Y")));
            }
        	
        	//update email

         	GenericValue emailPurpose = EntityUtil.getFirst(delegator.findByAnd("PartyContactMechPurpose",UtilMisc.toMap("partyId",partyId,"contactMechPurposeTypeId","PRIMARY_EMAIL"),null,false));
         	if(UtilValidate.isNotEmpty(emailPurpose)){
         		GenericValue emailContactMech = delegator.findOne("ContactMech", false, UtilMisc.toMap("contactMechId",emailPurpose.getString("contactMechId")));
             	emailContactMech.put("infoString", entry.getString("emailAddress"));
             	emailContactMech.store();
         	}
         	else if(!UtilValidate.isEmpty(entry.getString("emailAddress"))) {
                // make the email address
                GenericValue emailContactMech = delegator.makeValue("ContactMech", UtilMisc.toMap("contactMechId", delegator.getNextSeqId("ContactMech"), "contactMechTypeId", "EMAIL_ADDRESS", "infoString", entry.getString("emailAddress")));
                String emailContactMechId = emailContactMech.getString("contactMechId");
                toBeStored.add(emailContactMech);

                toBeStored.add(delegator.makeValue("PartyContactMech", UtilMisc.toMap("contactMechId", emailContactMechId, "partyId", partyId, "fromDate", importTimestamp,"allowSolicitation","Y")));
                toBeStored.add(UtilImport.makeContactMechPurpose("PRIMARY_EMAIL", emailContactMech, partyId, importTimestamp, delegator));
               // partySupplementalData.set("primaryEmailId", emailContactMechId);
            }

         	
         	//update webAddress
         	
         	GenericValue webPurpose = EntityUtil.getFirst(delegator.findByAnd("PartyContactMechPurpose",UtilMisc.toMap("partyId",partyId,"contactMechPurposeTypeId","PRIMARY_WEB_URL"),null,false));
         	if(UtilValidate.isNotEmpty(webPurpose)){
         		//webContactMech.put("contactMechId", webPurpose.getString("contactMechId"));
         		GenericValue webContactMech = delegator.findOne("ContactMech", false, UtilMisc.toMap("contactMechId",webPurpose.getString("contactMechId")));
             	webContactMech.put("infoString", entry.getString("webAddress"));
             	webContactMech.store();
         	}
         	else if (!UtilValidate.isEmpty(entry.getString("webAddress"))) {
                // make the web address
                GenericValue webContactMech = delegator.makeValue("ContactMech", UtilMisc.toMap("contactMechId", delegator.getNextSeqId("ContactMech"), "contactMechTypeId", "WEB_ADDRESS", "infoString", entry.getString("webAddress")));
                toBeStored.add(webContactMech);

                toBeStored.add(delegator.makeValue("PartyContactMech", UtilMisc.toMap("contactMechId", webContactMech.get("contactMechId"), "partyId", partyId, "fromDate", importTimestamp,"allowSolicitation","Y")));
                toBeStored.add(UtilImport.makeContactMechPurpose("PRIMARY_WEB_URL", webContactMech, partyId, importTimestamp, delegator));
            }
         	
        	//update fax contact
        	GenericValue findFaxContactMech = EntityUtil.getFirst(delegator.findByAnd("PartyContactMechPurpose",UtilMisc.toMap("partyId",partyId,"contactMechPurposeTypeId","FAX_NUMBER"),null,false));
        	if(UtilValidate.isNotEmpty(findFaxContactMech)){
        		String contactMechId = findFaxContactMech.getString("contactMechId");
        		GenericValue faxContactMech = delegator.findOne("TelecomNumber", false, UtilMisc.toMap("contactMechId",contactMechId));
        		faxContactMech.put("countryCode", entry.getString("faxCountryCode"));
        		faxContactMech.put("areaCode", entry.getString("faxAreaCode"));
        		faxContactMech.put("contactNumber", entry.getString("faxNumber"));
        		faxContactMech.store();
        		
        	}
        	else if(!UtilValidate.isEmpty(entry.getString("faxNumber"))) {
                // associate this as FAX_NUMBER
                GenericValue contactMech = delegator.makeValue("ContactMech", UtilMisc.toMap("contactMechId", delegator.getNextSeqId("ContactMech"), "contactMechTypeId", "TELECOM_NUMBER"));
                GenericValue faxNumber = UtilImport.makeTelecomNumber(contactMech, entry.getString("faxCountryCode"), entry.getString("faxAreaCode"), entry.getString("faxNumber"), delegator);
                toBeStored.add(contactMech);
                toBeStored.add(faxNumber);
                toBeStored.add(UtilImport.makeContactMechPurpose("FAX_NUMBER", faxNumber, partyId, importTimestamp, delegator));
                toBeStored.add(delegator.makeValue("PartyContactMech", UtilMisc.toMap("contactMechId", contactMech.get("contactMechId"), "partyId", partyId, "fromDate", importTimestamp,"allowSolicitation","Y")));
            }
        	
        	//update did contact
        	GenericValue findDidContactMech = EntityUtil.getFirst(delegator.findByAnd("PartyContactMechPurpose",UtilMisc.toMap("partyId",partyId,"contactMechPurposeTypeId","PHONE_DID"),null,false));
        	if(UtilValidate.isNotEmpty(findDidContactMech)){
        		String contactMechId = findDidContactMech.getString("contactMechId");
        		GenericValue didContactMech = delegator.findOne("TelecomNumber", false, UtilMisc.toMap("contactMechId",contactMechId));
        		didContactMech.put("countryCode", entry.getString("didCountryCode"));
        		didContactMech.put("areaCode", entry.getString("didAreaCode"));
        		didContactMech.put("contactNumber", entry.getString("didNumber"));
        		didContactMech.store();
        		
        		GenericValue partyContactMech = EntityUtil.getFirst(delegator.findByAnd("PartyContactMech",UtilMisc.toMap("partyId",partyId,"contactMechId",contactMechId),null,false));
        		if(UtilValidate.isNotEmpty(partyContactMech)){
        		partyContactMech.put("extension", entry.getString("didExtension"));
        		partyContactMech.store();
        		}
        	}
        	else if(!UtilValidate.isEmpty(entry.getString("didNumber"))) {
                // associate this as PHONE_DID
                GenericValue contactMech = delegator.makeValue("ContactMech", UtilMisc.toMap("contactMechId", delegator.getNextSeqId("ContactMech"), "contactMechTypeId", "TELECOM_NUMBER"));
                GenericValue didNumber = UtilImport.makeTelecomNumber(contactMech, entry.getString("didCountryCode"), entry.getString("didAreaCode"), entry.getString("didNumber"), delegator);
                toBeStored.add(contactMech);
                toBeStored.add(didNumber);

                toBeStored.add(UtilImport.makeContactMechPurpose("PHONE_DID", didNumber, partyId, importTimestamp, delegator));
                toBeStored.add(delegator.makeValue("PartyContactMech", UtilMisc.toMap("contactMechId", contactMech.get("contactMechId"), "partyId", partyId, "fromDate", importTimestamp, "extension", entry.getString("didExtension"),"allowSolicitation","Y")));
            }
        	
        	//update Postal Address
        	GenericValue findPostalContactMech = EntityUtil.getFirst(delegator.findByAnd("PartyContactMechPurpose",UtilMisc.toMap("partyId",partyId,"contactMechPurposeTypeId","GENERAL_LOCATION"),null,false));
        	if(UtilValidate.isNotEmpty(findPostalContactMech)){
        		String contactMechId = findPostalContactMech.getString("contactMechId");
        		GenericValue postalContactMech = delegator.findOne("PostalAddress", false, UtilMisc.toMap("contactMechId",contactMechId));
        		postalContactMech.put("toName", entry.getString("supplierName"));
        		postalContactMech.put("attnName", entry.getString("attnName"));
        		postalContactMech.put("address1", entry.getString("address1"));
        		postalContactMech.put("address2", entry.getString("address2"));
        		postalContactMech.put("city", entry.getString("city"));
        		postalContactMech.put("postalCode", entry.getString("postalCode"));
        		postalContactMech.put("postalCodeExt", entry.getString("postalCodeExt"));
        		postalContactMech.put("countryGeoId", entry.getString("countryGeoId"));
        		postalContactMech.put("stateProvinceGeoId", entry.getString("stateProvinceGeoId"));
        		postalContactMech.store();        	       		
        	}
        	else if (!UtilValidate.isEmpty(entry.getString("address1"))) {
                // associate this as the GENERAL_LOCATION and BILLING_LOCATION
                GenericValue contactMech = delegator.makeValue("ContactMech", UtilMisc.toMap("contactMechId", delegator.getNextSeqId("ContactMech"), "contactMechTypeId", "POSTAL_ADDRESS"));
                String postalAddressContactMechId = contactMech.getString("contactMechId");
                GenericValue mainPostalAddress = UtilImport.makePostalAddress(contactMech, entry.getString("supplierName"), "", "", entry.getString("attnName"), entry.getString("address1"), entry.getString("address2"), entry.getString("city"), entry.getString("stateProvinceGeoId"), entry.getString("postalCode"), entry.getString("postalCodeExt"), entry.getString("countryGeoId"), delegator);
                toBeStored.add(contactMech);
                toBeStored.add(mainPostalAddress);
                toBeStored.add(UtilImport.makeContactMechPurpose("GENERAL_LOCATION", mainPostalAddress, partyId, importTimestamp, delegator));
                toBeStored.add(UtilImport.makeContactMechPurpose("BILLING_LOCATION", mainPostalAddress, partyId, importTimestamp, delegator));
                toBeStored.add(UtilImport.makeContactMechPurpose("PRIMARY_LOCATION", mainPostalAddress, partyId, importTimestamp, delegator));
                toBeStored.add(delegator.makeValue("PartyContactMech", UtilMisc.toMap("contactMechId", postalAddressContactMechId, "partyId", partyId, "fromDate", importTimestamp,"allowSolicitation","Y")));
              //  partySupplementalData.set("primaryPostalAddressId", postalAddressContactMechId);
            }
        	
        	//update Shipping Postal Address
        	GenericValue shipPostalContactMech = EntityUtil.getFirst(delegator.findByAnd("PartyContactMechPurpose",UtilMisc.toMap("partyId",partyId,"contactMechPurposeTypeId","SHIPPING_LOCATION"),null,false));
        	if(UtilValidate.isNotEmpty(shipPostalContactMech)){
        		String contactMechId = shipPostalContactMech.getString("contactMechId");
        		GenericValue shipContactMech = delegator.findOne("PostalAddress", false, UtilMisc.toMap("contactMechId",contactMechId));
        		if(UtilValidate.isEmpty(entry.getString("supplierName")))
        			shipContactMech.put("toName", entry.getString("firstName")+" "+entry.getString("lastName"));
        		else
        			shipContactMech.put("toName", entry.getString("supplierName"));
        		shipContactMech.put("attnName", entry.getString("shipToAttnName"));
        		shipContactMech.put("address1", entry.getString("shipToAddress1"));
        		shipContactMech.put("address2", entry.getString("shipToAddress2"));
        		shipContactMech.put("city", entry.getString("shipToCity"));
        		shipContactMech.put("postalCode", entry.getString("shipToPostalCode"));
        		shipContactMech.put("postalCodeExt", entry.getString("shipToPostalCodeExt"));
        		shipContactMech.put("countryGeoId", entry.getString("shipToCountryGeoId"));
        		shipContactMech.put("stateProvinceGeoId", entry.getString("shipToStateProvinceGeoId"));
        		shipContactMech.store();
        		
        		
        	}
        	//update note
        	if (!UtilValidate.isEmpty(entry.getString("note"))) {
                // make the party note
        		GenericValue findNote = EntityUtil.getFirst(delegator.findByAnd("PartyNote",UtilMisc.toMap("partyId",partyId),null,false));
        		if(UtilValidate.isNotEmpty(findNote)){
        			GenericValue noteData = delegator.findOne("NoteData", false, UtilMisc.toMap("noteId",findNote.getString("noteId")));
        			noteData.put("noteInfo", entry.getString("note"));
        			noteData.store();
        		}
        		else if(!UtilValidate.isEmpty(entry.getString("note"))) {
                    // make the party note
                    GenericValue noteData = delegator.makeValue("NoteData", UtilMisc.toMap("noteId", delegator.getNextSeqId("NoteData"), "noteInfo", entry.getString("note"), "noteDateTime", importTimestamp));
                    toBeStored.add(noteData);
                    toBeStored.add(delegator.makeValue("PartyNote", UtilMisc.toMap("noteId", noteData.get("noteId"), "partyId", partyId)));
                }
            }

            if (!UtilValidate.isEmpty(entry.getString("source"))) {
            	GenericValue checkSource = delegator.findOne("PartyIdentification", UtilMisc.toMap("partyId", partyId,"partyIdentificationTypeId",entry.getString("source")), false);
            	if(UtilValidate.isEmpty(checkSource))
                    toBeStored.add(delegator.makeValue("PartyIdentification", UtilMisc.toMap( "partyId", partyId,"partyIdentificationTypeId",entry.getString("source"),"idValue",entry.getString("supplierId"))));
            }
         	
        	toBeStored.add(userLogin);
        	return toBeStored;
        	
        }

        /***********************/
        /** Import Party data **/
        /***********************/
        // create the company with the roles
        partyId = delegator.getNextSeqId("Party");
        String supplierPartyId = entry.getString("supplierId");     // use same partyId as the Supplier's supplierId
        toBeStored.addAll(UtilImport.makePartyWithRolesExt(partyId, "PARTY_GROUP", supplierPartyId , UtilMisc.toList("SUPPLIER"), delegator));
        GenericValue company = delegator.makeValue("PartyGroup", UtilMisc.toMap("partyId", partyId, "groupName", entry.getString("supplierName"), "isIncorporated", entry.getString("isIncorporated"), "federalTaxId", entry.getString("federalTaxId"), "requires1099", entry.getString("requires1099")));
        toBeStored.add(company);
        String primaryPartyName = org.ofbiz.party.party.PartyHelper.getPartyName(company);
        Debug.logInfo("Creating PartyGroup ["+partyId+"] for Supplier ["+entry.get("supplierId")+"].", module);

        //GenericValue partySupplementalData = delegator.makeValue("PartySupplementalData", UtilMisc.toMap("partyId", partyId, "companyName", primaryPartyName));

        /*******************************************************************************************************/
        /** Import contact mechs.  Note that each contact mech will be associated with the company and person. */
        /*******************************************************************************************************/

        if (!UtilValidate.isEmpty(entry.getString("address1"))) {
            // associate this as the GENERAL_LOCATION and BILLING_LOCATION
            GenericValue contactMech = delegator.makeValue("ContactMech", UtilMisc.toMap("contactMechId", delegator.getNextSeqId("ContactMech"), "contactMechTypeId", "POSTAL_ADDRESS"));
            String postalAddressContactMechId = contactMech.getString("contactMechId");
            GenericValue mainPostalAddress = UtilImport.makePostalAddress(contactMech, entry.getString("supplierName"), "", "", entry.getString("attnName"), entry.getString("address1"), entry.getString("address2"), entry.getString("city"), entry.getString("stateProvinceGeoId"), entry.getString("postalCode"), entry.getString("postalCodeExt"), entry.getString("countryGeoId"), delegator);
            toBeStored.add(contactMech);
            toBeStored.add(mainPostalAddress);

            toBeStored.add(UtilImport.makeContactMechPurpose("GENERAL_LOCATION", mainPostalAddress, partyId, importTimestamp, delegator));
            toBeStored.add(UtilImport.makeContactMechPurpose("BILLING_LOCATION", mainPostalAddress, partyId, importTimestamp, delegator));
            toBeStored.add(UtilImport.makeContactMechPurpose("PRIMARY_LOCATION", mainPostalAddress, partyId, importTimestamp, delegator));
            toBeStored.add(delegator.makeValue("PartyContactMech", UtilMisc.toMap("contactMechId", postalAddressContactMechId, "partyId", partyId, "fromDate", importTimestamp,"allowSolicitation","Y")));
          //  partySupplementalData.set("primaryPostalAddressId", postalAddressContactMechId);
        }

        if (!UtilValidate.isEmpty(entry.getString("primaryPhoneNumber"))) {
            // associate this as PRIMARY_PHONE
            GenericValue contactMech = delegator.makeValue("ContactMech", UtilMisc.toMap("contactMechId", delegator.getNextSeqId("ContactMech"), "contactMechTypeId", "TELECOM_NUMBER"));
            String telecomContactMechId = contactMech.getString("contactMechId");
            GenericValue primaryNumber = UtilImport.makeTelecomNumber(contactMech, entry.getString("primaryPhoneCountryCode"), entry.getString("primaryPhoneAreaCode"), entry.getString("primaryPhoneNumber"), delegator);
            toBeStored.add(contactMech);
            toBeStored.add(primaryNumber);

            toBeStored.add(UtilImport.makeContactMechPurpose("PRIMARY_PHONE", primaryNumber, partyId, importTimestamp, delegator));
            toBeStored.add(delegator.makeValue("PartyContactMech", UtilMisc.toMap("contactMechId", telecomContactMechId, "partyId", partyId, "fromDate", importTimestamp, "extension", entry.getString("primaryPhoneExtension"),"allowSolicitation","Y")));
          //  partySupplementalData.set("primaryTelecomNumberId", telecomContactMechId);
        }

        if (!UtilValidate.isEmpty(entry.getString("secondaryPhoneNumber"))) {
            // this one has no contactmech purpose type
            GenericValue contactMech = delegator.makeValue("ContactMech", UtilMisc.toMap("contactMechId", delegator.getNextSeqId("ContactMech"), "contactMechTypeId", "TELECOM_NUMBER"));
            GenericValue secondaryNumber = UtilImport.makeTelecomNumber(contactMech, entry.getString("secondaryPhoneCountryCode"), entry.getString("secondaryPhoneAreaCode"), entry.getString("secondaryPhoneNumber"), delegator);
            toBeStored.add(contactMech);
            toBeStored.add(secondaryNumber);
            toBeStored.add(UtilImport.makeContactMechPurpose("PHONE_WORK_SEC", secondaryNumber, partyId, importTimestamp, delegator));
            toBeStored.add(delegator.makeValue("PartyContactMech", UtilMisc.toMap("contactMechId", contactMech.get("contactMechId"), "partyId", partyId, "fromDate", importTimestamp, "extension", entry.getString("secondaryPhoneExtension"),"allowSolicitation","Y")));
        }

        if (!UtilValidate.isEmpty(entry.getString("faxNumber"))) {
            // associate this as FAX_NUMBER
            GenericValue contactMech = delegator.makeValue("ContactMech", UtilMisc.toMap("contactMechId", delegator.getNextSeqId("ContactMech"), "contactMechTypeId", "TELECOM_NUMBER"));
            GenericValue faxNumber = UtilImport.makeTelecomNumber(contactMech, entry.getString("faxCountryCode"), entry.getString("faxAreaCode"), entry.getString("faxNumber"), delegator);
            toBeStored.add(contactMech);
            toBeStored.add(faxNumber);

            toBeStored.add(UtilImport.makeContactMechPurpose("FAX_NUMBER", faxNumber, partyId, importTimestamp, delegator));
            toBeStored.add(delegator.makeValue("PartyContactMech", UtilMisc.toMap("contactMechId", contactMech.get("contactMechId"), "partyId", partyId, "fromDate", importTimestamp,"allowSolicitation","Y")));
        }

        if (!UtilValidate.isEmpty(entry.getString("didNumber"))) {
            // associate this as PHONE_DID
            GenericValue contactMech = delegator.makeValue("ContactMech", UtilMisc.toMap("contactMechId", delegator.getNextSeqId("ContactMech"), "contactMechTypeId", "TELECOM_NUMBER"));
            GenericValue didNumber = UtilImport.makeTelecomNumber(contactMech, entry.getString("didCountryCode"), entry.getString("didAreaCode"), entry.getString("didNumber"), delegator);
            toBeStored.add(contactMech);
            toBeStored.add(didNumber);

            toBeStored.add(UtilImport.makeContactMechPurpose("PHONE_DID", didNumber, partyId, importTimestamp, delegator));
            toBeStored.add(delegator.makeValue("PartyContactMech", UtilMisc.toMap("contactMechId", contactMech.get("contactMechId"), "partyId", partyId, "fromDate", importTimestamp, "extension", entry.getString("didExtension"),"allowSolicitation","Y")));
        }

        if (!UtilValidate.isEmpty(entry.getString("emailAddress"))) {
            // make the email address
            GenericValue emailContactMech = delegator.makeValue("ContactMech", UtilMisc.toMap("contactMechId", delegator.getNextSeqId("ContactMech"), "contactMechTypeId", "EMAIL_ADDRESS", "infoString", entry.getString("emailAddress")));
            String emailContactMechId = emailContactMech.getString("contactMechId");
            toBeStored.add(emailContactMech);

            toBeStored.add(delegator.makeValue("PartyContactMech", UtilMisc.toMap("contactMechId", emailContactMechId, "partyId", partyId, "fromDate", importTimestamp,"allowSolicitation","Y")));
            toBeStored.add(UtilImport.makeContactMechPurpose("PRIMARY_EMAIL", emailContactMech, partyId, importTimestamp, delegator));
           // partySupplementalData.set("primaryEmailId", emailContactMechId);
        }

        if (!UtilValidate.isEmpty(entry.getString("webAddress"))) {
            // make the web address
            GenericValue webContactMech = delegator.makeValue("ContactMech", UtilMisc.toMap("contactMechId", delegator.getNextSeqId("ContactMech"), "contactMechTypeId", "WEB_ADDRESS", "infoString", entry.getString("webAddress")));
            toBeStored.add(webContactMech);

            toBeStored.add(delegator.makeValue("PartyContactMech", UtilMisc.toMap("contactMechId", webContactMech.get("contactMechId"), "partyId", partyId, "fromDate", importTimestamp,"allowSolicitation","Y")));
            toBeStored.add(UtilImport.makeContactMechPurpose("PRIMARY_WEB_URL", webContactMech, partyId, importTimestamp, delegator));
        }

        //toBeStored.add(partySupplementalData);

        /*****************************/
        /** Import Party notes. **/
        /*****************************/

        if (!UtilValidate.isEmpty(entry.getString("note"))) {
            // make the party note
            GenericValue noteData = delegator.makeValue("NoteData", UtilMisc.toMap("noteId", delegator.getNextSeqId("NoteData"), "noteInfo", entry.getString("note"), "noteDateTime", importTimestamp));
            toBeStored.add(noteData);
            toBeStored.add(delegator.makeValue("PartyNote", UtilMisc.toMap("noteId", noteData.get("noteId"), "partyId", partyId)));
        }
        
        if (!UtilValidate.isEmpty(entry.getString("source"))) {
            // make the party note
            if (supplierPartyId != null) {
                toBeStored.add(delegator.makeValue("PartyIdentification", UtilMisc.toMap( "partyId", partyId,"partyIdentificationTypeId",entry.getString("source"),"idValue",entry.getString("supplierId"))));
            }
        }

        Long netPaymentDays = entry.getLong("netPaymentDays");
        if (netPaymentDays != null && netPaymentDays.longValue() > 0) {
            String agreementId = delegator.getNextSeqId("Agreement");

            Map<String, Object> agreement = UtilMisc.<String, Object>toMap("agreementId", agreementId);
            agreement.put("partyIdFrom", organizationPartyId);
            agreement.put("partyIdTo", partyId);
            agreement.put("roleTypeIdTo", "SUPPLIER");
            agreement.put("agreementTypeId", "PURCHASE_AGREEMENT");
            agreement.put("agreementDate", importTimestamp);
            agreement.put("fromDate", importTimestamp);
            agreement.put("statusId", "AGR_ACTIVE");
            agreement.put("description", "Supplier agreement" + (UtilValidate.isEmpty(primaryPartyName) ? "" : " for ") + primaryPartyName);
            toBeStored.add(delegator.makeValue("Agreement", agreement));

            int itemSeqId = 1;

           /* if (netPaymentDays != null && netPaymentDays.longValue() > 0) {
                itemSeqId++;*/
                Map<String, Object> netPaymentDaysItem = UtilMisc.<String, Object>toMap("agreementId", agreementId, "agreementItemSeqId", Integer.valueOf(itemSeqId).toString(), "agreementItemTypeId", "AGREEMENT_PAYMENT");
                toBeStored.add(delegator.makeValue("AgreementItem", netPaymentDaysItem));
                Map<String, Object> netPaymentDaysTerm = UtilMisc.toMap("agreementId", agreementId, "agreementTermId", delegator.getNextSeqId("AgreementTerm"), "termTypeId", "FIN_PAYMENT_TERM", "agreementItemSeqId", Integer.valueOf(itemSeqId).toString(), "termDays", netPaymentDays, "currencyUomId", baseCurrencyUomId);
                toBeStored.add(delegator.makeValue("AgreementTerm", netPaymentDaysTerm));
           /* }*/
        }

        entry.put("primaryPartyId", partyId);
        toBeStored.add(entry);

        return toBeStored;
    }
    
    
    
	public static Map<String, Object> validateCustomer(GenericValue entry, Delegator delegator) {


		List<String> dupCustList = getDupPartyList(entry, delegator);
		Map<String, Object> outMap = new HashMap<String, Object>();

		String email = (String) entry.get("emailAddress");
		String action = "";
		String partyId = null;

		if (dupCustList.size() == 0) {
			action = "CREATE";
		}  else {
			outMap = singleDupeValidate(dupCustList, entry, delegator);
		}

		return outMap;

	}
    
	public static List<String> getDupPartyList(GenericValue entry, Delegator delegator) {

		Map<String, Object> outMap = new HashMap<String, Object>();
		
		String groupName = (String) entry.get("supplierName");
		String zipCode = (String) entry.get("postalCode");
		String address = (String) entry.get("address1");
		String address1 = StringUtils.defaultIfEmpty(address, " ");

		String country = (String) entry.get("countryGeoId");

		String phone = (String) entry.get("primaryPhoneAreaCode") + (String) entry.get("primaryPhoneNumber");
		
		List<String> partyIdsList = FastList.newInstance();

		List<String> matchPartyList = FastList.newInstance();
		
		if (UtilValidate.isNotEmpty(groupName)&& UtilValidate.isNotEmpty(zipCode) && UtilValidate.isNotEmpty(address1)  && UtilValidate.isNotEmpty(country)) {
			
			String SqlQuery = "";

			String postal = zipCode;


				SqlQuery = "select  a.party_id,a.GROUP_NAME from party_group a, party_contact_mech c,  postal_address d where a.PARTY_ID=c.PARTY_ID and  d.CONTACT_MECH_ID=c.CONTACT_MECH_ID and  a.group_name ='" + groupName + "' and  d.postal_code = '" + postal + "'";

			
			if (UtilValidate.isNotEmpty(SqlQuery)) {

				// TODO
				List<String> PartySQlList = runSqlQuery(SqlQuery, delegator);

				if (UtilValidate.isNotEmpty(PartySQlList)) {
					for (String partyId : PartySQlList) {
						partyIdsList.add(partyId);
					}
				}

			}
			try {

				for (String partyId : partyIdsList) {

					GenericValue party = delegator.findOne("PartyGroup", UtilMisc.toMap("partyId", partyId),false);

					if (party != null) {

						String postalContact = getPrimaryContact(partyId, "GENERAL_LOCATION", delegator);

						if (postalContact != null) {
							GenericValue postalAddress = delegator.findOne("PostalAddress", UtilMisc.toMap("contactMechId", postalContact),false);

							if (postalAddress != null) {
								String address1s = postalAddress.getString("address1");
								String stateProvinceGeoIds = postalAddress.getString("stateProvinceGeoId");
								String countryGeoIds = postalAddress.getString("countryGeoId");
								if (UtilValidate.isNotEmpty(address1) && address1.equalsIgnoreCase(address1s)  && country.equals(countryGeoIds)) {
									matchPartyList.add(partyId);
								}
							}
						}
					}
				}

			} catch (GenericEntityException ex) {

			}
			
		}


		return matchPartyList;

	}
	
	public static List<String> runSqlQuery(String query, Delegator delegator) {

		ResultSet rs = null;
		ArrayList<String> resultList = new ArrayList<String>();
		String selGroup = "org.ofbiz";

		String sqlCommandSeq = query;

		if (sqlCommandSeq != null && sqlCommandSeq.length() > 0 && selGroup != null && selGroup.length() > 0) {

			String helperName = delegator.getGroupHelperName(selGroup);
			GenericHelperInfo ghi = delegator.getGroupHelperInfo("org.ofbiz");
			SQLProcessor dumpSeq = new SQLProcessor(delegator,ghi);

			try {
				if (sqlCommandSeq.toUpperCase().startsWith("SELECT")) {

					rs = dumpSeq.executeQuery(sqlCommandSeq);

					while (rs.next()) {
						resultList.add(rs.getString(1));
					}
				}
			} catch (Exception e) {
			}finally {
				try { 
					if (rs != null) {
						rs.close();
					}
				} catch (Exception e) {
				}
		}
		}
		return resultList;
	}
	
	public static String getPrimaryContact(String partyId, String ContactMecPurpose, Delegator delegator) {
		String contactMechId = null;

		try {

			List<EntityCondition> conditon = UtilMisc.<EntityCondition> toList(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId), EntityCondition.makeCondition("contactMechPurposeTypeId", EntityOperator.EQUALS, ContactMecPurpose));

			EntityCondition findConditions1 = EntityCondition.makeCondition(conditon, EntityOperator.AND);

			EntityFindOptions findOpt1 = new EntityFindOptions();
			findOpt1.setDistinct(true);

			List<String> orderBy1 = FastList.newInstance();
			orderBy1.add("fromDate DESC");

			List<GenericValue> pcpurpose = delegator.findList("PartyContactMechPurpose", findConditions1, null, orderBy1, findOpt1,false);
			pcpurpose = EntityUtil.filterByDate(pcpurpose);
			if (UtilValidate.isNotEmpty(pcpurpose)) {
				GenericValue phoneContact = EntityUtil.getFirst(pcpurpose);
				pcpurpose = delegator.findByAnd("PartyContactMech", UtilMisc.toMap("partyId", phoneContact.getString("partyId"), "contactMechId", phoneContact.getString("contactMechId")),null,false);
			}

			pcpurpose = EntityUtil.filterByDate(pcpurpose, true);

			GenericValue partyContactMech = EntityUtil.getFirst(pcpurpose);

			if (partyContactMech != null) {
				contactMechId = partyContactMech.getString("contactMechId");
			}

		} catch (GenericEntityException ex) {

		}

		return contactMechId;

	}
	
	public static Map<String, Object> singleDupeValidate(List<String> dupCustList, GenericValue entry, Delegator delegator) {
		String partyIdt = dupCustList.get(0);

	
		String email = (String) entry.get("emailAddress");

		Map<String, Object> rsponseMap = new HashMap<String, Object>();

		if (UtilValidate.isNotEmpty(email)) {
			// Getting Primary Email
			String emailContactMc = getPrimaryContact(partyIdt, "PRIMARY_EMAIL", delegator);
			if (UtilValidate.isNotEmpty(emailContactMc)) {

				try {

					GenericValue contactMech = delegator.findOne("ContactMech", UtilMisc.toMap("contactMechId", emailContactMc),false);
					if (contactMech != null && email.equals(contactMech.getString("infoString"))) {
						rsponseMap.put("action", "UPDATE");
						rsponseMap.put("partyId", partyIdt);
						// TODO;

					} else {
						// emailNotExist = true;
						// TODO;

						rsponseMap.put("action", "UPDATE");
						rsponseMap.put("partyId", partyIdt);

					}
				} catch (GenericEntityException ex) {

				}

			}else{
				rsponseMap.put("action", "UPDATE");
				rsponseMap.put("partyId", partyIdt);
			}

		}

		return rsponseMap;
	}
}
