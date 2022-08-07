package rpc.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import rpc.entity.MsgContent;
import rpc.entity.MyHeader;
import rpc.entity.PackageMsg;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/8/8 4:38 上午
 */
public class ServerDecoder extends ByteToMessageDecoder {
    //父类中有channelRead : {旧的拼接byteBuf -> decode(); -> 剩余留存} -> byteBuf
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        while (byteBuf.readableBytes() >= 92) {
            byte[] bytes = new byte[92];
            byteBuf.getBytes(byteBuf.readerIndex(), bytes);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            MyHeader header = (MyHeader) objectInputStream.readObject();

            //解码器在请求返回双向使用
            if (byteBuf.readableBytes() - 92 >= header.getDataLength()) {
                //移动指针到body开始的位置
                byteBuf.readBytes(92);
                byte[] data = new byte[(int) header.getDataLength()];
                byteBuf.readBytes(data);
                byteArrayInputStream = new ByteArrayInputStream(data);
                objectInputStream = new ObjectInputStream(byteArrayInputStream);


                if (header.getFlag() == 0x14141414) {
                    MsgContent content = (MsgContent) objectInputStream.readObject();
                    list.add(new PackageMsg(header, content));
                } else if (header.getFlag() == 0x14141424) {
                    MsgContent content = (MsgContent) objectInputStream.readObject();
                    list.add(new PackageMsg(header, content));
                }
            } else {
                break;
            }
        }
    }
}
