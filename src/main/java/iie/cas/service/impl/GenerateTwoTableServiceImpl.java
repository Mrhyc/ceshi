package iie.cas.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import iie.cas.dao.GenerateTwoTableElasticsearchRepository;
import iie.cas.po.GenerateTablePo;
import iie.cas.po.GenerateTwoTablePo;
import iie.cas.service.GenerateTwoTableService;
import iie.cas.utils.ReadExcel;
import iie.cas.utils.XssfStyleUtil;
import iie.cas.utils.dataTable.DataTableRequest;
import iie.cas.utils.dataTable.Order;
import net.sf.json.JSONObject;

@Service
public class GenerateTwoTableServiceImpl implements GenerateTwoTableService {

	@Autowired
	private GenerateTwoTableElasticsearchRepository generateTwoTableElasticsearchRepository;
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Override
	public Map<String, Object> addTableHead(String tableHead, String userId) {
		// TODO Auto-generated method stub
		Map<String, Object> map = new HashMap<>();
		try {
			if (StringUtils.isNotBlank(tableHead)) {
				Map<String, String> jsonMap = JSONObject.fromObject(tableHead);
				redisTemplate.opsForHash().put("dataHeadTwo", userId, JSON.toJSONString(jsonMap));
				map.put("message", "插入成功");
			}
			if (redisTemplate.opsForHash().hasKey("dataHeadTwo", userId)) {
				map.put("data", redisTemplate.opsForHash().get("dataHeadTwo", userId));
				map.put("message", "查询成功");
			} else {
				map.put("data", "");
			}
			map.put("status", 200);
		} catch (Exception e) {
			// TODO: handle exception
			map.put("message", "查询失败");
			map.put("status", 400);
		}
		return map;
	}

