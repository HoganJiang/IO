package com.yijiang.io.rpc;

/**
 * @Auther: jiangyi
 * @Date: 2022-02-06
 * @Description: com.yijiang.io.rpc
 */
public class Packmsg {

    private MsgHeader header;
    private MsgBody content;

    public Packmsg(MsgHeader header, MsgBody content) {
        this.header = header;
        this.content = content;
    }

    public MsgHeader getHeader() {
        return header;
    }

    public void setHeader(MsgHeader header) {
        this.header = header;
    }

    public MsgBody getContent() {
        return content;
    }

    public void setContent(MsgBody content) {
        this.content = content;
    }
}
