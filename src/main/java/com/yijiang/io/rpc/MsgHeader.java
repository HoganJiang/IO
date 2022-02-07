package com.yijiang.io.rpc;

import java.io.Serializable;

/**
 * @Auther: jiangyi
 * @Date: 2022-02-04
 * @Description: com.yijiang.io.rpc
 */
public class MsgHeader implements Serializable {
    private static final long serialVersionUID = -3413527196578585683L;
    // 通信上的协议
    /*
     * 1，ooxx值 疑问10：OOXX值指的是什么？
     * 答：表示用什么要的协议，用一个二进制位来表示，某些二进制为表示不同的状态，是request？还是response?
     * 2，UUID:requestID
     * 3，DATA_LEN
     *
     */
    int protocol; // 32bit可以设置很多信息。。。
    long requestID;
    long dataLen;

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public long getRequestID() {
        return requestID;
    }

    public void setRequestID(long requestID) {
        this.requestID = requestID;
    }

    public long getDataLen() {
        return dataLen;
    }

    public void setDataLen(long dataLen) {
        this.dataLen = dataLen;
    }
}