	@Override
	public void downTableData(String jsonString, HttpServletResponse response) {
		// TODO Auto-generated method stub
		Map<String, Object> map = new HashMap<>();
		try {
			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
					.withPageable(PageRequest.of(0, 1000)).build();
			Page<GenerateTwoTablePo> pageGenerateTable = generateTwoTableElasticsearchRepository.search(searchQuery);
			List<GenerateTwoTablePo> pageGenerateTables = pageGenerateTable.getContent();
			if (StringUtils.isNotBlank(jsonString)) {
				// 创建工作簿
				XSSFWorkbook excelFile = new XSSFWorkbook();
				// 创建一个sheet
				XSSFSheet sheet = excelFile.createSheet();
				// 配置表头 创建行
				sheet.createRow(0);
				sheet.createRow(1);
				// 创建样式
				XSSFCellStyle style1 = XssfStyleUtil.xssfCellStyleHead(excelFile);
				XSSFCellStyle style2 = XssfStyleUtil.xssfCellStyleInfo(excelFile);
				// 基本信息:listMap
				Map<String, Object> oneTitleMap = JSONObject.fromObject(jsonString);
				// 基本信息
				int firstCol = 1;
				XSSFRow row = null;
				//id
				row=sheet.getRow(0);
				row.createCell(0).setCellStyle(style1);
				row.getCell(0).setCellValue("id");
				row=sheet.getRow(1);
				row.createCell(1).setCellStyle(style1);
				row.getCell(1).setCellValue("id");
				String field = "";
				for (String oneTitle : oneTitleMap.keySet()) {
					// 二级表头 list
					List<Map<String, Object>> twoList = (List<Map<String, Object>>) oneTitleMap.get(oneTitle);
					row = sheet.getRow(1);
					row.setHeightInPoints(30);// 设置行高30
					// 合并单元格 // 合并单元格，cellRangAddress四个参数，第一个起始行，第二终止行，第三个起始列，第四个终止列
					sheet.addMergedRegion(new CellRangeAddress(0, 0, firstCol, firstCol + twoList.size() - 1));
					row.createCell(firstCol).setCellStyle(style1);
					row.getCell(firstCol).setCellValue(oneTitle);
					row = sheet.getRow(1);
					row.setHeightInPoints(30);
					for (int i = 0; i < twoList.size(); i++) {
						row.createCell(firstCol + i).setCellStyle(style2);
						row.getCell(firstCol + i).setCellValue(twoList.get(i).get("ctype").toString());
						field += twoList.get(i).get("type").toString() + ",";
					}
					firstCol += twoList.size();
				}
				int i = 2;
				int rows = 0;
				for (GenerateTwoTablePo generateTwoTablePo : pageGenerateTables) {
					sheet.createRow(i);
					row = sheet.getRow(i);
					row.setHeightInPoints(30);
					for (String string : field.substring(0, field.length() - 1).split(",")) {
						row.createCell(rows).setCellStyle(style2);
						row.getCell(rows).setCellValue(generateTwoTablePo.get(string));
						rows++;
					}
					rows = 0;
					i++;
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");// 精确到毫秒
				String time = sdf.format(new Date());
				OutputStream os = response.getOutputStream();// 获取输出流
				response.reset();// 重置输出流
				response.setHeader("Content-Disposition",
						"attachment;filename=" + new String(("双层表头" + time + ".xlsx").getBytes("utf-8"), "iso8859-1"));
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
	
	@Override
	public Map<String, Object> searchDataTable(String jsonString, DataTableRequest dataTableRequest) {
		// TODO Auto-generated method stub
		// Map<String, Object> map = new HashMap<>();
		Map<String, Object> results = new HashMap<String, Object>();
		if (dataTableRequest.orders == null || dataTableRequest.orders.size() == 0) {
			Order order = new Order();
			order.column = "id";
			order.dir = "desc";
			dataTableRequest.orders.add(order);
		}
		if (StringUtils.isBlank(jsonString)) {
			results.put("message", "请先勾选字段");
			results.put("status", 400);
			return results;
		}
		List<Map<String, String>> listResult = new ArrayList<Map<String, String>>();
		try {
			// String column = dataTableRequest.orders.get(0).column;
			// String dir = dataTableRequest.orders.get(0).dir;
			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			// BoolQueryBuilder boolQueryBuilder1 = QueryBuilders.boolQuery();
			// boolQueryBuilder1.should(QueryBuilders.multiMatchQuery("12","ceshi","ceshi","acb"));
			// boolQueryBuilder.must(boolQueryBuilder1);
			SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
					.withPageable(PageRequest.of(dataTableRequest.start / dataTableRequest.length, dataTableRequest.length))
					.build();
			Page<GenerateTwoTablePo> generateTwoTablePoContent = generateTwoTableElasticsearchRepository.search(searchQuery);
			long recordsTotal = generateTwoTablePoContent.getTotalElements();
			List<GenerateTwoTablePo> generateTwoTablePos = generateTwoTablePoContent.getContent();
			Map<String, String> resultData = new HashMap<>();
			List<String> listData = Arrays.asList(jsonString.split(","));
			
			for (GenerateTwoTablePo generateTwoTablePo : generateTwoTablePos) {
				resultData = new HashMap<>();
				for (String string : listData) {
					resultData.put(string, generateTwoTablePo.containsKey(string) ? generateTwoTablePo.get(string) : "");
				}
				resultData.put("id", generateTwoTablePo.getId());
				listResult.add(resultData);
			}
			results.put("recordsFiltered", recordsTotal);
			results.put("draw", dataTableRequest.draw);
			results.put("status", "200");
			results.put("message", "成功");
			results.put("recordsTotal", recordsTotal);
			results.put("data", listResult);
		} catch (Exception e) {
			// TODO: handle exception
			results.put("recordsFiltered", 0);
			results.put("draw", dataTableRequest.draw);
			results.put("status", "200");
			results.put("message", "成功");
			results.put("recordsTotal", 0);
			results.put("data", listResult);
			e.printStackTrace();
		}
		
		return results;
	}
	
	
	@Override
	public Map<String, Object> importExcelData(File path, String jsonString) {
		// TODO Auto-generated method stub
		Map<String, Object> map = new HashMap<>();
		try {
			String fileType = path.getName().substring(path.getName().lastIndexOf(".") + 1, path.getName().length());
			Workbook workbook = ReadExcel.getWorkbook(new FileInputStream(path), fileType);
			// 读取第一张sheet
			Sheet sheet = workbook.getSheetAt(0);
			// 读取最后一行
			int rowLast = sheet.getLastRowNum();
			// 读取第一行表头
			Row row0 = sheet.getRow(0);
			Row row1 = sheet.getRow(1);
			String oneTitleName = "";
			String twoTitleName = "";
			jsonString = URLDecoder.decode(jsonString, "utf-8");
			Map<String, Object> dataHead  = JSONObject.fromObject(jsonString);
			if(row0.getLastCellNum()==row1.getLastCellNum()){
				for(int i =0;i<row1.getLastCellNum();i++){
					if (StringUtils.isNotBlank(row0.getCell(i).toString())) {
						oneTitleName = row0.getCell(i).toString();// 合并的单元格只有第一个单元有数据，故遇无数据单元格时默认与前一个单元格数据相同
					}
					//取出二级菜单
					List<Map<String, Object>> listMap = (List<Map<String, Object>>) dataHead.get(oneTitleName);
//					twoTitleName+=listMap.stream().filter(e->e.get("ctype").toString().equals(row1.getCell(i).toString())).findFirst().get().get("type")+",";
					for(Map<String, Object> map2 : listMap){
						if(map2.get("ctype").toString().equals(row1.getCell(i).toString())){
							twoTitleName+=map2.containsKey("type")?map2.get("type")+",":"";
							continue;
						}
					}
				}
				List<GenerateTwoTablePo> listGenerateTwoTablePos = new ArrayList<>();
				String[] twoTitleNameArr = twoTitleName.substring(0, twoTitleName.length()-1).split(",");
				int rowStart = 2;
				for (int i = rowStart; i <= rowLast; i++) {
					row1 = sheet.getRow(i);
					GenerateTwoTablePo generateTwoTablePo = new GenerateTwoTablePo();
					for(int j=0;j<twoTitleNameArr.length;j++){
						generateTwoTablePo.put(twoTitleNameArr[j], ReadExcel.getCellValue(row1.getCell(j)));
					}
					if (generateTwoTablePo.containsKey("id")) {
						generateTwoTablePo.setId(generateTwoTablePo.get("id"));
						generateTwoTablePo.remove("id");
					}
					listGenerateTwoTablePos.add(generateTwoTablePo);
					if(listGenerateTwoTablePos.size()==1000){
						generateTwoTableElasticsearchRepository.saveAll(listGenerateTwoTablePos);
						listGenerateTwoTablePos = new ArrayList<>();
					}
				}
				
				if(listGenerateTwoTablePos!=null&&listGenerateTwoTablePos.size()>0){
					generateTwoTableElasticsearchRepository.saveAll(listGenerateTwoTablePos);
				}
				
			}else{
				map.put("status", 400);
				map.put("message", "一级二级标题占用列数不同");
			}
			 
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return map;
	}
}
