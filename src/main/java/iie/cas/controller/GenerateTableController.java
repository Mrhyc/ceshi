package iie.cas.controller;

import java.io.File;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import iie.cas.service.GenerateTableService;
import iie.cas.utils.ExportExcel;
import iie.cas.utils.XssfStyleUtil;
import iie.cas.utils.dataTable.DataTableRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

@RestController
@CrossOrigin
@RequestMapping("/generateTable")
@Api(tags = "自动生成表格")
public class GenerateTableController {
	
	@Autowired
	private GenerateTableService generateTableService;
	
	@ApiOperation(value = "添加字段", notes = "用户配置字段", httpMethod = "POST")
	@RequestMapping(value = "addTableHead")
	public Map<String, Object> addDataHead(@RequestParam(value="tableHead",required=false)String tableHead,@RequestParam(value="userId",required=true)String userId){
		Map<String, Object> map = new HashMap<String, Object>();
		map = generateTableService.addDataHead(tableHead,userId);
		return map;
	}
	
	@ApiOperation(value = "新增数据接口", notes = "新增数据接口传json数据", httpMethod = "POST")
	@RequestMapping(value = "addDataTable")
	public Map<String, Object> addDataTable(@RequestParam(value="jsonString",required=true)String jsonString){
		Map<String, Object> map = new HashMap<String, Object>();
		map = generateTableService.addDataTable(jsonString);
		return map;
	}
	
	@ApiOperation(value = "查询数据接口", notes = "查询数据接口传表头", httpMethod = "POST")
	@RequestMapping(value = "searchDataTable")
	public Map<String, Object> searchDataTable(@RequestParam(value="jsonString",required=false)String jsonString,HttpServletRequest request){
		Map<String, Object> map = new HashMap<String, Object>();
		DataTableRequest dataTableRequest = new DataTableRequest(request);
		map = generateTableService.searchDataTable(jsonString,dataTableRequest);
		return map;
	}
	
	@ApiOperation(value = "下载模板", notes = "导入模板下载", httpMethod = "POST")
	@RequestMapping(value = "downTeample")
	public void downTeample(@RequestParam(value="jsonString",required=true)String jsonString,HttpServletRequest request,HttpServletResponse response){
		Map<String, Object> map = new HashMap<String, Object>();
//		map = generateTableService.downTeample(jsonString,dataTableRequest);
		try {
			if(StringUtils.isNotBlank(jsonString)){
				jsonString = URLDecoder.decode(jsonString, "utf-8");
				//创建工作簿
				XSSFWorkbook excelFile = new XSSFWorkbook();
				//创建一个sheet
				XSSFSheet sheet = excelFile.createSheet();
				//配置表头 创建行
				sheet.createRow(0);
				//创建样式
				XSSFCellStyle style1 = XssfStyleUtil.xssfCellStyleHead(excelFile);
				XssfStyleUtil.xssfCellStyleInfo(excelFile);
				Map<String, String> tableDataMap =JSONObject.fromObject(jsonString);
				int i=0;
				for(String keys:tableDataMap.keySet()){
					XSSFRow row  = sheet.getRow(0);
					row.setHeightInPoints(30);
					row.createCell(i).setCellStyle(style1);
					row.getCell(i).setCellValue(tableDataMap.get(keys));
					i++;
				}			
				// 将数据写入文件
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");// 精确到毫秒
				String time = sdf.format(new Date());
				OutputStream os = response.getOutputStream();// 获取输出流
				response.reset();// 重置输出流
				response.setHeader("Content-Disposition",
						"attachment;filename=" + new String(("模板" + time + ".xlsx").getBytes("utf-8"), "iso8859-1"));
				response.setContentType("application/octet-stream");
				excelFile.write(os);
				os.close();
				excelFile.close();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	@ApiOperation(value = "导出数据", notes = "导入数据下载", httpMethod = "POST")
	@RequestMapping(value = "downTableData")
	public Map<String, Object> downTableData(@RequestParam(value="downTableData",required=true)String jsonString,HttpServletRequest request,HttpServletResponse response){
		Map<String, Object> map = new HashMap<String, Object>();
		map = generateTableService.downTableData(jsonString,response);
		return map;
	}
	
	@ApiOperation(value = "导入数据", notes = "导入数据", httpMethod = "POST")
	@RequestMapping(value = "importExcelData")
	public Map<String, Object> importExcelData(@RequestParam(value = "file", required = true) MultipartFile file,@RequestParam(value = "jsonString", required = true) String jsonString){
		Map<String, Object> map = new HashMap<String, Object>();
		File path = null;
		String pathFile ="/home/hyc/";
		try {
			if (file == null || file.isEmpty()) {
				map.put("path", null);
				map.put("status", "400");
				map.put("message", "文件为空!");
				return map;
			}
			// 上传文件
			File tmpfile = new File(pathFile);
			if (!tmpfile.exists()) {
				tmpfile.mkdirs();
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			String time = sdf.format(new Date());
			path = new File(pathFile + time + "-" + file.getOriginalFilename());
			if (path.exists()) {
				map.put("status", "400");
				map.put("message", "文件已存在!");
				return map;
			}
			file.transferTo(path);
			map = generateTableService.importExcelData(path,jsonString);
			path.delete();
			map.put("message", "成功");
			map.put("status", 200);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			map.put("message", "导入失败");
			map.put("status", 400);
		}
		return map;
	}
}
