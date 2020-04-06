package iie.cas.controller;

import java.io.File;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.util.CellRangeAddress;
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

import iie.cas.service.GenerateTwoTableService;
import iie.cas.utils.XssfStyleUtil;
import iie.cas.utils.dataTable.DataTableRequest;
import io.netty.handler.codec.base64.Base64Decoder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

@RestController
@CrossOrigin
@RequestMapping("/generateTwoTable")
@Api(tags = "自动生成表格")
public class GenerateTwoTableController {
	
	@Autowired
	private GenerateTwoTableService generateTwoTableService;
	
	
	@ApiOperation(value = "添加字段二级表头", notes = "用户配置字段二级表头", httpMethod = "POST")
	@RequestMapping(value = "addTableHead")
	public Map<String, Object> addTableHead(@RequestParam(value="tableHead",required=false)String tableHead,@RequestParam(value="userId",required=true)String userId){
		Map<String, Object> map = new HashMap<String, Object>();
		map = generateTwoTableService.addTableHead(tableHead,userId);
		return map;
	}
	
	@ApiOperation(value = "下载模板", notes = "下载模板", httpMethod = "POST")
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
				sheet.createRow(1);
				//创建样式
				XSSFCellStyle style1 = XssfStyleUtil.xssfCellStyleHead(excelFile);
				XSSFCellStyle style2 = XssfStyleUtil.xssfCellStyleInfo(excelFile);
				//基本信息:listMap
				Map<String, Object> oneTitleMap = JSONObject.fromObject(jsonString);
				//基本信息
				int firstCol = 0;
				for(String oneTitle:oneTitleMap.keySet()){
					//二级表头 list
					List<Map<String, Object>> twoList =(List<Map<String, Object>>) oneTitleMap.get(oneTitle);
							
					XSSFRow row = sheet.getRow(0);
					row.setHeightInPoints(30);//设置行高30
					for(int z=0;z<twoList.size();z++){
						row.createCell(firstCol+z).setCellStyle(style2);
					}
					sheet.addMergedRegion(new CellRangeAddress(0, 0, firstCol, firstCol+twoList.size()-1));
					row.getCell(firstCol).setCellValue(oneTitle);
					row = sheet.getRow(1);
					row.setHeightInPoints(30);
					for(int i=0;i<twoList.size();i++){
						row.createCell(firstCol+i).setCellStyle(style2);
						row.getCell(firstCol+i).setCellValue(twoList.get(i).get("ctype").toString());
					}
					firstCol+=twoList.size();
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
	public void downTableData(@RequestParam(value="downTableData",required=true)String jsonString,HttpServletRequest request,HttpServletResponse response){
		try {
			jsonString = URLDecoder.decode(jsonString, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		generateTwoTableService.downTableData(jsonString,response);
	}
	
	@ApiOperation(value = "查询数据接口", notes = "查询数据接口传表头", httpMethod = "POST")
	@RequestMapping(value = "searchDataTable")
	public Map<String, Object> searchDataTable(@RequestParam(value="jsonString",required=false)String jsonString,HttpServletRequest request){
		Map<String, Object> map = new HashMap<String, Object>();
		DataTableRequest dataTableRequest = new DataTableRequest(request);
		map = generateTwoTableService.searchDataTable(jsonString,dataTableRequest);
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
			map = generateTwoTableService.importExcelData(path,jsonString);
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
