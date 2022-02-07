package com.yijiang.io.rpc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * @Auther: jiangyi
 * @Date: 2022-02-06
 * @Description: com.yijiang.io.rpc
 */
public class ServerDecode extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        while (buf.readableBytes() >= 102) {
            byte[] bytes = new byte[102];
            buf.getBytes(buf.readerIndex(), bytes); // 从哪里读取，读多少，但是readindex不变
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            ObjectInputStream oin = new ObjectInputStream(in);
            MsgHeader header = (MsgHeader) oin.readObject();

            if ((buf.readableBytes() - 102) >= header.getDataLen()) {
                buf.readBytes(102);
                byte[] data = new byte[(int) header.getDataLen()];
                buf.readBytes(data);
                ByteArrayInputStream din = new ByteArrayInputStream(data);
                ObjectInputStream doin = new ObjectInputStream(din);

                if (header.getProtocol() == 0x14141414) {
                    MsgBody content = (MsgBody) doin.readObject();
                    out.add(new Packmsg(header, content));
                } else if (header.getProtocol() == 0x14141424) {
                    MsgBody content = (MsgBody) doin.readObject();
                    out.add(new Packmsg(header, content));
                }
            } else {
                break;
            }

        }
    }
}
