package rpc.handler;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import rpc.entity.MsgContent;
import rpc.entity.MyHeader;
import rpc.entity.PackageMsg;
import rpc.utils.SerDerUtil;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/8/8 2:25 上午
 */
public class ServerRequestHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        PackageMsg requestPackage = (PackageMsg) msg;

        //返回新的header和content
        String ioThreadName = Thread.currentThread().getName();

        //使用Netty的EventLoop来处理业务及返回
        ctx.executor().parent().next().execute(() -> {
            String execThreadName = Thread.currentThread().getName();
            String res = "IO Thread " + ioThreadName + " execThread: " + execThreadName + " from args: " + requestPackage.getContent().getArgs()[0];

            MsgContent content = MsgContent.builder().res(res).build();
            byte[] contentBytes = SerDerUtil.ser(content);

            MyHeader header = MyHeader.builder()
                    .flag(0X14141424)
                    .requestId(requestPackage.getHeader().getRequestId())
                    .dataLength(contentBytes.length).build();
            byte[] headerBytes = SerDerUtil.ser(header);

            ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(headerBytes.length + contentBytes.length);
            byteBuf.writeBytes(headerBytes);
            byteBuf.writeBytes(contentBytes);
            ctx.writeAndFlush(byteBuf);
        });
    }
}
