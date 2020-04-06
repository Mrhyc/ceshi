package iie.cas.service;

import java.io.File;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import iie.cas.utils.dataTable.DataTableRequest;

public interface GenerateTableService {
	Map<String, Object> addDataTable(String jsonString);
	
	Map<String, Object> addDataHead(String tableHead,String userId);
	
	Map<String, Object> searchDataTable(String jsonString,DataTableRequest dataTableRequest);
	
	Map<String, Object> downTableData(String jsonString,HttpServletResponse response);
	
	Map<String, Object> importExcelData(File path,String jsonString);
	
}
