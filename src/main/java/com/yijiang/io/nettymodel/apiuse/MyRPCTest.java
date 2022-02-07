package com.yijiang.io.nettymodel.apiuse;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: 马士兵教育
 * @create: 2020-07-12 20:08
 */

/*
    1，先假设一个需求，写一个RPC    疑问1：什么是RPC？
    2，来回通信，连接数量，拆包？   疑问2：老师提到的共享，复用，独享连接是什么意思？拆包是什么意思？来回通信是什么意思？为什么这些要素必不可少？
    3，动态代理呀，序列化，协议封装 疑问3：这三者的含义是什么？为什么要使用这三个技术？动态代理有哪几个版本？
    4，连接池
    5，RPC通俗的解释：就像调用本地方法一样去调用远程的方法，面向java中就是所谓的 面向interface开发 疑问4：这句话是什么意思？ 解释：服务端实现接口的中的方法，客户端只需要调用接口，通过动态代理
        获取代理类，并通过底层调用远程的接口的实现的方法，并将接口返回给客户端。
 */
public class MyRPCTest {


    public static <T> T proxyGet(Class<T> interfaceInfo) {
        //实现各个版本的动态代理。。。。
        ClassLoader loader = interfaceInfo.getClassLoader();
        Class<?>[] methodInfo = {interfaceInfo};


        return (T) Proxy.newProxyInstance(loader, methodInfo, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //如何设计我们的consumer对于provider的调用过程

                //1，调用远程接口的服务，方法，参数  -> 封装成message [content] 疑问7：这里的服务指什么？ 答：服务指的是接口。 老师在课上提到，这一步应该要实现注册发现中心，注册发现中心指的是什么？
                String name = interfaceInfo.getName();
                String methodName = method.getName();
                Class<?>[] parameterTypes = method.getParameterTypes();
                MyContent content = new MyContent();

                content.setArgs(args);
                content.setName(name);
                content.setMethodName(methodName);
                content.setParameterTypes(parameterTypes);

                // new ObjectOutputStream(new ByteArrayOutputStream())： 将msg序列化到内存中，其目的是为了将content封装成msg时，同时能够将content的大小能够封装进msg中
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream(out);
                oout.writeObject(content);
                byte[] msgBody = out.toByteArray();

                //2，requestID+message: 疑问4：设计requestID目的是什么？ 答：requestID用于标识发送到哪儿，
                //本地要缓存: 疑问5：为什么需要本地缓存？答：设计缓存的目的是为了获取保存返回的ID，使得能够回到当前的动态代理的线程中来
                //协议：【header<>】【msgBody】 疑问8：这里的协议指的是什么？答：这里的协议指的消息头与消息体，在dubbo框架中，消息头的实现包含了消息的大小，UUID等信息
                //参考dubbo的实现，这里的header也要封装消息的大小，以及UUID等信息。
                Myheader header = createHeader(msgBody);

                out.reset();
                oout = new ObjectOutputStream(out);
                oout.writeObject(header);
                //解决数据decode问题
                //TODO：Server：： dispatcher  Executor
                byte[] msgHeader = out.toByteArray();
                System.out.println("old:::" + msgHeader.length);
//                System.out.println("msgHeader :"+msgHeader.length);
                //3，连接池：：取得连接
                ClientFactory factory = ClientFactory.getFactory();
                NioSocketChannel clientChannel = factory.getClient(new InetSocketAddress("localhost", 9090));
                //获取连接过程中： 开始-创建，过程-直接取
                //4，发送--> 走IO  out -->走Netty（event 驱动）
                ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(msgHeader.length + msgBody.length);
                CountDownLatch countDownLatch = new CountDownLatch(1);
                long id = header.getRequestID();
                CompletableFuture<String> res = new CompletableFuture<>();
//                ResponseMappingCallback.addCallBack(id, res);
                byteBuf.writeBytes(msgHeader);
                byteBuf.writeBytes(msgBody);
                ChannelFuture channelFuture = clientChannel.writeAndFlush(byteBuf);
                channelFuture.sync();  //io是双向的，你看似有个sync，她仅代表out
                countDownLatch.await();
                //5，？，如果从IO ，未来回来了，怎么将代码执行到这里 疑问6：这里的回来了指什么？ 答：指的是通过远程调用后，如果有返回值应该怎么处理
                //（睡眠/回调，如何让线程停下来？你还能让他继续。。。）
                return res.get();//阻塞的
            }
        });
    }

    public static Myheader createHeader(byte[] msg) {
        Myheader header = new Myheader();
        int size = msg.length;
        int f = 0x14141414; //这里的4个字节可以表示不同的含义：前一个字节可以表示version的版本号，后一个字节的前三个二进制为可以表示请求的不同状态，后五个二进制位表示协议的ID....
        long requestID = Math.abs(UUID.randomUUID().getLeastSignificantBits());
        //0x14  0001 0100
        header.setFlag(f);
        header.setDataLen(size);
        header.setRequestID(requestID);
        return header;
    }
}

