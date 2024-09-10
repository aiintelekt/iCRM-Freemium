/**
 * 
 */
package org.fio.homeapps.export;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.fio.homeapps.ResponseCodes;
import org.fio.homeapps.constants.GlobalConstants;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;

/**
 * @author Sharif
 *
 */
public class ExcelExporter extends Exporter {
	
	private final static String MODULE = ExcelExporter.class.getName();
	
	private static ExcelExporter instance;
	
	public static synchronized ExcelExporter getInstance(){
        if(instance == null) {
            instance = new ExcelExporter();
        }
        return instance;
    }

	@Override
	protected Map<String, Object> doExporter(Map<String, Object> context) throws Exception {
		
		Delegator delegator = (Delegator) context.get("delegator");
		List<Map<String, Object>> rows = (List<Map<String, Object>>) context.get("rows");
		List<String> headers = (List<String>) context.get("headers");
		String fileName = (String) context.get("fileName");
		String location = (String) context.get("location"); 
		Boolean isHeaderRequird = (Boolean) context.get("isHeaderRequird");
		
		Map<String, Object> response = new HashMap<String, Object>();
		
		try {
			
			if (UtilValidate.isNotEmpty(rows)) {
				
				List<String> headerFields = new ArrayList<String>();
				Set<String> fieldSet = rows.get(0).keySet();
				if(isHeaderRequird) {

					if(headers != null && headers.size() > 0) {
						headerFields.addAll(headers);
					} else {
						// get the header data from the generic value
						fieldSet.forEach(e->{
							headerFields.add(e);
						});
					}
				}
				
				HSSFWorkbook workbook = new HSSFWorkbook();
		    	HSSFSheet sheet = workbook.createSheet("LeadFeed");
		    	
		    	int colNum = 0;
		    	HSSFRow excelRow = sheet.createRow(0);
		    	for(String key : fieldSet) {
		    		HSSFCell cell = excelRow.createCell(colNum++);
		    		cell.setCellValue((String) key);
		    	}
		    	
		    	int rowNum = 0;
				for(Map<String, Object> row : rows) {
					excelRow = sheet.createRow(++rowNum);
					colNum = 0;
					for(String key : fieldSet) {
						HSSFCell cell = excelRow.createCell(colNum++);
						cell.setCellValue((String) row.get(key));
					}
				}
				
				if(rowNum > 0) {
					String filePath = location+File.separatorChar+fileName;
					File file = new File(filePath);
					
					FileOutputStream outputStream = new FileOutputStream(file);
		            workbook.write(outputStream);
		            workbook.close();
		            outputStream.close();
					
					Debug.logInfo("Excel File Exported with "+ rowNum +" rows", MODULE);
				}
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			
			response.put(GlobalConstants.RESPONSE_CODE, ResponseCodes.INTERNAL_SERVER_ERROR_CODE);
			response.put(GlobalConstants.RESPONSE_MESSAGE, e.getMessage());
			
			return response;
		}
		
		response.put(GlobalConstants.RESPONSE_CODE, ResponseCodes.SUCCESS_CODE);
		
		return response;
	}

}
