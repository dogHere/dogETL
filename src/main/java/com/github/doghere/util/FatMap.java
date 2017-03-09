package com.github.doghere.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dog on 1/23/17.
 */
public class FatMap {
    private String old;
    //json key -> table key
    private Map<String,String> mapper= new HashMap<>();
    public  FatMap(String old,Map<String,String> mapper){
        this.old=old;
        this.mapper=mapper;
    }
}
