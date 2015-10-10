package com.brotherjing.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Brotherjing on 2015/10/10.
 */
public class DateUtil {

    static final DateFormat format_datetime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.CHINA);

    public static String getDateTime(Date date){
        return format_datetime.format(date);
    }

}
