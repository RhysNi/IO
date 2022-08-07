package rpc.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import rpc.entity.PackageMsg;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/8/8 1:36 上午
 */
public class ClientResponse extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        PackageMsg responsePackage = (PackageMsg) msg;
        ResponseMappingCallBack.runCallBack(responsePackage);
    }
}
