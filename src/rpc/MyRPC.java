package rpc;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Test;
import rpc.handler.ServerDecoder;
import rpc.handler.ServerRequestHandler;
import rpc.interfaces.Car;
import rpc.proxys.GetProxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.*;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/8/7 8:59 下午
 */
public class MyRPC {
    @Test
    public void get() {
        new Thread(() -> {
            try {
                startServer();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        out.println("Server Started...");

        int size = 20;
        Thread[] threads = new Thread[size];
        AtomicInteger num = new AtomicInteger(0);
        for (int i = 0; i < size; i++) {
            threads[i] = new Thread(() -> {
                Car car = GetProxy.proxy(Car.class);
                String arg = "Hello Car" + num.incrementAndGet();
                String carMsg = car.getCarMsg(arg);
                out.println("Client Over Msg: " + carMsg + " src arg: " + arg);
            });
        }
        for (Thread thread : threads) {
            thread.start();
        }

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startServer() throws InterruptedException {
        //混杂模式：一个 NioEventLoopGroup 既满足客户端又满足连接池
        NioEventLoopGroup boss = new NioEventLoopGroup(50);
        NioEventLoopGroup worker = boss;
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        ChannelFuture bind = serverBootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel socketChannel) throws Exception {
                        out.println("Server Accept Client Port: " + socketChannel.remoteAddress().getPort());
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new ServerDecoder());
                        pipeline.addLast(new ServerRequestHandler());
                    }
                })
                .bind(new InetSocketAddress("192.168.2.237", 9090));
        bind.sync().channel().closeFuture().sync();
    }
}



