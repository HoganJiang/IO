package com.yijiang.io.rpc;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Time;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: jiangyi
 * @Date: 2022-02-04
 * @Description: com.yijiang.io.rpc
 */
public class ServerRequestHandler extends ChannelInboundHandlerAdapter {

    private Dispatcher dis;

    public ServerRequestHandler(Dispatcher dispatcher) {
        this.dis = dispatcher;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Packmsg responsePkg = (Packmsg) msg;
//        System.out.println("Server handler:" + responsePkg.getContent().getArgs()[0]);
        String ioThreadName = Thread.currentThread().getName();
        //处理客户端返回：返回的信息应该包括新的消息头：requestID, 消息体
//        ctx.executor().execute(
        ctx.executor().parent().next().execute(
        new Runnable() {
            @Override
            public void run() {
                String serviceName = responsePkg.getContent().getInterfaceName();
                String method = responsePkg.getContent().getMethodName();
                Object c = dis.get(serviceName);
                Class<?> clazz = c.getClass();
                Object res = null;
                try {
                    Method m = clazz.getMethod(method, responsePkg.getContent().getParameterTypes());
                    res = m.invoke(c, responsePkg.getContent().getArgs());
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
//                System.out.println(responsePkg.getContent().getResponseString());
//                String execThreadName = Thread.currentThread().getName();
//                String responseString = "io thread: " + ioThreadName + " exec thread: " + execThreadName + " from args: " + responsePkg.getContent().getArgs()[0];
                MsgBody responseBody = new MsgBody();
                responseBody.setResponseString((String) res);
                byte[] responseBodyBytes = SerDerUtil.serialize(responseBody);
//                System.out.println("responseBodyBytes.length:" + responseBodyBytes.length);

                MsgHeader responseHead = new MsgHeader();
                responseHead.setRequestID(responsePkg.getHeader().requestID);
                responseHead.setProtocol(0x14141424);
                responseHead.setDataLen(responseBodyBytes.length);
                byte[] responseHeadBytes = SerDerUtil.serialize(responseHead);
//                System.out.println("responseHeadBytes.length:" + responseHeadBytes.length);

                ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(responseHeadBytes.length + responseBodyBytes.length);
                byteBuf.writeBytes(responseHeadBytes);
                byteBuf.writeBytes(responseBodyBytes);
                ctx.writeAndFlush(byteBuf);
            }
        });

//        Thread.sleep(10000);
    }
}
