import org.ofbiz.base.util.*;
import org.ofbiz.entity.util.*;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.UtilMisc;
import java.util.HashMap;

import org.ofbiz.entity.GenericValue
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.condition.EntityFieldValue;
import org.ofbiz.entity.condition.EntityFunction;
import java.util.LinkedHashMap;

import org.fio.admin.portal.constant.AdminPortalConstant
import org.fio.admin.portal.util.DataHelper;
import org.apache.commons.lang.StringUtils;


delegator = request.getAttribute("delegator");

inputContext = new LinkedHashMap<String, Object>();

String prodCatalogId = request.getParameter("prodCatalogId");

if(UtilValidate.isNotEmpty(prodCatalogId)) {
	productCatalogs = EntityUtil.getFirst(delegator.findByAnd("ProdCatalog", UtilMisc.toMap("prodCatalogId", prodCatalogId), null, false));
	if (productCatalogs != null) {
		inputContext.put("prodCatalogId", productCatalogs.getString("prodCatalogId"));
		inputContext.put("productCatalog", productCatalogs.getString("catalogName"));
		inputContext.put("sequenceNumber", productCatalogs.getString("sequenceNumber"));
		inputContext.put("isEnable", productCatalogs.getString("isEnable"));
	}
}


context.put("inputContext", inputContext);

