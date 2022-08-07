package netty;

import io.netty.channel.*;

/**
 * @description: 过桥handler @Sharable 代表共享的 如果没有此注解 后续用户所传进来的所有XxxHandler都得设计成单例的，单例的就不能支持多客户端连接每次new一个对应Handler
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/8/7 5:31 下午
 */
@ChannelHandler.Sharable
public class ChannelInit extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Channel client = ctx.channel();
        ChannelPipeline pipeline = client.pipeline();
        pipeline.addLast(new InHandler());
        //此时pipeline中有[ChannelInit,InHandler]
        //ChannelInit起到过桥作用，用完即可去除
        ctx.pipeline().remove(this);
    }
}
