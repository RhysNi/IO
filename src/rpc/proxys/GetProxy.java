package rpc.proxys;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.nio.NioSocketChannel;
import rpc.entity.MsgContent;
import rpc.entity.MyHeader;
import rpc.handler.ResponseMappingCallBack;
import rpc.pool.ClientFactory;
import rpc.utils.SerDerUtil;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 动态代理
 *
 * @author Rhys.Ni
 * @date 2022/8/7
 */
public class GetProxy {
    public static <T> T proxy(Class<T> interfaceInfo) {
        ClassLoader classLoader = interfaceInfo.getClassLoader();
        Class<?>[] methodInfo = {interfaceInfo};

        //new InvocationHandler() -> invoke
        return (T) Proxy.newProxyInstance(classLoader, methodInfo, (proxy, method, args) -> {
            String name = interfaceInfo.getName();
            String methodName = method.getName();
            Class<?>[] parameterTypes = method.getParameterTypes();
            //构建消息体
            MsgContent content = MsgContent.builder()
                    .args(args)
                    .name(name)
                    .methodName(methodName)
                    .parameterTypes(parameterTypes)
                    .build();

            //写到内存字节数组中
            byte[] msgBody = SerDerUtil.ser(content);

            //协议：[header<>][msgBody]
            //构建消息头
            MyHeader header = buildHeader(msgBody);

            byte[] msgHeader = SerDerUtil.ser(header);

            //连接池获取连接
            ClientFactory factory = ClientFactory.getFactory();
            NioSocketChannel client = factory.getClient(new InetSocketAddress("192.168.2.237", 9090));

            //发送：IO OUT -> Netty(event驱动)
            ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(msgHeader.length + msgBody.length);

            long requestId = header.getRequestId();
            CompletableFuture<String> response = new CompletableFuture<>();
            ResponseMappingCallBack.addCallBack(requestId, response);

            byteBuf.writeBytes(msgHeader);
            byteBuf.writeBytes(msgBody);
            ChannelFuture send = client.writeAndFlush(byteBuf);
            send.sync();

            return response.get();
        });
    }

    private static MyHeader buildHeader(byte[] msg) {

        int size = msg.length;
        //0x14：0001 0100
        int flag = 0x14141414;
        long requestId = Math.abs(UUID.randomUUID().getLeastSignificantBits());
        MyHeader header = MyHeader.builder()
                .flag(flag)
                .requestId(requestId)
                .dataLength(size)
                .build();
        return header;
    }
}
