package com.github.doghere.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;

/**
 * Created by dog on 12/16/16.
 * <p>
 * Db mapper
 *
 * @author dogHere@tutamail.com
 */


public class Type {


    public static HashMap<Integer, Class> dbJava;
    public static HashMap<Class, Integer> javaDb;

    static {
        javaDb = new HashMap<Class, Integer>();


        javaDb.put(String.class, Types.CHAR);
        javaDb.put(String.class, Types.VARCHAR);
        javaDb.put(String.class, Types.LONGVARCHAR);

        javaDb.put(BigDecimal.class, Types.NUMERIC);
        javaDb.put(BigDecimal.class, Types.DECIMAL);

        javaDb.put(Byte.class, Types.BIT);
        javaDb.put(Byte.class, Types.TINYINT);
        javaDb.put(Short.class, Types.SMALLINT);

        javaDb.put(Integer.class, Types.INTEGER);
        javaDb.put(BigInteger.class, Types.BIGINT);
        javaDb.put(Float.class, Types.FLOAT);
        javaDb.put(Double.class, Types.DOUBLE);
        javaDb.put(java.sql.Date.class, Types.DATE);
        javaDb.put(java.sql.Time.class, Types.TIME);

        javaDb.put(Timestamp.class, Types.TIMESTAMP);

        dbJava = new HashMap<Integer, Class>();
        dbJava.put(Types.CHAR, String.class);
        dbJava.put(Types.VARCHAR, String.class);
        dbJava.put(Types.LONGVARCHAR, String.class);
        dbJava.put(Types.TINYINT, Byte.class);
        dbJava.put(Types.SMALLINT, Short.class);

        dbJava.put(Types.TIMESTAMP, Timestamp.class);
        dbJava.put(Types.DATE, java.sql.Date.class);
        dbJava.put(Types.TIME, java.sql.Time.class);
        dbJava.put(Types.FLOAT, Float.class);
        dbJava.put(Types.REAL,Float.class);
        dbJava.put(Types.DOUBLE, Double.class);
        dbJava.put(Types.DECIMAL, BigDecimal.class);
        dbJava.put(Types.NUMERIC, BigDecimal.class);

        dbJava.put(Types.INTEGER, Integer.class);
        dbJava.put(Types.BIGINT, BigInteger.class);

        dbJava.put(Types.BIT, Byte.class);
    }
}
