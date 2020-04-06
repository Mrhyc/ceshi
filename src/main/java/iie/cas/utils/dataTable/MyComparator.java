package iie.cas.utils.dataTable;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.Date;

import net.sourceforge.pinyin4j.PinyinHelper;

/**
 * TODO
 * 
 * @author wuyonghang
 * @since 2015年6月1日 下午6:20:59
 * @version 1.0
 */
public class MyComparator implements Comparator {

    Order order;
    Field field;
    String fieldType;
    boolean dir = true;// true为升序

    /**
     * 
     */
    public MyComparator(Order order, Class<?> type) {
        this.order = order;
        try {
            field = type.getDeclaredField(order.column);
            field.setAccessible(true);// 修改访问权限
            fieldType = field.getType().toString();
            if (order.dir.equals("desc"))
                dir = false;
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param <fieldType>
     * @param o1
     * @param o2
     * @return
     */
    @Override
    public int compare(Object o1, Object o2) {
        if (field == null)
            return 0;
        Object fieldValue1, fieldValue2;
        int result = 0;
        try {
            fieldValue1 = field.get(o1)==null?"":field.get(o1);
            fieldValue2 = field.get(o2)==null?"":field.get(o2);

            switch (fieldType) {
            case "class java.lang.String":
                result = (ToPinYinString((String) fieldValue1)).compareTo(ToPinYinString((String) fieldValue2));
                break;
            case "class java.lang.Integer":
            case "class int":
            case "int":
                result = ((Integer) fieldValue1).compareTo((Integer) fieldValue2);
                break;
            case "class java.lang.Long":
            case "class long":
            case "long":
                result = ((Long) fieldValue1).compareTo((Long) fieldValue2);
                break;
            case "class java.lang.Float":
            case "class float":
            case "float":
                result = ((Float) fieldValue1).compareTo((Float) fieldValue2);
                break;
            case "class java.lang.Double":
            case "class double":
            case "double":
                result = ((Double) fieldValue1).compareTo((Double) fieldValue2);
                break;
            case "class java.util.Date":
            case "class java.sql.Date":
                result = ((Date) fieldValue1).compareTo((Date) fieldValue2);
                break;
            case "class java.sql.Timestamp":
                result = ((Timestamp) fieldValue1).compareTo((Timestamp) fieldValue2);
                break;
            default:
                break;
            }
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
        }
        if (!dir)// false 为降序
            return -result;
        return result;
    }

    private String ToPinYinString(String str) {

        StringBuilder sb = new StringBuilder();
        String[] arr = null;

        for (int i = 0; i < str.length(); i++) {
            arr = PinyinHelper.toHanyuPinyinStringArray(str.charAt(i));
            if (arr != null && arr.length > 0)
                for (String string : arr)
                    sb.append(string);
            else
                sb.append(str.charAt(i));
        }

        return sb.toString();
    }
}
