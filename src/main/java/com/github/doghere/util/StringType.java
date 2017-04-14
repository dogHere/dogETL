package com.github.doghere.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by dog on 4/13/17.
 */
final public class StringType {
    private static String format = "yyyy-MM-dd hh:mm:ss.SSS";
    public static Set<String> set = new HashSet<>();

    static {
        set.add("NULL");
        set.add("null");
        set.add("\\N");
    }
    public static Timestamp string2Timestamp(String str)  {
        if (isNull(str)) return null;
        else return string2Timestamp(str, format);
    }

    public static Timestamp string2Timestamp(String str, String format)  {
        if (isNull(str)) return null;
        if(str.equals("")) return null;
        else {
            java.util.Date date = string2JavaDate(str,format);
            if(date==null) return null;
            else return new Timestamp(date.getTime());
        }
    }

    public static Date string2Date(String str)  {
        if (isNull(str)) return null;
        else
            return string2Date(str, format);
    }

    public static Date string2Date(String str, String format)  {
        if (isNull(str)) return null;
        else {

            java.util.Date date = string2JavaDate(str,format);
            if(date==null) return null;
            return new Date(date.getTime());
        }
    }

    private static java.util.Date string2JavaDate(String str, String format)  {
        if (isNull(str)) return null;
        if(str.equals("")) return null;
        else {
            DateFormat formatter = new SimpleDateFormat(format);
            java.util.Date date = null;
            try {
                date =  formatter.parse(str);
            } catch (ParseException e) {
                e.printStackTrace();

            }
            return date;
        }
    }

    public static Time string2Time(String str) throws ParseException {
        if (isNull(str)) return null;
        else
            return string2Time(str, format);
    }

    public static Time string2Time(String str, String format)  {
        if (isNull(str)) return null;
        else {

            java.util.Date date = string2JavaDate(str,format);
            if(date==null) return null;
            return new Time(date.getTime());
        }
    }

    public static BigDecimal string2BigDecimal(String str) {
        if (isNull(str)) return null;
        else
            return new BigDecimal(str);
    }

    public static BigInteger string2BigInteger(String str) {
        if (isNull(str)) return null;
        else
            return new BigInteger(str);
    }

    public static Byte string2Byte(String str) {
        if (isNull(str)) return null;
        else
            return Byte.parseByte(str);
    }

    public static Short string2Short(String str) {
        if (isNull(str)) return null;
        else
            return Short.parseShort(str);
    }

    public static Integer string2Integer(String str) {
        if (isNull(str)) return null;
        else
            return Integer.parseInt(str);
    }

    public static Long string2Long(String str) {
        if (isNull(str)) return null;
        else
            return Long.parseLong(str);

    }

    public static Double string2Double(String str) {
        if (isNull(str)) return null;
        else
            return Double.parseDouble(str);
    }


    public static String string2String(String str){

        if (isNull(str)) return null;
        else {
            return str;
        }
    }
    private static boolean isNull(Object o) {
        if (o == null) return true;
        String os = o.toString();

        for (String s: set) {
            if(os.equals(s))
                return true;
        }
        return false;
    }


    private static boolean isBlack(String str){
        if(str.equals("")) return true;
        else {
            return false;
        }
    }
}
