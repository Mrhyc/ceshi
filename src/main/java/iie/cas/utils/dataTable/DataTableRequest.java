package iie.cas.utils.dataTable;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.LoggerFactory;

//import cas.iie.util.ExceptionUtil;

/**
 * TODO
 * 
 * @author wuyonghang
 * @since 2015年4月24日 下午2:33:58
 * @version 1.0
 */
public class DataTableRequest {
    private org.slf4j.Logger logger_ = LoggerFactory.getLogger(DataTableRequest.class.getName());

    public Integer draw = 0;
    public List<Column> columns = new ArrayList<Column>();
    public List<Order> orders = new ArrayList<Order>();
    public Integer start = 0;
    public Integer length = 0;
    public String searchValue = null;
    public boolean searchRegex = false;
    public Integer size = 0;

    public DataTableRequest(HttpServletRequest request) {

        Enumeration pNames = request.getParameterNames();
        while (pNames.hasMoreElements()) {
            Object object = pNames.nextElement();
            if (object instanceof String) {
                String pName = (String) object;
                dealWithOneParameter(request, pName);
            }

        }
        for (int i = 0; i < orders.size(); i++) {
            orders.get(i).column = columns.get(Integer.valueOf(orders.get(i).column).intValue()).data;
        }

        // 有病吗 就是为了避免空指针异常才new的
        /*if (columns.size() == 0) {
            columns = null;
        }
        if (orders.size() == 0) {
            orders = null;
        }*/
    }

   
   

    @SuppressWarnings("unchecked")
    public <T> List<T> Filter(List<T> datas) {

        if (datas.size() == 0)
            return datas;
        Class<?> clazz = datas.get(0).getClass();
        List<Field> fieldList = new ArrayList<Field>();
        List<T> result = new ArrayList<T>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (searchValue != null && !searchValue.equals("")) {
            try {
                for (Column column : columns)
                    if (column.searchable) {
                        try {
                            Field field = clazz.getDeclaredField(column.data);
                            fieldList.add(field);
                            field.setAccessible(true);// 修改访问权限
                        } catch (Exception e) {
                        }
                    }
                // 数据
                for (T data : datas) {
                    Object fieldValue;
                    boolean stop=false;//一个属性查找到, 其他属性不在处理
                    for (Field field : fieldList) {
                        if(stop) break;
                        fieldValue = field.get(data);

                        if (fieldValue == null)
                            continue;
                        switch (field.getType().toString()) {
                        case "class java.lang.String":
                            String value = (String) fieldValue;
                            if (value.toLowerCase().indexOf(searchValue.toLowerCase()) >= 0){
                                result.add(data);
                                stop=true;
                            }
                            break;
                        case "class java.util.Date":
                            String dataTime = sdf.format((Date) fieldValue);
                            if (dataTime != null && dataTime.toLowerCase().indexOf(searchValue.toLowerCase()) >= 0){
                                result.add(data);
                                stop=true;
                            }
                            break;
                        default:
                            if (searchValue.equals(fieldValue.toString())){
                                result.add(data);
                                stop=true;
                            }
                            break;
                        }
                    }
                }
            } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
            }
        }
        else
            result = datas;

        if (result.size() == 0)
            return result;

        for (int i = orders.size() - 1; i >= 0; i--) {
            Collections.sort(result, new MyComparator(orders.get(i), result.get(0).getClass()));
        }

        size = result.size();

        if (start == null || start < 0)
            start = 0;
        if (length == null || length < 0)
            length = 20;

        if (start >= result.size())
            return new ArrayList<T>();
        if (start + length > result.size() || length==0)
            length = result.size() - start;
        return result.subList(start, start + length);
    }

    /**
     * @param request
     * @param pName
     */
    final private void dealWithOneParameter(HttpServletRequest request, String pName) {

        String parameter = request.getParameter(pName);

        if (pName.startsWith("columns")) {
            dealWithColumns(parameter, pName);
        } else if (pName.startsWith("order"))
            dealWithOrder(parameter, pName);
        else if (pName.startsWith("search")) {
            dealWithSearch(parameter, pName);
        } else if (pName.equals("draw"))
            draw = parseInt(parameter);
        else if (pName.equals("start"))
            start = parseInt(parameter);
        else if (pName.equals("length"))
            length = parseInt(parameter);

    }

    /**
     * @param parameter
     * @param pName
     * @param columnsIndexList
     */
    final private void dealWithColumns(String parameter, String pName) {
        if (parameter == null)
            return;
        try {
            Integer pos = Integer.parseInt(pName.substring(8, pName.indexOf(']')));
            Column oneColumn = null;

            // 如果没有,先让list里面有元素
            if (columns.size() <= pos)
                for (int i = columns.size(); i <= pos; i++)
                    columns.add(new Column());

            oneColumn = columns.get(pos);

            if (pName.endsWith("data]"))
                oneColumn.data = parameter;
            else if (pName.endsWith("name]"))
                oneColumn.name = parameter;
            else if (pName.endsWith("searchable]")) {
                oneColumn.searchable = Boolean.parseBoolean(parameter);
            } else if (pName.endsWith("orderable]"))
                oneColumn.orderable = Boolean.parseBoolean(parameter);
            else if (pName.endsWith("search][value]"))
                oneColumn.search_value = parameter;
            else if (pName.endsWith("search][regex]"))
                oneColumn.search_regex = Boolean.parseBoolean(parameter);

        } catch (Exception e) {
            logger_.error("参数下标不是int pName= " + pName);
//            logger_.error(ExceptionUtil.getStackTrace(e));
        }

    }

    /**
     * @param parameter
     * @param pName
     */
    final private void dealWithSearch(String parameter, String pName) {
        if (parameter == null)
            return;
        if (pName.endsWith("value]")) {
            searchValue = parameter;
        } else if (pName.endsWith("regex]"))
            try {
                searchRegex = Boolean.parseBoolean(parameter);
            } catch (Exception e) {
                logger_.error("参数不是boolean, pName=" + pName);
//                logger_.error(ExceptionUtil.getStackTrace(e));
            }

    }

    /**
     * @param parameter
     * @param pName
     * @param ordersIndexList
     * 
     */
    final private void dealWithOrder(String parameter, String pName) {
        if (parameter == null)
            return;

        try {
            Integer pos = Integer.parseInt(pName.substring(6, pName.indexOf(']')));
            Order order = null;

            // 如果没有,先让list里面有元素
            if (orders.size() <= pos)
                for (int i = orders.size(); i <= pos; i++)
                    orders.add(new Order());

            order = orders.get(pos);

            // 获取列和方向
            if (pName.endsWith("column]"))
                order.column = parameter;
            else if (pName.endsWith("dir]"))
                order.dir = parameter;

        } catch (Exception e) {
            logger_.error("参数下标不是int pName= " + pName);
//            logger_.error(ExceptionUtil.getStackTrace(e));
        }
    }

    /**
     * @param parameter
     * @return
     */
    final private Integer parseInt(String parameter) {
        if (parameter == null)
            return null;
        try {
            return Integer.parseInt(parameter);
        } catch (Exception e) {
            return null;
        }

    }

}
