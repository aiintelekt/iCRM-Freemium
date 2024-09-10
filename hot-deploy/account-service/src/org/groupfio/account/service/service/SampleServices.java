/**
 * 
 */
package org.groupfio.account.service.service;

import java.util.HashMap;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

/**
 * @author Sharif
 *
 */
public class SampleServices {

	private static final String MODULE = SampleServices.class.getName();
    
    public static Map testService(DispatchContext dctx, Map context) {
    	
    	
    	
    	Map<String, Object> result = new HashMap<String, Object>();
    	
    	try {
        	
    		
    		
    	} catch (Exception e) {
    		Debug.logError(e.getMessage(), MODULE);
    		result.putAll(ServiceUtil.returnError(e.getMessage()));
			return result;
		}
    	
    	result.putAll(ServiceUtil.returnSuccess("Successfully execute test service.."));
    	
    	return result;
    	
    }
    
}
