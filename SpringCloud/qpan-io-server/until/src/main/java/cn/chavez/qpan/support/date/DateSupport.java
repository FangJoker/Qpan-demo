package cn.chavez.qpan.support.date;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/3/30 19:18
 */
public class DateSupport {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.SIMPLIFIED_CHINESE);

    public static String getSimpleDateString(Date date) {
        return sdf.format(date);
    }
}
