/**
 * 
 */
package org.fio.crm.writer;

import java.util.Map;

/**
 * @author Sharif
 *
 */
public interface Writer {

	public Map<String, Object> write(Map<String, Object> context);
	
}
