package com.yijiang.io.rpc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Auther: jiangyi
 * @Date: 2022-02-04
 * @Description: com.yijiang.io.rpc
 */
public class ResponseHandler {

    static ConcurrentHashMap<Long,CompletableFuture> mapping = new ConcurrentHashMap<>();

    public static void addCallBack(long requestID,CompletableFuture clf){
        mapping.putIfAbsent(requestID,clf);
    }

    public static void runCallBack(Packmsg packmsg){
        CompletableFuture clf = mapping.get(packmsg.getHeader().getRequestID());
//        System.out.println(packmsg.getContent().getResponseString());
        clf.complete(packmsg.getContent().getResponseString());
        removeCB(packmsg.getHeader().getRequestID());
    }

    private static void removeCB(long requestID) {
        mapping.remove(requestID);
    }
}
