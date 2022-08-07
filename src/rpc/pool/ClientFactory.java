package rpc.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import rpc.handler.ClientResponse;
import rpc.handler.ServerDecoder;

import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/8/8 12:33 上午
 */
public class ClientFactory {
    //初始化大小
    int poolSize = 10 ;

    Random random = new Random();

    NioEventLoopGroup group;


    private static final ClientFactory factory;

    static {
        factory = new ClientFactory();
    }

    public static ClientFactory getFactory() {
        return factory;
    }

    ConcurrentHashMap<InetSocketAddress, ClientPool> outBoxs = new ConcurrentHashMap<>();


    public synchronized NioSocketChannel getClient(InetSocketAddress address) throws InterruptedException {
        ClientPool clientPool = outBoxs.get(address);
        //第一次连接
        if (clientPool == null) {
            //不存在的时候添加
            outBoxs.putIfAbsent(address, new ClientPool(poolSize));
            clientPool = outBoxs.get(address);
        }

        int index = random.nextInt(poolSize);
        NioSocketChannel client = clientPool.clients[index];
        //判断连接是否有效
        if (client != null && client.isActive()) {
            return client;
        }

        synchronized (clientPool.lock[index]) {
            return create(address);
        }
    }

    private NioSocketChannel create(InetSocketAddress address) throws InterruptedException {
        //基于netty客户端的创建方式
        group = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        ChannelFuture connect = bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        ChannelPipeline pipeline = nioSocketChannel.pipeline();
                        pipeline.addLast(new ServerDecoder());
                        pipeline.addLast(new ClientResponse());
                    }
                })
                .connect(address);
        return (NioSocketChannel) connect.sync().channel();
    }

    private ClientFactory() {
    }
}
