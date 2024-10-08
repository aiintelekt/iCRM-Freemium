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

String productCategoryId = request.getParameter("productCategoryId");
String prodCatalogId = request.getParameter("prodCatalogId");
String subCategoryId = request.getParameter("subCategoryId");
String productId = request.getParameter("productId");

if(UtilValidate.isNotEmpty(prodCatalogId)&&UtilValidate.isNotEmpty(productCategoryId)&&UtilValidate.isNotEmpty(subCategoryId)&&UtilValidate.isNotEmpty(productId)) {
	productSubCategory = EntityUtil.getFirst(delegator.findByAnd("ProductCategoryCatalogAssoc", UtilMisc.toMap("catalogId", prodCatalogId,"categoryId",productCategoryId,"subCategoryId",subCategoryId,"productId",productId), ["productFromDate DESC"], false));
	if (productSubCategory != null) {
		inputContext.put("prodCatalogId", productSubCategory.getString("catalogId"));
		inputContext.put("productCategoryId", productSubCategory.getString("categoryId"));
		inputContext.put("subCategoryId", productSubCategory.getString("subCategoryId"));
		inputContext.put("productId", productSubCategory.getString("productId"));
		inputContext.put("product", productSubCategory.getString("productName"));
		inputContext.put("sequenceNumber", productSubCategory.getString("productSeqNum"));
		isEnable = "Y"
		if(UtilValidate.isNotEmpty(productSubCategory.getString("productThruDate")))
			isEnable = "N"
		inputContext.put("isEnable",isEnable );
		Debug.log("=="+productSubCategory.getString("productId"));
	}
}


context.put("inputContext", inputContext);

