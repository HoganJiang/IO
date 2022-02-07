package com.yijiang.io.rpc;

import java.util.concurrent.ConcurrentHashMap;

public class Dispatcher {

    public  static ConcurrentHashMap<String,Object> invokeMap = new ConcurrentHashMap<>();

    public void register(String k,Object obj){
        invokeMap.put(k,obj);
    }

    public Object get(String k){
        return invokeMap.get(k);
    }

}
