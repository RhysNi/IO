package netty;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;

import static java.lang.System.out;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/8/7 5:31 下午
 */
public class AcceptHandler extends ChannelInboundHandlerAdapter {
    private final EventLoopGroup selector;
    private final ChannelHandler handler;

    public AcceptHandler(EventLoopGroup thread, ChannelHandler inHandler) {
        this.selector=thread;
        this.handler=inHandler;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        out.println("Server Registered~~~");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //对于AcceptHandler来讲接收到的是一个SocketChannel
        //对于Listen Socket -> accept -> client
        //对于普通Socket -> R/W -> data
        SocketChannel client = (SocketChannel) msg;

        //响应式Handler
        ChannelPipeline pipeline = client.pipeline();
        pipeline.addLast(handler);

        //注册
        selector.register(client);
    }
}
