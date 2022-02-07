package com.yijiang.io.rpc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @Auther: jiangyi
 * @Date: 2022-02-06
 * @Description: com.yijiang.io.rpc
 */
public class SerDerUtil {

    static ByteArrayOutputStream out = new ByteArrayOutputStream();

    public static synchronized byte[] serialize(Object responseMsg){
        out.reset();
        ObjectOutputStream oos = null;
        byte[] responseBody = null;
        try {
            oos = new ObjectOutputStream(out);
            oos.writeObject(responseMsg);
            responseBody = out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseBody;
    }

}
