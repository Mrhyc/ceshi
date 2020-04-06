package iie.cas.utils;

import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XssfStyleUtil {
	public static XSSFCellStyle xssfCellStyleHead(XSSFWorkbook xssfWorkbook){
		XSSFCellStyle style1 = xssfWorkbook.createCellStyle();
		style1.setAlignment(XSSFCellStyle.ALIGN_CENTER); // 文字水平居中
		style1.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);// 文字垂直居中
//		style1.setWrapText(true);//自动换行
		style1.setBorderBottom(XSSFCellStyle.BORDER_THIN); // 底边框加黑
		style1.setBorderLeft(XSSFCellStyle.BORDER_THIN); // 左边框加黑
		style1.setBorderRight(XSSFCellStyle.BORDER_THIN); // 右边框加黑
		style1.setBorderTop(XSSFCellStyle.BORDER_THIN); // 上边框加黑
		XSSFFont font1 = xssfWorkbook.createFont();
		font1.setFontName("黑体");
		font1.setFontHeightInPoints((short) 10);
		font1.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);// 粗体显示
		style1.setFont(font1);
		return style1;
	}
	
	
	public static XSSFCellStyle xssfCellStyleInfo(XSSFWorkbook xssfWorkbook){
		XSSFCellStyle style2 = xssfWorkbook.createCellStyle();
		style2.setAlignment(XSSFCellStyle.ALIGN_CENTER); // 文字水平居中
		style2.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);// 文字垂直居中
//		style2.setWrapText(true);
		style2.setBorderBottom(XSSFCellStyle.BORDER_THIN); // 底边框加黑
		style2.setBorderLeft(XSSFCellStyle.BORDER_THIN); // 左边框加黑
		style2.setBorderRight(XSSFCellStyle.BORDER_THIN); // 右边框加黑
		style2.setBorderTop(XSSFCellStyle.BORDER_THIN); // 上边框加黑
		XSSFFont font2 = xssfWorkbook.createFont();
		font2.setFontName("黑体");
		font2.setFontHeightInPoints((short) 10);
		font2.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);// 粗体显示
		style2.setFont(font2);
		return style2;
	}
}
