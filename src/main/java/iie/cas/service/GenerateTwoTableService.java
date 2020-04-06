package iie.cas.service;

import java.io.File;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import iie.cas.utils.dataTable.DataTableRequest;

public interface GenerateTwoTableService {
	
	Map<String,Object> addTableHead(String tableHead,String userId);
	
	void downTableData(String jsonString,HttpServletResponse response);
	
	Map<String, Object> searchDataTable(String jsonString,DataTableRequest dataTableRequest);
	
	Map<String, Object> importExcelData(File path,String jsonString);
}
