package com.github.doghere.util;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Created by dog on 12/12/16.
 *
 * @author dogHere@tutamail.com
 */
public class Log {
    private static Logger logger = LogManager.getLogger(Log.class);


    public static void setLogger(Logger logger) {
        Log.logger = logger;
    }


    public static Logger getLogger() {
        return logger;
    }


}