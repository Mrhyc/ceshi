package iie.cas.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
       
       /**
        * 导出统计分析excel文件接口
        * 本文件使用了jdk8的新特性，故最小运行版本为jdk1.8
        * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
        * @version 1.0
        * @sine 2017/4/18
        */
       public interface ExportExcel<T> {
           /**
            * 将字节数组写出到servlet输出流
            * @param response  http回应对象，为excel回应的目的地
            * @param list 要导出到 excel的数据集合
            *  @param titles excel的标题 通常取第一行作为excel的标题
            * */
           default void exportExcel(HttpServletResponse response,List<T> list,String[] titles,String fileName) throws IOException  {
               byte[] bytes = selectExcel(list,titles);
//               response.setContentType("application/x-msdownload");
               response.setContentType("application/octet-stream");
               response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
//               response.setHeader("Content-Disposition", "attachment;filename=" + new String(
//						fileName.getBytes("utf-8"), "iso8859-1"));
               response.setContentLength(bytes.length);
               response.getOutputStream().write(bytes);
               response.getOutputStream().flush();
               response.getOutputStream().close();
           }
           /**
            * 选择要导出的文件 导出的excel 属于office 2007格式的文件
            * @param list excel文件内容
            *  @param titles excel 文件的标题
            * @return 已经生成excel文件的字节数组
            * */
           default byte[] selectExcel(List<T> list, String[] titles) throws IOException {
                   Workbook workbook = new XSSFWorkbook();
                   Sheet sheet = workbook.createSheet();
                   XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
	       			style.setAlignment(XSSFCellStyle.ALIGN_CENTER); // 文字水平居中
	       			style.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);// 文字垂直居中
	       			style.setWrapText(true);
	       			style.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 底边框加黑
	       			style.setBorderLeft(HSSFCellStyle.BORDER_THIN); // 左边框加黑
	       			style.setBorderRight(HSSFCellStyle.BORDER_THIN); // 右边框加黑
	       			style.setBorderTop(HSSFCellStyle.BORDER_THIN); // 上边框加黑
	       			XSSFFont font = (XSSFFont) workbook.createFont();
	       			font.setFontName("黑体");
	       			font.setFontHeightInPoints((short) 10);
	       			style.setFont(font);
                   generateExcelTitle(titles,sheet,style);
                   eachListAndCreateRow(list,sheet,style);
                   ByteArrayOutputStream out = new ByteArrayOutputStream();
                   workbook.write(out);
               return out.toByteArray();
           }
       
		/**
            * 遍历集合，并创建单元格行
            * @param list 数据集合
            *  @param sheet 工作簿
            * */
          default void eachListAndCreateRow(List<T> list, Sheet sheet,XSSFCellStyle style){
              for (int i = 0,j = 1; i < list.size(); i ++,j++){
                  T t = list.get(i);
                  Row row = sheet.createRow(j);;
                  generateExcelForAs(t,row);
              }
          }
       
           /**
            * 生成excel文件的标题
            * */
          default void generateExcelTitle(String[] titles, Sheet sheet,XSSFCellStyle style){
              Row row = sheet.createRow(0);
              for (int i = 0; i < titles.length; i++){
            	  row.createCell(i,1).setCellStyle(style);
//                  row.createCell(i,1).setCellValue(titles[i]);
                  row.getCell(i).setCellValue(titles[i]);
              }
          }
       
           /**
            * 创建excel内容文件
            * @param t 组装excel 文件的内容
            * @param row 当前excel 工作行
            * */
           void generateExcelForAs(T t, Row row);
       
           /**
            * 当发生错误时如此回应信息
            * */
           default void errorResponse(HttpServletResponse response){
               byte[] message = "导出excel文件错误,请重试!".getBytes();
               response.setContentType("text/json;charset=UTF-8");
               response.setContentLength(message.length);
               try {
                   response.getOutputStream().write(message);
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
       }