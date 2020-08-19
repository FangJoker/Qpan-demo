package com.chavez.qpan.util.support.date;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateSupport {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.SIMPLIFIED_CHINESE);

    public static String getSimpleDateString(Date date) {
        return sdf.format(date);
    }

}
