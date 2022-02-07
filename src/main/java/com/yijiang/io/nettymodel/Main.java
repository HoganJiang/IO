package com.yijiang.io.nettymodel;

/**
 * @Auther: jiangyi
 * @Date: 2022-01-30
 * @Description: com.yijiang.io.nettymodel
 */
public class Main {
    public static void main(String[] args) {
        //1.申请指定数量处理服务端与客户端逻辑的线程
        SelectorThreadsGroup boss = new SelectorThreadsGroup(3);
        SelectorThreadsGroup worker = new SelectorThreadsGroup(3);
        boss.setWorker(worker);

        //2.绑定端口
        boss.bind(9999);
        boss.bind(8888);
        boss.bind(7777);
    }
}