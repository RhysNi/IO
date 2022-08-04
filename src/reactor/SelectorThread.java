package reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/8/2 12:13 上午
 */
public class SelectorThread extends ThreadLocal<LinkedBlockingQueue<Channel>> implements Runnable {
    Selector selector = null;
    LinkedBlockingQueue<Channel> lbq = get();
    SelectorThreadGroup group;

    @Override
    protected LinkedBlockingQueue<Channel> initialValue() {
        return new LinkedBlockingQueue<>();
    }

    public SelectorThread(SelectorThreadGroup group) {
        try {
            this.group = group;
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 轮询
     *
     * @param
     * @return void
     * @author Rhys.Ni
     * @date 2022/8/2
     */
    @Override
    public void run() {
        //Loop轮询
        while (true) {
            try {
                //阻塞
                int nums = selector.select();

                //处理selectkeys
                if (nums > 0) {
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    //线程内保证线性处理
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        //判断key的可操作状态 可接受新的连接 、 可读 、可写
                        if (key.isAcceptable()) {
                            acceptHandler(key);
                        } else if (key.isReadable()) {
                            readHandler(key);
                        } else if (key.isWritable()) {

                        }
                    }
                }

                //处理一些任务
                if (!lbq.isEmpty()) {
                    Channel channel = lbq.take();
                    //服务端
                    if (channel instanceof ServerSocketChannel) {
                        ServerSocketChannel server = (ServerSocketChannel) channel;
                        server.register(selector, SelectionKey.OP_ACCEPT);
                        System.out.println(Thread.currentThread().getName() + " register listen");
                    }
                    //客户端
                    else if (channel instanceof SocketChannel) {
                        SocketChannel client = (SocketChannel) channel;
                        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4096);
                        client.register(selector, SelectionKey.OP_READ, byteBuffer);
                        System.out.println(Thread.currentThread().getName() + " register client: " + client.getRemoteAddress());
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void readHandler(SelectionKey key) {
        System.out.println(Thread.currentThread().getName() + " read...");
        ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
        SocketChannel client = (SocketChannel) key.channel();
        byteBuffer.clear();
        while (true) {
            try {
                int num = client.read(byteBuffer);
                if (num > 0) {
                    //将读到的内容翻转直接写出
                    byteBuffer.flip();
                    while (byteBuffer.hasRemaining()) {
                        client.write(byteBuffer);
                    }
                    //清理缓冲区
                    byteBuffer.clear();
                } else if (num == 0) {
                    break;
                } else if (num < 0) {
                    //客户端断开了
                    System.out.println("Client: " + client.getRemoteAddress() + " closed...");
                    key.cancel();
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void acceptHandler(SelectionKey key) {
        System.out.println(Thread.currentThread().getName() + " accept...");

        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        try {
            SocketChannel client = server.accept();
            client.configureBlocking(false);

            //选择一个多路复用器并注册
            // group.nextSelector(client);
            // group.nextSelectorV2(client);
            group.nextSelectorV3(client);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setWorker(SelectorThreadGroup worker) {
        this.group = worker;
    }
}
