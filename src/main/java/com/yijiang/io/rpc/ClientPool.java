package com.yijiang.io.rpc;

import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @Auther: jiangyi
 * @Date: 2022-02-04
 * @Description: com.yijiang.io.rpc
 */
public class ClientPool {

    private NioSocketChannel[] clients;
    private Object[] lock;

    ClientPool(int size) {
        clients = new NioSocketChannel[size];// init 连接都是空的
        lock = new Object[size]; // 锁是可以初始化的
        for (int i = 0; i < size; i++) {
            lock[i] = new Object();
        }
    }

    public NioSocketChannel[] getClients() {
        return clients;
    }

    public void setClients(NioSocketChannel[] clients) {
        this.clients = clients;
    }

    public Object[] getLock() {
        return lock;
    }

    public void setLock(Object[] lock) {
        this.lock = lock;
    }

}
