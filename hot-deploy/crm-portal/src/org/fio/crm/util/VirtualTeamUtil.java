/**
 * 
 */
package org.fio.crm.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fio.crm.party.PartyHelper;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;

import javolution.util.FastList;

/**
 * @author Sharif
 *
 */
public class VirtualTeamUtil {
	public static final String MODULE = VirtualTeamUtil.class.getName();

	public static List<GenericValue> getVirtualTeamList(Delegator delegator, String countryGeoId) {
		
		List<GenericValue> virtualTeamList = new ArrayList<GenericValue>();
		
		try {
			
			List<EntityCondition> conditions = new ArrayList <EntityCondition>();
			conditions.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "ACCOUNT_TEAM"));
			conditions.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "PARTY_DISABLED"));
			
			if (UtilValidate.isNotEmpty(countryGeoId)) {
				conditions.add(EntityCondition.makeCondition("teamCountryGeoId", EntityOperator.EQUALS, countryGeoId));
			}
			
			Set<String> fieldToSelect = new HashSet<String>();
			fieldToSelect.add("partyId");
			fieldToSelect.add("statusId");
			fieldToSelect.add("groupName");
			fieldToSelect.add("roleTypeId");
			fieldToSelect.add("teamCountryGeoId");
			fieldToSelect.add("partyGroupComments");

			virtualTeamList = delegator.findList("PartyRoleStatusAndPartyDetail", EntityCondition.makeCondition(conditions, EntityOperator.AND), fieldToSelect, UtilMisc.toList("groupName"), null, false);
			return virtualTeamList;
			
		} catch (Exception e) {
			//e.printStackTrace();
    		Debug.logError(e.getMessage(), MODULE);

		}
		
		return virtualTeamList;
		
	}
	
	public static List<GenericValue> getVirtualTeamList(Delegator delegator, String countryGeoId, String virtualTeamMemberId) {
		
		List<GenericValue> virtualTeamList = new ArrayList<GenericValue>();
		
		try {
			
			List<Map<String, Object>> virtualTeamMemberList = getVirtualTeamMemberList(delegator, null, virtualTeamMemberId);
			if (UtilValidate.isNotEmpty(virtualTeamMemberList)) {
				for (Map<String, Object> virtualTeamMember : virtualTeamMemberList) {
					List<EntityCondition> conditions = new ArrayList <EntityCondition>();
					conditions.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "ACCOUNT_TEAM"));
					conditions.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "PARTY_DISABLED"));
					
					if (UtilValidate.isNotEmpty(countryGeoId)) {
						conditions.add(EntityCondition.makeCondition("teamCountryGeoId", EntityOperator.EQUALS, countryGeoId));
					}
					
					conditions.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, virtualTeamMember.get("virtualTeamId")));
					
					Set<String> fieldToSelect = new HashSet<String>();
					fieldToSelect.add("partyId");
					fieldToSelect.add("statusId");
					fieldToSelect.add("groupName");
					fieldToSelect.add("roleTypeId");
					fieldToSelect.add("teamCountryGeoId");
					fieldToSelect.add("partyGroupComments");
					
					GenericValue virtualTeam = EntityUtil.getFirst( delegator.findList("PartyRoleStatusAndPartyDetail", EntityCondition.makeCondition(conditions, EntityOperator.AND), fieldToSelect, UtilMisc.toList("groupName"), null, false) );
					if (UtilValidate.isNotEmpty(virtualTeam)) {
						virtualTeamList.add(virtualTeam);
					}
				}
			}
			
		} catch (Exception e) {
			//e.printStackTrace();
    		Debug.logError(e.getMessage(), MODULE);

		}
		
		return virtualTeamList;
		
	}
	
	public static List<Map<String, Object>> getVirtualTeamMemberList(Delegator delegator, String virtualTeamId, String virtualTeamMemberId) {
		
		List<Map<String, Object>> virtualTeamMemberList = new ArrayList<Map<String, Object>>();
		
		try {
			
			if (UtilValidate.isEmpty(virtualTeamId) && UtilValidate.isEmpty(virtualTeamMemberId)) {
				return virtualTeamMemberList;
			}
			
			List<EntityCondition> conditionList = FastList.newInstance();
			conditionList.add(EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("roleTypeIdFrom", EntityOperator.EQUALS, "ACCOUNT_TEAM"),
				EntityCondition.makeCondition("partyRelationshipTypeId", EntityOperator.EQUALS, "ASSIGNED_TO"),
				EntityUtil.getFilterByDateExpr()
			));
			
			if (UtilValidate.isNotEmpty(virtualTeamId)) {
				conditionList.add(EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, virtualTeamId));
			}
			if (UtilValidate.isNotEmpty(virtualTeamMemberId)) {
				conditionList.add(EntityCondition.makeCondition("partyIdTo", EntityOperator.EQUALS, virtualTeamMemberId));
			}
			
		    List<GenericValue> partyRelationships = delegator.findList("PartyRelationship", EntityCondition.makeCondition(conditionList, EntityOperator.AND), null, null, null, false);
		    
		    if (UtilValidate.isNotEmpty(partyRelationships)) {
		    	for (GenericValue partyRelationship : partyRelationships) {
		    		
		    		Map<String, Object> teamMember = new HashMap<String, Object>();
		    		
		    		teamMember.put("virtualTeamId", partyRelationship.getString("partyIdFrom"));
		    		teamMember.put("virtualTeamName", PartyHelper.getPartyName(delegator, partyRelationship.getString("partyIdFrom"), false));
		    		
		    		teamMember.put("virtualTeamMemberId", partyRelationship.getString("partyIdTo"));
		    		teamMember.put("virtualTeamMemberName", PartyHelper.getPartyName(delegator, partyRelationship.getString("partyIdTo"), false));
		    		
		    		teamMember.put("securityGroupId", partyRelationship.getString("securityGroupId"));
		    		
		    		virtualTeamMemberList.add(teamMember);
		    	}
		    }
			
		} catch (Exception e) {
			//e.printStackTrace();
    		Debug.logError(e.getMessage(), MODULE);

		}
		
		return virtualTeamMemberList;
		
	}
	
	public static Map<String, Object> getFirstVirtualTeamMember(Delegator delegator, String virtualTeamMemberId) {
		
		return getFirstVirtualTeamMember(delegator, null, virtualTeamMemberId);
		
	}
	
	public static Map<String, Object> getFirstVirtualTeamMember(Delegator delegator, String virtualTeamId, String virtualTeamMemberId) {
		
		List<Map<String, Object>> virtualTeamMemberList = getVirtualTeamMemberList(delegator, virtualTeamId, virtualTeamMemberId);
		
		if (UtilValidate.isNotEmpty(virtualTeamMemberList)) {
			return virtualTeamMemberList.get(0);
		}
		
		return new HashMap<String, Object>();
		
	}
	
	public static Map<String, Object> getVirtualTeamLead(Delegator delegator, String virtualTeamId) {
		
		List<Map<String, Object>> virtualTeamMemberList = getVirtualTeamMemberList(delegator, virtualTeamId, null);
		
		if (UtilValidate.isNotEmpty(virtualTeamMemberList)) {
			for (Map<String, Object> virtualTeamMember : virtualTeamMemberList) {
				if (UtilValidate.isNotEmpty(virtualTeamMember.get("securityGroupId"))){
						if(((String)virtualTeamMember.get("securityGroupId")).equals("VT_SG_TL")) 
								return virtualTeamMember;
				}
			}
		}
		
		return new HashMap<String, Object>();
		
	}
	
	public static String getVirtualTeamId(Delegator delegator, String virtualTeamMemberId) {
		
		List<Map<String, Object>> virtualTeamMemberList = getVirtualTeamMemberList(delegator, null, virtualTeamMemberId);
		
		if (UtilValidate.isNotEmpty(virtualTeamMemberList)) {
			return (String) virtualTeamMemberList.get(0).get("virtualTeamId");
		}
		
		return null;
		
	}
	
	public static boolean isVirtualTeamMember(Delegator delegator, String virtualTeamId, String partyId) {
		
		List<Map<String, Object>> virtualTeamMemberList = getVirtualTeamMemberList(delegator, virtualTeamId, partyId);
		
		if (UtilValidate.isNotEmpty(virtualTeamMemberList)) {
			return true;
		}
		
		return false;
		
	}
	
	public static Set<String> getVirtualTeamIds(List<Map<String, Object>> virtualTeamMemberList, String securityGroupId, boolean isEqual) {
		
		Set<String> virtualTeamIdList = new HashSet<String>();
		
		if (UtilValidate.isNotEmpty(virtualTeamMemberList)) {
			for (Map<String, Object> virtualTeamMember : virtualTeamMemberList) {
				if (isEqual) {
					if (UtilValidate.isNotEmpty(securityGroupId) && virtualTeamMember.get("securityGroupId").equals(securityGroupId)) {
						virtualTeamIdList.add((String) virtualTeamMember.get("virtualTeamId"));
					}
				} else {
					if (UtilValidate.isNotEmpty(securityGroupId) && !virtualTeamMember.get("securityGroupId").equals(securityGroupId)) {
						virtualTeamIdList.add((String) virtualTeamMember.get("virtualTeamId"));
					}
				}
			}
		}
		
		return virtualTeamIdList;
	}
	public static List<String> getVirtualTeamIds(Delegator delegator, String virtualTeamMemberId) {
		List<String> virtualTeamIdList = new ArrayList<String>();
		List<Map<String, Object>> virtualTeamMemberList = getVirtualTeamMemberList(delegator, null, virtualTeamMemberId);
		
		if (UtilValidate.isNotEmpty(virtualTeamMemberList)) {
			for (Map<String, Object> virtualTeamMember : virtualTeamMemberList) {
				virtualTeamIdList.add((String) virtualTeamMember.get("virtualTeamId"));
			/*return (String) virtualTeamMemberList.get(0).get("virtualTeamId");*/
			}
			return virtualTeamIdList;
		}
		
		return null;
		
	}
}
