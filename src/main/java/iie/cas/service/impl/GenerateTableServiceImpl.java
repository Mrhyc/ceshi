package iie.cas.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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

import iie.cas.dao.GenerateTableElasticsearchRepository;
import iie.cas.po.GenerateTablePo;
import iie.cas.service.GenerateTableService;
import iie.cas.utils.ExportExcel;
import iie.cas.utils.ReadExcel;
import iie.cas.utils.dataTable.DataTableRequest;
import iie.cas.utils.dataTable.Order;
import net.sf.json.JSONObject;

@Service
public class GenerateTableServiceImpl implements GenerateTableService {

	@Autowired
	private GenerateTableElasticsearchRepository generateTableElasticsearchRepository;
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Override
	public Map<String, Object> addDataHead(String tableHead, String userId) {
		// TODO Auto-generated method stub
		Map<String, Object> map = new HashMap<>();
		try {
			if (StringUtils.isNotBlank(tableHead)) {
				Map<String, String> jsonMap = JSONObject.fromObject(tableHead);
				redisTemplate.opsForHash().put("dataHead", userId, JSON.toJSONString(jsonMap));
				map.put("message", "插入成功");
			}
			if (redisTemplate.opsForHash().hasKey("dataHead", userId)) {
				map.put("data", redisTemplate.opsForHash().get("dataHead", userId));
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
	public Map<String, Object> addDataTable(String jsonString) {
		// TODO Auto-generated method stub
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, String> jsonStringMap = JSONObject.fromObject(jsonString);
		GenerateTablePo generateTablePo = new GenerateTablePo();
		generateTablePo.putAll(jsonStringMap);
		generateTableElasticsearchRepository.save(generateTablePo);
		map.put("message", "插入成功");
		map.put("status", 200);
		return map;
	}

	@Override
	public Map<String, Object> searchDataTable(String jsonString, DataTableRequest dataTableRequest) {
		// TODO Auto-generated method stub
		// Map<String, Object> map = new HashMap<>();
		List<Map<String, String>> listResult = new ArrayList<Map<String, String>>();
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
		// String column = dataTableRequest.orders.get(0).column;
		// String dir = dataTableRequest.orders.get(0).dir;
		try {
			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			// BoolQueryBuilder boolQueryBuilder1 = QueryBuilders.boolQuery();
			// boolQueryBuilder1.should(QueryBuilders.multiMatchQuery("12","ceshi","ceshi","acb"));
			// boolQueryBuilder.must(boolQueryBuilder1);
			SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
					.withPageable(PageRequest.of(dataTableRequest.start / dataTableRequest.length, dataTableRequest.length))
					.build();
			Page<GenerateTablePo> pageGenerateTable = generateTableElasticsearchRepository.search(searchQuery);
			long recordsTotal = pageGenerateTable.getTotalElements();
			List<GenerateTablePo> pageGenerateTables = pageGenerateTable.getContent();
			Map<String, String> resultData = new HashMap<>();
			List<String> listData = Arrays.asList(jsonString.split(","));
			for (GenerateTablePo generateTablePo : pageGenerateTables) {
				resultData = new HashMap<>();
				for (String string : listData) {
					resultData.put(string, generateTablePo.containsKey(string) ? generateTablePo.get(string) : "");
				}
				resultData.put("id", generateTablePo.getId());
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
		}
		return results;
	}

	@Override
	public Map<String, Object> downTableData(String jsonString, HttpServletResponse response) {
		// TODO Auto-generated method stub
		Map<String, Object> map = new HashMap<>();
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
				.withPageable(PageRequest.of(0, 1000)).build();
		Page<GenerateTablePo> pageGenerateTable = generateTableElasticsearchRepository.search(searchQuery);
		List<GenerateTablePo> pageGenerateTables = pageGenerateTable.getContent();
		Map<String, String> tableHead = JSONObject.fromObject(jsonString);
		String chines = "";
		List<String> englishs = new ArrayList<>();
		;
		for (String string : tableHead.keySet()) {
			englishs.add(string);
			chines += tableHead.get(string) + ",";
		}
		try {
			((ExportExcel<GenerateTablePo>) (obj, row) -> {
				for (int i = 0; i < englishs.size(); i++) {
					if (i == 0) {
						row.createCell(0, Cell.CELL_TYPE_STRING).setCellValue(obj.getId());
						continue;
					}
					row.createCell(i, Cell.CELL_TYPE_STRING).setCellValue(obj.get(englishs.get(i)));
				}
			}).exportExcel(response, pageGenerateTables, ("id" + chines.substring(0, chines.length() - 1)).split(","),
					"数据导出-" + System.currentTimeMillis() / 1000);
			map.put("message", "导出成功");
			map.put("status", 200);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			map.put("message", "导出失败");
			map.put("status", 400);
		}
		return map;
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
			Row row = sheet.getRow(0);
			// 获取当前行的列数
			int colNum = row.getLastCellNum();
			// 存 2个map 一个map为中文对英文 一个map为index和中文
			Map<Integer, String> indexToEnglish = new HashMap<>();
			for (int k = 0; k < colNum; k++) {
				indexToEnglish.put(k, row.getCell(k).toString());
			}
			// 将key value调换
			jsonString = URLDecoder.decode(jsonString, "utf-8");
			Map<String, String> mapKeysValue = JSONObject.fromObject(jsonString);
			Map<String, String> mapValueKeys = new HashMap<>();
			mapKeysValue.put("id", "id");
			for (String key : mapKeysValue.keySet()) {
				mapValueKeys.put(mapKeysValue.get(key), key);
			}
			// 记录第一行
			int rowStart = 1;
			List<GenerateTablePo> listGenerateTablePos = new ArrayList<>();
			for (int i = rowStart; i <= rowLast; i++) {
				// 获取第i行
				row = sheet.getRow(i);
				GenerateTablePo generateTablePo = new GenerateTablePo();
				for (int j = 0; j < colNum; j++) {
					generateTablePo.put(mapValueKeys.get(indexToEnglish.get(j)), ReadExcel.getCellValue(row.getCell(j)));
				}
				if (generateTablePo.containsKey("id")) {
					generateTablePo.setId(generateTablePo.get("id"));
					generateTablePo.remove("id");
				}
				listGenerateTablePos.add(generateTablePo);
				if(listGenerateTablePos.size()==1000){
					generateTableElasticsearchRepository.saveAll(listGenerateTablePos);
					listGenerateTablePos = new ArrayList<>();
				}
			}
			if(listGenerateTablePos!=null&&listGenerateTablePos.size()>0){
				generateTableElasticsearchRepository.saveAll(listGenerateTablePos);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return map;
	}
	
	
	
}
