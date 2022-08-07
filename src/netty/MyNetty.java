package netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static java.lang.System.*;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/8/4 11:04 下午
 */
public class MyNetty {

    /**
     * ByteBuf测试
     *
     * @param
     * @return void
     * @author Rhys.Ni
     * @date 2022/8/6
     */
    @Test
    public void myByteBuf() {
        //分配器
        //ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(8, 20);

        //非池化分配器
        //ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.heapBuffer(8, 20);

        //池化分配器
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.heapBuffer(8, 20);

        print(buffer);

        buffer.writeBytes(new byte[]{1, 2, 3, 4});
        print(buffer);

        buffer.writeBytes(new byte[]{1, 2, 3, 4});
        print(buffer);

        buffer.writeBytes(new byte[]{1, 2, 3, 4});
        print(buffer);

        buffer.writeBytes(new byte[]{1, 2, 3, 4});
        print(buffer);

        buffer.writeBytes(new byte[]{1, 2, 3, 4});
        print(buffer);

        // 超过最大分配上限 IndexOutOfBoundsException
        // buffer.writeBytes(new byte[]{1,2,3,4});
        // print(buffer);
    }

    /**
     * 打印属性
     *
     * @param buf
     * @return void
     * @author Rhys.Ni
     * @date 2022/8/6
     */
    public static void print(ByteBuf buf) {
        //是否可读
        out.println("buf.isReadable(是否可读): " + buf.isReadable());
        //从哪里开始读
        out.println("buf.readerIndex(启读索引): " + buf.readerIndex());
        //可读的字节数
        out.println("buf.readableBytes(可读字节数): " + buf.readableBytes());
        //是否可写
        out.println("buf.isWritable(是否可写): " + buf.isWritable());
        //从哪里开始写
        out.println("buf.writerIndex(启写索引): " + buf.writerIndex());
        //可写字节数
        out.println("buf.writableBytes(可写字节数): " + buf.writableBytes());
        //动态分配空间大小
        out.println("buf.capacity(动态分配上限): " + buf.capacity());
        //最大动态分配空间大小
        out.println("buf.maxCapacity(最大动态分配上限): " + buf.maxCapacity());
        //堆内还是堆外分配 true:堆外  false:堆内
        out.println("buf.isDirect(是否堆外分配): " + buf.isDirect());

        out.println("===========================================");
    }

    /**
     * NioEventLoopGroup线程池
     *
     * @param
     * @return void
     * @author Rhys.Ni
     * @date 2022/8/6
     */
    @Test
    public void loopExecutor() throws IOException {
        //NioEventLoopGroup本身并非多路复用器而是相当于线程池，多路复用器概念是属于由NioEventLoopGroup new出来的线程
        NioEventLoopGroup group = new NioEventLoopGroup(2);

        //selector.execute执行数量取决于NioEventLoopGroup中可创建的线程数
        group.execute(() -> {
            try {
                for (; ; ) {
                    out.println("selector--1");
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        group.execute(() -> {
            try {
                for (; ; ) {
                    out.println("selector--2");
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        in.read();
    }

    /**
     * 客户端模式
     *
     * @param
     * @return void
     * @author Rhys.Ni
     * @date 2022/8/6
     */
    @Test
    public void clientModel() throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        NioSocketChannel client = new NioSocketChannel();
        //将客户端注册到NioEventLoopGroup
        group.register(client);

        //客户端有事件到达多路复用器 基础事件通过管道中调用后续操作的handler
        ChannelPipeline pipeline = client.pipeline();
        pipeline.addLast(new InHandler());


        //客户端连接服务端
        //由于reactor操作都是异步的，所以要利用Future来接收响应数据
        ChannelFuture connect = client.connect(new InetSocketAddress("192.168.2.237", 9090));
        //等待connectFuture
        ChannelFuture connectFuture = connect.sync();

        //拷贝字节数组
        ByteBuf buf = Unpooled.copiedBuffer("Hello Netty".getBytes(StandardCharsets.UTF_8));
        //客户端刷写数据
        ChannelFuture send = client.writeAndFlush(buf);
        //阻塞等待数据成功发送
        send.sync();

        //阻塞等待Client断开连接
        connectFuture.channel().closeFuture().sync();
        out.println("Client断开连接~~~");
    }

    /**
     * 服务端模式
     *
     * @param
     * @return void
     * @author Rhys.Ni
     * @date 2022/8/7
     */
    @Test
    public void serverMode() throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        NioServerSocketChannel server = new NioServerSocketChannel();

        group.register(server);

        ChannelPipeline pipeline = server.pipeline();
        //Accept由此处理
        pipeline.addLast(new AcceptHandler(group, new ChannelInit()));

        //对于服务端Accept后 -> Bind
        ChannelFuture bind = server.bind(new InetSocketAddress("192.168.2.103", 9090));
        //阻塞等待连接成功并阻塞等待Server断开连接
        bind.sync().channel().closeFuture().sync();
    }

    /**
     * netty官方版客户端写法
     *
     * @param
     * @return void
     * @author Rhys.Ni
     * @date 2022/8/7
     */
    @Test
    public void nettyClient() throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        ChannelFuture connect = bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new InHandler());
                    }
                })
                .connect(new InetSocketAddress("192.168.2.103", 9090));

        //阻塞同步接收客户端channel
        Channel client = connect.sync().channel();

        //拷贝字节数组
        ByteBuf buffer = Unpooled.copiedBuffer("".getBytes(StandardCharsets.UTF_8));
        //客户端刷写数据
        ChannelFuture send = client.writeAndFlush(buffer);
        //阻塞等待数据成功发送并阻塞等待Client断开连接
        send.sync();

        //阻塞等待Client断开连接
        client.closeFuture().sync();
        out.println("Client断开连接~~~");
    }

    /**
     * netty官方版服务端写法
     *
     * @param
     * @return void
     * @author Rhys.Ni
     * @date 2022/8/7
     */
    @Test
    public void nettyServer() throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        //需要传两个group 额外一个用于处理Accept
        ChannelFuture bind = serverBootstrap.group(group, group)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new InHandler());
                    }
                })
                .bind(new InetSocketAddress("192.168.2.103", 9090));
        bind.sync().channel().closeFuture().sync();
    }
}
