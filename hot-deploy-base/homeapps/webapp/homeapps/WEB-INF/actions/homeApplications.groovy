import org.ofbiz.base.util.*;
import java.util.*;
import org.ofbiz.entity.*;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.util.EntityQuery;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.util.EntityUtil;

// security
security = request.getAttribute("security");
context.put("security", security);

// external login key
extLogin = request.getAttribute("externalLoginKey");

if (extLogin != null) {
    context.put("externalKeyParam", "externalLoginKey=" + requestAttributes.get("externalLoginKey"));
}
userLogin = request.getAttribute("userLogin");
println("userLogin.userLoginId:::::"+userLogin.userLoginId);
List<GenericValue> userLoginSecurityGroup = EntityQuery.use(delegator).from("UserLoginSecurityGroup")
                    .where(
                    EntityCondition.makeCondition("groupId",EntityOperator.LIKE,"VND_%"), 
                    EntityCondition.makeCondition("userLoginId", userLogin.userLoginId)
                    )
                    .cache().filterByDate().queryList();
println("userLoginSecurityGroup:::::"+userLoginSecurityGroup);
if(UtilValidate.isNotEmpty(userLoginSecurityGroup)){
   List<GenericValue> securityGroupPermission = EntityQuery.use(delegator).from("SecurityGroupPermission")
        .where(EntityCondition.makeCondition("groupId", EntityOperator.IN, EntityUtil.getFieldListFromEntityList(userLoginSecurityGroup, "groupId", true)))
        .queryList();
   List<String> permissionIds = EntityUtil.getFieldListFromEntityList(securityGroupPermission, "permissionId", true);
   println("securityGroupPermission:::::"+securityGroupPermission);
   if(UtilValidate.isNotEmpty(permissionIds)){
      List<GenericValue> componentAccess = EntityQuery.use(delegator).from("OfbizComponentAccess")
        .where(EntityCondition.makeCondition("permissionId", EntityOperator.IN, permissionIds))
        .queryList();
      println("componentAccess:::::"+componentAccess);
      if(UtilValidate.isNotEmpty(componentAccess)){
          context.put("componentAccess",componentAccess);
      }else{
        context.put("componentAccess","");
      }
   }
}

/*if(userLogin != null) {
	if(person != null && person.size()){
		String firstName = person.getString("firstName");
		String lastName = person.getString("lastName");	
		context.put("userName",firstName+" "+lastName);
	}
}*/
if(userLogin != null) {
	if(person != null && person.size()){
		String firstName = person.getString("firstName");
		String lastName = person.getString("lastName");
		String name = firstName;
		if(UtilValidate.isNotEmpty(lastName)) {
			name = name +" "+ lastName;
		}
		context.put("userName",name);
	}
}