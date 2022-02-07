package com.yijiang.io.nettymodel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Auther: jiangyi
 * @Date: 2022-01-30
 * @Description: com.yijiang.io.nettymodel
 */
public class SelectorThreadsGroup {

    private ExecutorService service;
    private SelectorThreads[] selectorThreads;
    private ServerSocketChannel serverSocketChannel;
    private AtomicInteger xid = new AtomicInteger(0);
    private SelectorThreadsGroup workerGroup = this;

    public SelectorThreadsGroup(Integer selectorThreadsCounts) {
        service = Executors.newFixedThreadPool(selectorThreadsCounts);
        selectorThreads = new SelectorThreads[selectorThreadsCounts];
        for (int i = 0; i < selectorThreadsCounts; i++) {
            selectorThreads[i] = new SelectorThreads(this);
            service.execute(selectorThreads[i]);
        }
    }

    public void bind(Integer port) {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            //注册到某个selector
//            nextSelector(serverSocketChannel);
            nextSelectorV3(serverSocketChannel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void nextSelectorV3(Channel channel) {
        try {
            if (channel instanceof ServerSocketChannel) {
                SelectorThreads st = next();
                st.getLbq().add(channel);
                st.setWorker(workerGroup);
                st.getSelector().wakeup();
            } else {
                SelectorThreads st = nextV3();  //在 main线程种，取到堆里的selectorThread对象
                //1,通过队列传递数据 消息
                st.getLbq().add(channel);
                //2,通过打断阻塞，让对应的线程去自己在打断后完成注册selector
                st.getSelector().wakeup();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void nextSelectorV2(Channel channel) {
        try {
            if (channel instanceof ServerSocketChannel) {

                selectorThreads[0].getLbq().put(channel);
                selectorThreads[0].getSelector().wakeup();
            } else {
                SelectorThreads st = nextV3();  //在 main线程种，取到堆里的selectorThread对象
                //1,通过队列传递数据 消息
                st.getLbq().add(channel);
                //2,通过打断阻塞，让对应的线程去自己在打断后完成注册selector
                st.getSelector().wakeup();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


//    public void nextSelector(Channel channel) {
//        SelectorThreads selectorThread = next();
//        LinkedBlockingQueue<Channel> lbq = selectorThread.getLbq();
//        lbq.add(channel);
//        selectorThread.getSelector().wakeup();

//        ServerSocketChannel serverSocketChannel = (ServerSocketChannel)channel;
//        try {
//            serverSocketChannel.register(selectorThread.getSelector(),SelectionKey.OP_ACCEPT);
//            selectorThread.getSelector().wakeup();
//        } catch (ClosedChannelException e) {
//            e.printStackTrace();
//        }
//    }


    private SelectorThreads nextV2() {
        //轮询获取selector
        int index = xid.incrementAndGet() % (selectorThreads.length - 1);
        return selectorThreads[index + 1];
    }

    private SelectorThreads next() {
        //轮询获取selector
        int index = xid.incrementAndGet() % selectorThreads.length;
        return selectorThreads[index];
    }

    private SelectorThreads nextV3() {
        int index = xid.incrementAndGet() % workerGroup.selectorThreads.length;
        return workerGroup.selectorThreads[index];
    }

    public void setWorker(SelectorThreadsGroup worker) {
        this.workerGroup = worker;
    }
}
