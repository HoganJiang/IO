package com.yijiang.io.rpc;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

/**
 * @Auther: jiangyi
 * @Date: 2022-02-04
 * @Description: com.yijiang.io.rpc
 */
public class MyRPC {

    public static <T>T getByProxy(Class<T> interfaceInfor) {

        ClassLoader inforClassLoader = interfaceInfor.getClassLoader();
        Class<?>[] interfaceInforClass = {interfaceInfor};

        return (T)Proxy.newProxyInstance(inforClassLoader, interfaceInforClass, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //如何设计consumer对于provider的调用过程？
                /*
                站在consumer的角度，要想获得provider提供的服务，需要向provider提供下面的信息：
                1. 想要获得的服务的名称，方法，参数
                2. 能够标识自己身份的ID，以便provider能够准确的将服务发给准确的consumer
                3. 指定与那个provider建立连接
                4. 发送自己信息
                5. 获得provider提供的服务
                 */
                //1.提供调用远程接口的服务，方法，参数  -> 封装成RequestBody
                String interfaceInforName = interfaceInfor.getName();
                Class<?>[] parameterTypes = method.getParameterTypes();
                String methodName = method.getName();
                MsgBody requestBody = new MsgBody();
                requestBody.setArgs(args);
                requestBody.setMethodName(methodName);
                requestBody.setInterfaceName(interfaceInforName);
                requestBody.setParameterTypes(parameterTypes);
                //1.1 将requestBody序列化的内存中:将msg序列化到内存中，其目的是为了将content封装成msg时，同时能够将content的大小能够封装进msg中
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();//???
                ObjectOutputStream bodyOutPut = new ObjectOutputStream(byteArrayOutputStream);//???
                //TODO:解决数据decode问题
                //TODO:Server::dispather Executor
                bodyOutPut.writeObject(requestBody);//????
                byte[] bodyBytes = byteArrayOutputStream.toByteArray();//???
//                System.out.println("requestBody size: " + bodyBytes.length);

                //2.设计请求头，请求头中的信息要包括请求的ID，选择的协议，消息体的大小
                MsgHeader requestHeader = createHeader(bodyBytes);
                byteArrayOutputStream.reset();
                bodyOutPut = new ObjectOutputStream(byteArrayOutputStream);
                bodyOutPut.writeObject(requestHeader);
                byte[] headerBytes = byteArrayOutputStream.toByteArray();
//                System.out.println("Request Header size: " + headerBytes.length);

                //3.连接池:取得连接
                ClientFactory clientFactory = ClientFactory.getFactory();
                NioSocketChannel clientChannel = clientFactory.getClient(new InetSocketAddress("localhost", 9090));

                //4.发送RequestHeader+RequestBody
                ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(headerBytes.length + bodyBytes.length);
                long requestID = requestHeader.getRequestID();
                CompletableFuture<String> res = new CompletableFuture<>();
                ResponseHandler.addCallBack(requestID,new CompletableFuture());
                byteBuf.writeBytes(headerBytes);
                byteBuf.writeBytes(bodyBytes);
                ChannelFuture channelFuture = clientChannel.writeAndFlush(byteBuf);
                channelFuture.sync();
//                System.out.println(res.get());
                return res.get();
            }
        });
    }

    public static MsgHeader createHeader(byte[] msg) {
        MsgHeader header = new MsgHeader();
        int size = msg.length;
        // 这里的4个字节可以表示不同的含义：前一个字节可以表示version的版本号，后一个字节的前三个二进制为可以表示请求的不同状态，后五个二进制位表示协议的ID....
        // 0x14 0001 0100
        int f = 0x14141414;
        long requestID = Math.abs(UUID.randomUUID().getLeastSignificantBits());
        header.setProtocol(f);
        header.setDataLen(size);
        header.setRequestID(requestID);
        return header;
    }

}
