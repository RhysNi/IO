package netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import static java.lang.System.*;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/8/6 4:53 下午
 */
@ChannelHandler.Sharable
public class InHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        out.println("Client Registered~~~");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        out.println("Client Active~~~");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        //指针推移,会造成刷写数据的时候读不到新数据 readCharSequence(可以读多少,字符集)
        // CharSequence data = buf.readCharSequence(buf.readableBytes(), CharsetUtil.UTF_8);

        //会覆盖旧数据达到重用ByteBuf  getCharSequence(从哪里开始读,可以读多少,字符集)
        CharSequence data = buf.getCharSequence(0, buf.readableBytes(), CharsetUtil.UTF_8);
        out.println(data);

        //刷写数据
        ctx.writeAndFlush(data);
    }
}