class ClientFactory {

    int poolSize = 1;
    NioEventLoopGroup clientWorker;
    Random rand = new Random();

    private ClientFactory() {
    }

    private static final ClientFactory factory;

    static {
        factory = new ClientFactory();
    }

    public static ClientFactory getFactory() {
        return factory;
    }


    //一个consumer 可以连接很多的provider，每一个provider都有自己的pool  K,V
    ConcurrentHashMap<InetSocketAddress, ClientPool> outboxs = new ConcurrentHashMap<>();

    public synchronized NioSocketChannel getClient(InetSocketAddress address) {

        ClientPool clientPool = outboxs.get(address);
        if (clientPool == null) {
            outboxs.putIfAbsent(address, new ClientPool(poolSize));
            clientPool = outboxs.get(address);
        }

        int i = rand.nextInt(poolSize);

        if (clientPool.clients[i] != null && clientPool.clients[i].isActive()) {
            return clientPool.clients[i];
        }

        synchronized (clientPool.lock[i]) {
            return clientPool.clients[i] = create(address);
        }

    }

    private NioSocketChannel create(InetSocketAddress address) {

        //基于 netty 的客户端创建方式
        clientWorker = new NioEventLoopGroup(1);
        Bootstrap bs = new Bootstrap();
        ChannelFuture connect = bs.group(clientWorker)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
//                        p.addLast(new ServerDecode());
//                        p.addLast(new ClientResponses());  //解决给谁的？？  requestID..
                    }
                }).connect(address);
        try {
            NioSocketChannel client = (NioSocketChannel) connect.sync().channel();
            return client;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}

//class ClientResponses extends ChannelInboundHandlerAdapter {
//    //consumer.....
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        Packmsg responsepkg = (Packmsg) msg;
//        //曾经我们没考虑返回的事情
//        ResponseMappingCallback.runCallBack(responsepkg);
//    }
//}


class ClientPool {
    //一个连接存放多个连接
    NioSocketChannel[] clients;
    //伴生锁
    Object[] lock;

    ClientPool(int size) {
        clients = new NioSocketChannel[size];//init  连接都是空的
        lock = new Object[size]; //锁是可以初始化的
        for (int i = 0; i < size; i++) {
            lock[i] = new Object();
        }
    }
}

class Myheader implements Serializable {
    //通信上的协议
    /*
    1，ooxx值 疑问10：OOXX值指的是什么？ 答：表示用什么要的协议，用一个二进制位来表示，某些二进制为表示不同的状态，是request？还是response?
    2，UUID:requestID
    3，DATA_LEN

     */
    int flag;  //32bit可以设置很多信息。。。
    long requestID;
    long dataLen;


    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
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


class MyContent implements Serializable {

    String name;
    String methodName;
    Class<?>[] parameterTypes;
    Object[] args;
    String res;

    public String getRes() {
        return res;
    }

    public void setRes(String res) {
        this.res = res;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}

interface Car {
    public String ooxx(String msg);
}

interface Fly {
    void xxoo(String msg);
}



