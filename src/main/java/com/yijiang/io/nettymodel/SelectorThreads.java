package com.yijiang.io.nettymodel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Auther: jiangyi
 * @Date: 2022-01-30
 * @Description: com.yijiang.io.nettymodel
 */
public class SelectorThreads implements Runnable {

    private Selector selector;
    private SelectorThreadsGroup stg;
    private LinkedBlockingQueue<Channel> lbq = new LinkedBlockingQueue<>();

    public SelectorThreads(SelectorThreadsGroup stg) {
        this.stg = stg;
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (selector.select() > 0) {
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> selectionKeyIterator = selectionKeys.iterator();
                    while (selectionKeyIterator.hasNext()) {
                        SelectionKey selectionKey = selectionKeyIterator.next();
                        selectionKeyIterator.remove();
                        if (selectionKey.isAcceptable()) {
                            acceptHandler(selectionKey);
                        } else if (selectionKey.isReadable()) {
                            readEventHandler(selectionKey);
                        } else if (selectionKey.isWritable()) {
                            writeEventHandler(selectionKey);
                        }
                    }
                }
                //注册
                if(!lbq.isEmpty()){
                    Channel channel = lbq.take();
                    if(channel instanceof ServerSocketChannel){
                        ServerSocketChannel server = (ServerSocketChannel)channel;
                        server.register(selector, SelectionKey.OP_ACCEPT);
                        System.out.println(Thread.currentThread().getName()+" register listen");
                    } else if (channel instanceof SocketChannel){
                        SocketChannel client = (SocketChannel)channel;
                        ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
                        client.register(selector,SelectionKey.OP_READ,buffer);
                        System.out.println(Thread.currentThread().getName()+" register client: " + client.getRemoteAddress());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void acceptHandler(SelectionKey selectionKey) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        try {
            SocketChannel client = serverSocketChannel.accept();
            client.configureBlocking(false);
            stg.nextSelectorV3(client);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readEventHandler(SelectionKey selectionKey) {
        System.out.println(Thread.currentThread().getName() + " is reading....");
        ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        buffer.clear();
        while (true) {
            try {
                int read = socketChannel.read(buffer);
                if (read > 0) {
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        socketChannel.write(buffer);
                    }
                    buffer.clear();
                } else if (read == 0) {
                    break;
                } else {
                    System.out.println("Client: " + socketChannel.getRemoteAddress() + " closed.");
                    selectionKey.cancel();
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeEventHandler(SelectionKey selectionKey) {

    }

    public Selector getSelector() {
        return selector;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public LinkedBlockingQueue<Channel> getLbq() {
        return lbq;
    }

    public void setWorker(SelectorThreadsGroup workerGroup) {
        this.stg = workerGroup;
    }
}
